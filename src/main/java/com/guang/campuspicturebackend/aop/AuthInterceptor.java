package com.guang.campuspicturebackend.aop;

import com.guang.campuspicturebackend.annotation.AuthCheck;
import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import com.guang.campuspicturebackend.exception.ThrowUtils;
import com.guang.campuspicturebackend.model.entity.User;
import com.guang.campuspicturebackend.model.enums.UserRole;
import com.guang.campuspicturebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Author L.
 * @Date 2025/12/17 16:57
 * @Description 权限认证切面
 * @Version 1.0
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object auth(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        UserRole mustRoleEnum = UserRole.getRoleByValue(mustRole);
        // 如果不需要权限则直接放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 获取到HttpServletRequest对象，从而获取用户信息
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User currentUser = userService.getLoginUser(request);
        UserRole userRole = UserRole.getRoleByValue(currentUser.getUserRole());
        ThrowUtils.throwIf(userRole == null, ErrorCode.NO_AUTH_ERROR);
        // 只有管理员才能执行的接口
        if (UserRole.ADMIN.equals(mustRoleEnum) && !UserRole.ADMIN.equals(userRole)) {
            throw new CustomException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
