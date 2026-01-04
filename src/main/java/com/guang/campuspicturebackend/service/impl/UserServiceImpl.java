package com.guang.campuspicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guang.campuspicturebackend.constant.UserConstant;
import com.guang.campuspicturebackend.exception.ErrorCode;
import com.guang.campuspicturebackend.exception.ThrowUtils;
import com.guang.campuspicturebackend.model.dto.user.UserLoginRequest;
import com.guang.campuspicturebackend.model.dto.user.UserQueryRequest;
import com.guang.campuspicturebackend.model.dto.user.UserRegisterRequest;
import com.guang.campuspicturebackend.model.entity.User;
import com.guang.campuspicturebackend.model.enums.UserRole;
import com.guang.campuspicturebackend.model.vo.UserLoginVO;
import com.guang.campuspicturebackend.model.vo.UserVO;
import com.guang.campuspicturebackend.service.UserService;
import com.guang.campuspicturebackend.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Ocean
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-12-16 18:39:21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(UserRegisterRequest userRegister) {
        // 参数校验
        ThrowUtils.throwIf(userRegister == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(userRegister.getUserAccount().length() < 4, ErrorCode.PARAMS_ERROR, "用户名太短");
        ThrowUtils.throwIf(userRegister.getUserPassword().length() < 8, ErrorCode.PARAMS_ERROR, "密码太短");
        ThrowUtils.throwIf(!userRegister.getUserPassword().equals(userRegister.getCheckPassword()), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        // 判断是否已经存在该用户
        Long res = this.baseMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userRegister.getUserAccount())
        );
        ThrowUtils.throwIf(res > 0, ErrorCode.PARAMS_ERROR, "用户已存在");
        // 加密
        String encryptPsw = getEncryptPsw(userRegister.getUserPassword());
        User user = new User();
        BeanUtils.copyProperties(userRegister, user);
        user.setUserPassword(encryptPsw);
        user.setUserRole(UserRole.USER.getValue());
        user.setUserName("gust");
        boolean saveRes = save(user);
        ThrowUtils.throwIf(!saveRes, ErrorCode.SYSTEM_ERROR, "保存用户失败");
        return user.getId();
    }

    @Override
    public UserLoginVO userLogin(UserLoginRequest userLogin, HttpServletRequest request) {
        ThrowUtils.throwIf(userLogin == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(userLogin.getUserAccount() == null || userLogin.getUserAccount().length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userLogin.getUserPassword() == null || userLogin.getUserPassword().length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        String encryptPsw = getEncryptPsw(userLogin.getUserPassword());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userLogin.getUserAccount())
                .eq(User::getUserPassword, encryptPsw)
        );
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        // 记录用户登陆状态
        // 将登录态保存到redis缓存当中
        stringRedisTemplate.opsForValue().set("user:state:" + UserConstant.USER_LOGIN_STATE, JSONUtil.toJsonStr(user), 1, TimeUnit.DAYS);
//        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public String getEncryptPsw(String password) {
        final String salt = "guang";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }

    @Override
    public UserLoginVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        return userLoginVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 从redis中获取到用户的登录态
        String str = stringRedisTemplate.opsForValue().get("user:state:" + UserConstant.USER_LOGIN_STATE);
        User user = JSONUtil.toBean(str, User.class);
//        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(user == null || user.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        user = this.getById(user.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 只有登录后才能操作此方法
        User user = this.getLoginUser(request);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String orderFiled = userQueryRequest.getOrderFiled();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq(ObjUtil.isNotNull(id), "id", id)
                .like(StrUtil.isNotBlank(userName), "user_name", userName)
                .like(StrUtil.isNotBlank(userAccount), "user_account", userAccount)
                .like(StrUtil.isNotBlank(userProfile), "user_profile", userProfile)
                .eq(StrUtil.isNotBlank(userRole), "user_role", userRole)
                .orderBy(StrUtil.isNotEmpty(orderFiled), sortOrder.equals("desc"), orderFiled);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getUserRole().equals(UserRole.ADMIN.getValue());
    }
}




