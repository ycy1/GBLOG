package com.mojian.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.mojian.common.Result;
import com.mojian.vo.article.ArticleListVo;
import com.mojian.vo.article.CategoryListVo;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

/**
 * @author xxj
 * @title HttpApi
 * @date 2026/5/25 22:50
 * @description TODO
 */

@RetrofitClient(baseUrl ="http://127.0.0.1:8800/api/app/")
public interface HttpApi {
    @GET("article/list")
    Result<Page<ArticleListVo>> getArticle(@Query("pageNum")Integer pageNum, @Query("pageSize")Integer pageSize);


    @GET("article/categories")
    Result<List<CategoryListVo>> getCategories();
}
