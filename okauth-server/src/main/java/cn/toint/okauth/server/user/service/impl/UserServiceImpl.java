/*
 * Copyright 2025 Toint (599818663@qq.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.toint.okauth.server.user.service.impl;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.toint.okauth.server.exception.UserPasswordException;
import cn.toint.okauth.server.properties.OkAuthProperties;
import cn.toint.okauth.server.user.event.UserLoginEvent;
import cn.toint.okauth.server.user.mapper.UserMapper;
import cn.toint.okauth.server.user.model.*;
import cn.toint.okauth.server.user.service.UserEncryptService;
import cn.toint.okauth.server.user.service.UserService;
import cn.toint.oksms.aliyun.AliyunSmsClient;
import cn.toint.oksms.aliyun.model.AliyunRegionEnum;
import cn.toint.oksms.aliyun.model.AliyunSmsClientConfig;
import cn.toint.oksms.aliyun.model.AliyunSmsSendRequest;
import cn.toint.oksms.aliyun.model.AliyunSmsSendResponse;
import cn.toint.oksms.util.SmsUtil;
import cn.toint.oktool.spring.boot.cache.Cache;
import cn.toint.oktool.util.Assert;
import cn.toint.oktool.util.JacksonUtil;
import cn.toint.oktool.util.KeyBuilderUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.SqlUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.data.PasswdStrength;
import org.dromara.hutool.core.lang.Validator;
import org.dromara.hutool.core.util.RandomUtil;
import org.dromara.hutool.extra.spring.SpringUtil;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Toint
 * @date 2025/6/29
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    /**
     * 缓存
     */
    @Resource
    private Cache cache;

    /**
     * 短信客户端
     */
    private AliyunSmsClient aliyunSmsClient;

    /**
     * 配置
     */
    @Resource
    private OkAuthProperties okAuthProperties;

    @Resource
    private UserMapper userMapper;

    /**
     * 用户信息加密
     */
    @Resource
    private UserEncryptService userEncryptService;

    @Override
    public UserLoginResponse login(UserLoginByPasswordRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 账号密码
        String username = request.getUsername();
        String password = request.getPassword();

        // 校验用户是否存在
        UserDo userDo = userMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(UserDo::getUsername).eq(username)
                        .or(UserDo::getPhone).eq(username)
                        .or(UserDo::getEmail).eq(username));

        if (userDo == null) {
            // 用户不存在, 自动注册
            userDo = register(username, password);
        } else {
            // 用户存在, 登录校验
            if (!Objects.equals(userDo.getPassword(), userEncryptService.encrypt(password))) {
                throw new UserPasswordException("密码错误");
            }
        }

        return checkSuccessThenLogin(userDo);
    }

    /**
     * 校验成功后执行登录, 特别注意: 必须是经过登录判断后才可调用本方法, 本方法无任何判断能力
     */
    private UserLoginResponse checkSuccessThenLogin(UserDo userDo) {
        Assert.notNull(userDo, "非法参数");

        // 执行到这里说明这个用户一切正常, 获取用户的登录信息返回给客户端
        SaTokenInfo tokenInfo = SaTokenContextMockUtil.setMockContext(() -> {
            StpUtil.login(userDo.getId());
            return StpUtil.getTokenInfo();
        });

        // res
        UserLoginResponse.user user = new UserLoginResponse.user();
        BeanUtil.copyProperties(userDo, user);

        UserLoginResponse.Token token = new UserLoginResponse.Token();
        BeanUtil.copyProperties(tokenInfo, token);

        UserLoginResponse loginResponse = new UserLoginResponse();
        loginResponse.setUser(user);
        loginResponse.setToken(token);

        // 发布登录事件
        SpringUtil.publishEvent(new UserLoginEvent(loginResponse));
        return loginResponse;
    }

    /**
     * 注册用户
     *
     * @param username 账号
     * @param password 密码明文
     * @return 注册的用户对象
     */
    private UserDo register(String username, String password) {
        Assert.notBlank(username, "用户名不能为空");
        Assert.notBlank(password, "密码不能为空");

        // 校验用户名格式
        checkUsernameFormat(username);

        // 校验密码
        checkPasswordFormat(password);
        String encryptPassword = userEncryptService.encrypt(password);

        // 昵称
        String nickName = genNickName();

        UserDo userDo = new UserDo();
        userDo.init();
        userDo.setUsername(username);
        userDo.setPassword(encryptPassword);
        userDo.setName(nickName);
        int insertStatus = userMapper.insert(userDo, false);
        Assert.isTrue(SqlUtil.toBool(insertStatus), "用户账号创建失败");
        return userDo;
    }

    private static void checkPasswordFormat(String password) {
        Assert.isTrue(password.length() >= 6, "您的密码过短");
        Assert.isTrue(password.length() <= 20, "您的密码过长");
        Assert.isTrue(password.matches("^[A-Za-z0-9]+$"), "密码只允许包含字母或数字");
        Assert.isTrue(PasswdStrength.check(password) >= 4, "您的密码太简单");
    }

    private static void checkUsernameFormat(String username) {
        Assert.isTrue(username.length() >= 3, "您的用户名过短");
        Assert.isTrue(username.length() <= 20, "您的用户名过长");
        Assert.isTrue(username.matches("^[a-z0-9]+$"), "用户名只允许包含小写字母或数字");
    }

    /**
     * 生成用户昵称
     */
    private String genNickName() {
        StringBuilder nickNameBuilder = new StringBuilder();
        nickNameBuilder.append("用户");
        for (int i = 0; i < 5; i++) {
            nickNameBuilder.append(RandomUtil.randomChar(RandomUtil.BASE_CHAR));
        }
        return nickNameBuilder.toString();
    }

    @Override
    public UserDo getById(Long userId) {
        Assert.notNull(userId, "userId不能为空");
        return userMapper.selectOneById(userId);
    }

    @Override
    public UserLoginResponse login(UserLoginBySmsRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request, "请求参数校验失败: {}");

        // 校验验证码, 成功后删除
        String cacheKey = buildLoginCodeCacheKey(request.getCode());
        String phone = cache.get(cacheKey);
        Assert.notBlank(phone, "验证码错误");
        cache.delete(cacheKey);

        // 查询账号,
        UserDo userDo = getByPhone(phone);
        if (userDo == null) {
            userDo = new UserDo();
            userDo.init();
            userDo.setUsername(phone);
            userDo.setPhone(phone);
            userDo.setName(genNickName());
            int inserted = userMapper.insert(userDo, false);
        }

        return checkSuccessThenLogin(userDo);
    }

    @Override
    public UserDo getByPhone(String phone) {
        Assert.notBlank(phone, "查询的手机号码不能为空");
        return userMapper.selectOneByQuery(QueryWrapper.create().eq(UserDo::getPhone, phone));
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notBlank(oldPassword, "旧密码不能为空");
        Assert.notBlank(newPassword, "新密码不能为空");
        Assert.notEquals(oldPassword, newPassword, "新旧密码不能一致");
        UserDo userDo = getById(userId);
        Assert.notNull(userDo, "用户[{}]不存在", userId);

        Assert.equals(userEncryptService.encrypt(oldPassword), userDo.getPassword(), "密码错误");
        checkPasswordFormat(newPassword);
        userDo.setPassword(userEncryptService.encrypt(newPassword));
        userDo.freshUpdateTime();
        userMapper.update(userDo, false);
    }

    @Override
    public UserDo getByUsername(String username) {
        Assert.notBlank(username, "账号不能为空");
        return userMapper.selectOneByQuery(QueryWrapper.create()
                .eq(UserDo::getUsername, username));
    }

    @Override
    public void sendLoginSms(UserLoginSendSmsRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request, "请求参数校验失败: {}");
        Assert.notNull(aliyunSmsClient, "未开启短信服务");

        // 开放应用是否存在
        Long clientId = request.getClientId();
        UserDo userDo = getById(clientId);
        Assert.notNull(userDo, "开放应用[{}]不存在", clientId);

        // 短信配置
        String phone = request.getPhone();
        Assert.isTrue(Validator.isMobile(phone), "手机号码格式错误");
        OkAuthProperties.Sms.LoginCode loginCode = okAuthProperties.getSms().getLoginCode();
        String signName = loginCode.getSignName();
        Assert.notBlank(signName, "短信配置错误");
        String templateCode = loginCode.getTemplateCode();
        Assert.notBlank(templateCode, "短信配置错误");
        Map<String, Object> templateParam = loginCode.getTemplateParam();
        Assert.notEmpty(templateParam, "短信配置错误");

        templateParam.forEach((k, v) -> {
            if ("code4()".equals(v)) {
                templateParam.put(k, cacheLoginSmsCode(phone, SmsUtil::smsCode4));
            } else if ("code6()".equals(v)) {
                templateParam.put(k, cacheLoginSmsCode(phone, SmsUtil::smsCode6));
            }
        });

        // 发送短信
        AliyunSmsSendRequest aliyunSmsSendRequest = new AliyunSmsSendRequest();
        aliyunSmsSendRequest.setPhoneNumbers(List.of(phone));
        aliyunSmsSendRequest.setSignName(signName);
        aliyunSmsSendRequest.setTemplateCode(templateCode);
        aliyunSmsSendRequest.setTemplateParam(templateParam);
        AliyunSmsSendResponse aliyunSmsSendResponse = aliyunSmsClient.send(aliyunSmsSendRequest);
        log.info("开放应用[{}]短信验证码下发结果: {}", clientId, JacksonUtil.writeValueAsString(aliyunSmsSendResponse));
    }

    /**
     * 缓存登录短信验证码
     *
     * @param phone        手机号码
     * @param codeSupplier 验证码生成
     * @return 验证码
     */
    private String cacheLoginSmsCode(String phone, Supplier<String> codeSupplier) {
        while (true) {
            String code = codeSupplier.get();
            String key = buildLoginCodeCacheKey(code);
            Duration timeout = okAuthProperties.getSms().getLoginCode().getTimeout();
            boolean putIfAbsent = cache.putIfAbsent(key, phone, timeout);
            if (putIfAbsent) return code;
        }
    }

    /**
     * 构建登录验证码缓存key
     *
     * @param code 验证码
     * @return 缓存key
     */
    private String buildLoginCodeCacheKey(String code) {
        return KeyBuilderUtil.of("userLoginCode").build(code);
    }

    @PostConstruct
    private void initSmsClient() {
        OkAuthProperties.Sms sms = okAuthProperties.getSms();
        if (!sms.isEnableSms()) {
            log.info("用户登录短信验证未开启");
        }

        String smsKey = sms.getSmsKey();
        Assert.notBlank(smsKey, "短信smsKey不能为空");
        String smsSecret = sms.getSmsSecret();
        Assert.notBlank(smsSecret, "短信smsSecret不能为空");

        AliyunSmsClientConfig aliyunSmsClientConfig = new AliyunSmsClientConfig();
        aliyunSmsClientConfig.setAccessKeyId(smsKey);
        aliyunSmsClientConfig.setAccessKeySecret(smsSecret);
        aliyunSmsClientConfig.regionId(AliyunRegionEnum.CN_HANGZHOU);
        aliyunSmsClient = SmsUtil.aliyunSms(aliyunSmsClientConfig);
        log.info("阿里云短信客户端初始化成功");
    }
}
