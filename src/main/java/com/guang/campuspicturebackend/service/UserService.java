package com.guang.campuspicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guang.campuspicturebackend.model.dto.user.UserLoginRequest;
import com.guang.campuspicturebackend.model.dto.user.UserQueryRequest;
import com.guang.campuspicturebackend.model.dto.user.UserRegisterRequest;
import com.guang.campuspicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guang.campuspicturebackend.model.vo.UserLoginVO;
import com.guang.campuspicturebackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author Ocean
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-12-16 18:39:21
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegister 注册信息
     * @return 用户id
     */
    long userRegister(UserRegisterRequest userRegister);

    UserLoginVO userLogin(UserLoginRequest userLogin, HttpServletRequest request);

    String getEncryptPsw(String password);

    UserLoginVO getLoginUserVO(User user);

    /**
     * 从当前会话中获取到登录用户
     * @param request 请求
     * @return 登录用户
     */
    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);


    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    boolean isAdmin(User user);
}
