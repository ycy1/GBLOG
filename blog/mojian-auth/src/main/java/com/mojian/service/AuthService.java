package com.mojian.service;

import com.alibaba.fastjson.JSONObject;
import com.mojian.dto.Captcha;
import com.mojian.dto.EmailRegisterDto;
import com.mojian.dto.LoginDTO;
import com.mojian.dto.user.LoginUserInfo;
import com.mojian.entity.SysUser;
import com.mojian.vo.user.SysUserVo;
import me.zhyd.oauth.model.AuthCallback;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AuthService {

    /**
     * 用户登录
     */
    LoginUserInfo login(LoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     */
    SysUserVo getLoginUserInfo(String source);

    /**
     * 用户更新
     */
    Boolean updateProfile(SysUser user);

    /**
     * 发送注册邮箱验证码
     * @param email
     * @param type 1:注册 2:重置密码
     * @return
     */
    String sendEmailCode(String email, Integer type) throws MessagingException;

    /**
     * 邮箱账号注册
     * @param dto
     * @return
     */
    Boolean register(EmailRegisterDto dto);

    /**
     * 邮箱账号重置密码
     * @param dto
     * @return
     */
    Boolean forgot(EmailRegisterDto dto);

    /**
     *  获取微信扫码登录验证码
     * @return
     */
    String getWechatLoginCode();


    /**
     * 验证微信是否扫码登录
     * @param loginCode
     * @return
     */
    LoginUserInfo getWechatIsLogin(String loginCode);

    /**
     * 微信公众号登录
     * @param message
     * @return
     */
//    String wechatLogin(WxMpXmlMessage message);

    /**
     * 获取第三方授权地址
     * @param source
     * @return
     */
    String renderAuth(String source);

    /**
     * 第三方授权登录
     * @param source
     * @param httpServletResponse
     */
    void authLogin(AuthCallback callback,String source, HttpServletResponse httpServletResponse) throws IOException;

    /**
     * 小程序登录
     * @param code
     * @return
     */
    LoginUserInfo appletLogin(String code, JSONObject userInfo);


    LoginUserInfo wechatAuthLogin(JSONObject userInfo);


    /**
     * 获取滑块验证码
     * @return
     */
    Captcha getCaptcha();
}
