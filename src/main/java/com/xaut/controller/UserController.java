package com.xaut.controller;

import com.xaut.constant.HeaderConstant;
import com.xaut.dao.UserInfoDao;
import com.xaut.dto.TokenModel;
import com.xaut.dto.UserInfoDto;
import com.xaut.entity.UserInfo;
import com.xaut.manager.TokenManager;
import com.xaut.service.UserService;
import com.xaut.util.ResultBuilder;
import com.xaut.web.annotation.Authorization;
import com.xaut.web.annotation.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Author : wangzhe
 * Date : on 2018/01/25
 * Description :
 * Version :
 */
@RestController
@RequestMapping("/user/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoDao userInfoDao;

    @Autowired
    private TokenManager tokenManager;

    /**
     * 用户注册
     *
     * @param response 响应头
     * @param username 用户名
     * @param password 密码
     * @return 响应实体 {@link Object}
     */
    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Object register(HttpServletResponse response, String username, String password, MultipartFile file) {
        boolean flag = userService.checkRegister(username, password, file);
        if (flag) {
            UserInfo user = userInfoDao.selectByName(username);
            // 生成一个 token，保存用户登录状态
            TokenModel model = tokenManager.createToken(user.getUid());
            response.setHeader(HeaderConstant.X_AUTH_TOKEN, model.toString());
            response.setHeader("username", user.getName());
            /**
             *  todo
             */
            return ResultBuilder.create().code(200).message("注册成功").data("userinfo", user).build();
        }
        return ResultBuilder.create().code(500).message("用户名已存在").build();
    }

    @PostMapping("/login")
    public Object login(HttpServletResponse response, @RequestParam String username, @RequestParam String password) {
        boolean flag = userService.checkLogin(username, password);
        if (flag) {
            UserInfo user = userInfoDao.selectByName(username);

            // 设置格式
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST");
            response.setHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");
            response.setContentType("text/html;charset=utf-8");
            response.setCharacterEncoding("utf-8");

            // 生成一个 token，保存用户登录状态
            TokenModel model = tokenManager.createToken(user.getUid());
            Cookie cookie = new Cookie(HeaderConstant.X_AUTH_TOKEN,  model.toString());
            // 有效期,秒为单位
            cookie.setMaxAge(3600);
            response.addCookie(cookie);


            /**
             * todo
             */
            response.setHeader(HeaderConstant.X_AUTH_TOKEN, model.toString());
            response.setHeader("username", user.getName());
            return ResultBuilder.create().code(200).message("请去首页进行选购").data("userinfo", user).build();
        }
        return ResultBuilder.create().code(500).message("用户名或者密码错误").build();
    }

    /**
     * 用户退出登录
     *
     * @param user 注入的当前用户信息
     * @return 响应实体 {@link Object}
     */
    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.DELETE)
    @Authorization
    public Object loginOff(@CurrentUser UserInfo user) {
        System.out.println("loginoff");
        tokenManager.deleteToken(user.getUid());
        return ResultBuilder.create().code(200).message("注销成功").build();
    }

    @RequestMapping("/buy")
    public String buy(@CurrentUser UserInfoDto a) {
        return "buy success";
    }
}
