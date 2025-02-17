package com.sinohealth.common.utils;

import com.sinohealth.common.constant.HttpStatus;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Objects;
import java.util.Optional;

/**
 * 安全服务工具类
 */
@Slf4j
public class SecurityUtils {
    /**
     * 暂用于全流程控制
     */
    private static final ThreadLocal<Long> USER_ID_LOCAL = new ThreadLocal<>();

    /**
     * 获取用户中文名
     *
     * @return
     */
    public static String getRealName() {
        try {
            return getLoginUser().getUser().getRealName();
        } catch (Exception e) {
            throw new CustomException("获取用户账户异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户账户
     **/
    public static String getUsername() {
        try {
            return getLoginUser().getUsername();
        } catch (Exception e) {
            throw new CustomException("获取用户账户异常", HttpStatus.UNAUTHORIZED);
        }
    }

    public static Long getUserId() {
        try {
            return getLoginUser().getUserId();
        } catch (Exception e) {
            throw new CustomException("获取用户账户异常", HttpStatus.UNAUTHORIZED);
        }
    }

    public static Long getUserIdIgnoreError() {
        try {
            return getLoginUser().getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    public static Optional<Pair<Long, String>> getUserPairIgnoreError() {
        try {
            LoginUser loginUser = getLoginUser();
            return Optional.ofNullable(loginUser).map(v -> Pair.create(v.getUserId(), v.getRealName()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Long getUserIdOrSystem() {
        try {
            return getLoginUser().getUserId();
        } catch (Exception e) {
            return 0L;
        }
    }

    public static String getLogUserId() {
        try {
            return Optional.of(getLoginUser()).map(LoginUser::getUserId).map(v -> v + "").orElse("");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取用户
     **/
    public static LoginUser getLoginUser() {
        try {
            return (LoginUser) getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new CustomException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    public static boolean isAdmin() {
        Long userId = getUserId();
        return userId != null && 1L == userId;
    }

    /**
     * 设置用户id 到threadLocal
     *
     * @param userId 用户id
     */
    public static void setLocalUserId(Long userId) {
        USER_ID_LOCAL.set(userId);
    }

    /**
     * 获取缓存的用户id
     *
     * @return 用户id
     */
    public static Long getLocalUserId() {
        return USER_ID_LOCAL.get();
    }

    /**
     * 清除threadLocal
     */
    public static void removeLocal() {
        USER_ID_LOCAL.remove();
    }

    /**
     * 获取登录用户id
     * 若无，则获取本地缓存
     *
     * @return 用户id
     */
    public static Long getUserIdOrLocal() {
        try {
            return getUserId();
        } catch (Exception e) {
            Long localUserId = getLocalUserId();
            if (Objects.nonNull(localUserId)) {
                return localUserId;
            }
            throw e;
        }
    }
}
