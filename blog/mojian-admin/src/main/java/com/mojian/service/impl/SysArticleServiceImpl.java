package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.plugin.markdown.MarkdownRenderData;
import com.deepoove.poi.plugin.markdown.MarkdownRenderPolicy;
import com.deepoove.poi.plugin.markdown.MarkdownStyle;
import com.mojian.common.Constants;
import com.mojian.common.ResultCode;
import com.mojian.dto.article.ArticleQueryDto;
import com.mojian.entity.SysArticle;
import com.mojian.entity.SysCategory;
import com.mojian.entity.SysTag;
import com.mojian.exception.ServiceException;
import com.mojian.mapper.SysArticleMapper;
import com.mojian.mapper.SysCategoryMapper;
import com.mojian.mapper.SysTagMapper;
import com.mojian.service.SysArticleService;
import com.mojian.utils.AiUtil;
import com.mojian.utils.DateUtil;
import com.mojian.utils.FileUtils;
import com.mojian.utils.PageUtil;
import com.mojian.vo.article.ArticleDetailVo;
import com.mojian.vo.article.ArticleListVo;
import com.mojian.vo.article.SysArticleDetailVo;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SysArticleServiceImpl extends ServiceImpl<SysArticleMapper, SysArticle> implements SysArticleService {

    private final SysTagMapper sysTagMapper;

    private final AiUtil aiUtil;
    private final SysCategoryMapper sysCategoryMapper;

    @Override
    public IPage<ArticleListVo> selectPage(ArticleQueryDto articleQueryDto) {
        return baseMapper.selectPageList(PageUtil.getPage(), articleQueryDto);
    }

    @Override
    public SysArticleDetailVo detail(Integer id) {
        SysArticle sysArticle = baseMapper.selectById(id);

        SysArticleDetailVo sysArticleDetailVo = new SysArticleDetailVo();
        BeanUtils.copyProperties(sysArticle, sysArticleDetailVo);

        SysCategory sysCategory = sysCategoryMapper.selectById(sysArticle.getCategoryId());
        sysArticleDetailVo.setCategoryName(sysCategory.getName());

        //获取标签
        List<String> tags = sysTagMapper.getTagNameByArticleId(id);
        sysArticleDetailVo.setTags(tags);
        return sysArticleDetailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SysArticleDetailVo sysArticle) {

        SysArticle obj = new SysArticle();
        BeanUtils.copyProperties(sysArticle, obj);
        obj.setUserId(StpUtil.getLoginIdAsLong());

        //添加分类
        addCategory(sysArticle, obj);
        baseMapper.insert(obj);

        addTags(sysArticle, obj);

        ThreadUtil.execAsync(() -> {
            String res = aiUtil.send(obj.getContent() + "请提供一段简短的介绍描述该文章的内容");
            if (StringUtils.isNotBlank(res)) {
                obj.setAiDescribe(res);
                baseMapper.updateById(obj);
            }
        });
        return true;
    }




    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SysArticleDetailVo sysArticle) {

        SysArticle obj = new SysArticle();
        BeanUtils.copyProperties(sysArticle, obj);

        //没有管理员权限就只能修改自己的文章
        if (!StpUtil.hasRole(Constants.ADMIN)) {
            SysArticle article = baseMapper.selectById(sysArticle.getId());
            if (article.getUserId() != StpUtil.getLoginIdAsLong()) {
                throw new ServiceException("只能修改自己的文章");
            }
        }

        addCategory(sysArticle, obj);
        baseMapper.updateById(obj);

        //先删除标签在新增标签
        sysTagMapper.deleteArticleTagsByArticleIds(Collections.singletonList(obj.getId()));
        addTags(sysArticle, obj);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> ids) {

        //没有管理员权限就只能删除自己的文章
        if (!StpUtil.hasRole(Constants.ADMIN)) {
            List<SysArticle> sysArticles = baseMapper.selectBatchIds(ids);
            for (SysArticle sysArticle : sysArticles) {
                if (sysArticle.getUserId() != StpUtil.getLoginIdAsLong()) {
                    throw new RuntimeException("只能删除自己的文章");
                }
            }
        }

        baseMapper.deleteBatchIds(ids);
        sysTagMapper.deleteArticleTagsByArticleIds(ids);
        return true;
    }


    @Override
    public void reptile(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            Elements title  = document.getElementsByClass("title-article");
            Elements tags  = document.getElementsByClass("tag-link");
            Elements content  = document.getElementsByClass("article_content");
            if (StringUtils.isBlank(content.toString())) {
                throw new ServiceException(ResultCode.CRAWLING_ARTICLE_FAILED.getDesc());
            }

            //爬取的是HTML内容，需要转成MD格式的内容
            String newContent = content.get(0).toString().replaceAll("<code>", "<code class=\"lang-java\">");
            String markdown = FlexmarkHtmlConverter.builder(new MutableDataSet()).build().convert(newContent)
                    .replace("lang-java","java");

            SysArticle entity = SysArticle.builder().userId(StpUtil.getLoginIdAsLong()).contentMd(markdown)
                    .isOriginal(Constants.NO).originalUrl(url).categoryId(32)
                    .title(title.get(0).text()).cover("https://api.btstu.cn/sjbz/api.php?lx=dongman&format=images").content(newContent).build();

            baseMapper.insert(entity);
            //为该文章添加标签
            List<Integer> tagIds = new ArrayList<>();
            tags.forEach(item ->{
                String tag = item.text();
                SysTag result = sysTagMapper.selectOne(new LambdaQueryWrapper<SysTag>()
                        .eq(SysTag::getName, tag).eq(SysTag::getType, "article"));
                if (result == null){
                    result = SysTag.builder().name(tag).type("article").build();
                    sysTagMapper.insert(result);
                }
                tagIds.add(result.getId());
            });
            Set<Integer> collect = new HashSet<>(tagIds);
            sysTagMapper.addArticleTagRelations(entity.getId(), new ArrayList<>(collect));

//            System.out.println("文章抓取成功，内容为:" + JSON.toJSONString(entity));
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<byte[]> exportWord(Long id) {
        ArticleDetailVo articleDetail = baseMapper.getArticleDetail(id);
        if (articleDetail == null){
            throw new ServiceException("文章不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("title", articleDetail.getTitle());
        data.put("author", articleDetail.getNickname());
        data.put("time", DateUtil.formatLocalDateTime(articleDetail.getCreateTime(), DateUtil.YYYY_MM_DD));
        data.put("categoryName", articleDetail.getCategory().getName());
        data.put("original", !StringUtils.isEmpty(articleDetail.getOriginalUrl()));
        data.put("originalUrl", articleDetail.getOriginalUrl());

        List<File> imgFiles = convertMd(articleDetail);
        MarkdownRenderData codeMd = new MarkdownRenderData();
        codeMd.setMarkdown(articleDetail.getContentMd());
        codeMd.setStyle(MarkdownStyle.newStyle());
        data.put("md", codeMd);

        // 2. 编译模板并渲染
        String basePath = System.getProperty("user.dir");
        HttpHeaders headers = new HttpHeaders();
        byte[] byteArray = null;
        try{
            ConfigureBuilder builder = Configure.builder();
            builder.useSpringEL(); // 使用SpringEL
            builder.bind("md", new MarkdownRenderPolicy());
            String outputPath = basePath + File.separator + "template";
            XWPFTemplate template = XWPFTemplate.compile(outputPath + File.separator + "template_article.docx", builder.build()).render(data);

            // 3. 输出文档
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            template.writeAndClose(bao);
            // 删除图片文件
            for (File imgFile : imgFiles) {FileUtil.del(imgFile);}
            byteArray = bao.toByteArray();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + URLEncoder.encode( articleDetail.getTitle() +"_" + DateUtil.parseDateToStr(DateUtil.YYYYMMDDHHMMSS, DateUtil.getNowDate()) + ".docx", StandardCharsets.UTF_8));
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(byteArray.length);
        }catch (Exception e){
            log.error("导出文档失败:{}", e.getMessage());
            throw new ServiceException("导出文档失败");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(byteArray);
    }

    private List<File> convertMd(ArticleDetailVo detailVo) {
        StringBuilder result = new StringBuilder();
        List<File> imgFiles = new ArrayList<>();

        try {
            Matcher matcher = Pattern.compile("!\\[([^]]*)]\\(([^)]+)\\)")
                    .matcher(detailVo.getContentMd());
            String basePath = System.getProperty("user.dir");
            while (matcher.find()) {
//                System.out.println("Alt: " + matcher.group(1));
//                System.out.println("URL: " + matcher.group(2));
                String fileName = FileUtils.getFileNameFromUrl(matcher.group(2));
                File file = FileUtils.urlToFile(matcher.group(2));
                if(file == null){
                    throw new ServiceException("md中图片不存在");
                }
                File filePng = FileUtils.uncompressFile(file);
                String outputPath = basePath + File.separator + "template" + File.separator + "image" + File.separator
                        + fileName.substring(0, fileName.lastIndexOf('.')) + ".png";
                File fileDest = new File(outputPath);
                FileUtil.copy(filePng, fileDest, false);
                imgFiles.add(fileDest);
                String replacement = "![" + matcher.group(1) + "](" + outputPath + ")"; // 构建新的png图片路径
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(result);
            detailVo.setContentMd(result.toString());
        } catch (Exception e) {
            throw new ServiceException("md转换失败");
        }
        return imgFiles;
    }

    private void addCategory(SysArticleDetailVo sysArticle, SysArticle obj) {
        SysCategory sysCategory = sysCategoryMapper.selectOne(new LambdaQueryWrapper<SysCategory>()
                .eq(SysCategory::getName, sysArticle.getCategoryName()));
        if (sysCategory == null) {
            sysCategory = SysCategory.builder().name(sysArticle.getCategoryName()).build();
            sysCategoryMapper.insert(sysCategory);
        }
        obj.setCategoryId(sysCategory.getId());
    }

    private void addTags(SysArticleDetailVo sysArticle, SysArticle obj) {
        //添加标签
        List<Integer> tagIds = new ArrayList<>();
        for (String tag : sysArticle.getTags()) {
            SysTag sysTag = sysTagMapper.selectOne(new LambdaQueryWrapper<SysTag>()
                    .eq(SysTag::getName, tag).eq(SysTag::getType, "article"));
            if (sysTag == null) {
                sysTag = SysTag.builder().name(tag).type("article").build();
                sysTagMapper.insert(sysTag);
            }
            tagIds.add(sysTag.getId());
        }
        sysTagMapper.addArticleTagRelations(obj.getId(), tagIds);
    }
}
