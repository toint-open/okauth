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
import cn.toint.okauth.server.exception.UserNotExistException;
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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.lang.Validator;
import org.dromara.hutool.core.text.StrUtil;
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
                QueryWrapper.create().eq(UserDo::getUsername, username));
        if (userDo == null) {
            throw new UserNotExistException(StrUtil.format("账号[{}]不存在", username));
        }

        // 校验密码是否一致
        password = userEncryptService.encrypt(password);
        if (!Objects.equals(userDo.getPassword(), password)) {
            throw new UserPasswordException("密码错误");
        }

        // 登录
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

    @Override
    public UserDo getById(Long userId) {
        Assert.notNull(userId, "userId不能为空");
        return userMapper.selectOneById(userId);
    }

    @Override
    public UserLoginResponse login(UserLoginBySmsRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request, "请求参数校验失败: {}");

        // 校验验证码
        String phone = cache.get(buildLoginCodeCacheKey(request.getCode()));
        Assert.notBlank(phone, "验证码错误");

        log.info("手机号码验证成功: {}", phone);

        return null;
    }

    @Override
    public void sendSms(UserLoginSendSmsRequest request) {
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
    }
}
