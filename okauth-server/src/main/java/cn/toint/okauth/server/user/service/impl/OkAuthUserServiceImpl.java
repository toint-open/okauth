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
import cn.toint.okauth.server.exception.OkAuthUserNotExistException;
import cn.toint.okauth.server.exception.OkAuthUserPasswordException;
import cn.toint.okauth.server.user.event.OkAuthUserLoginEvent;
import cn.toint.okauth.server.user.mapper.OkAuthUserMapper;
import cn.toint.okauth.server.user.model.OkAuthUserDo;
import cn.toint.okauth.server.user.model.OkAuthUserLoginByPasswordRequest;
import cn.toint.okauth.server.user.model.OkAuthUserLoginResponse;
import cn.toint.okauth.server.user.service.OkAuthUserEncryptService;
import cn.toint.okauth.server.user.service.OkAuthUserService;
import cn.toint.oktool.util.Assert;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.extra.spring.SpringUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Toint
 * @date 2025/6/29
 */
@Service
@Slf4j
public class OkAuthUserServiceImpl implements OkAuthUserService {
    @Resource
    private OkAuthUserMapper okAuthUserMapper;

    /**
     * 用户信息加密
     */
    @Resource
    private OkAuthUserEncryptService okAuthUserEncryptService;

    @Override
    public OkAuthUserLoginResponse login(OkAuthUserLoginByPasswordRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 账号密码
        String username = request.getUsername();
        String password = request.getPassword();

        // 校验用户是否存在
        OkAuthUserDo userDo = okAuthUserMapper.selectOneByQuery(
                QueryWrapper.create().eq(OkAuthUserDo::getUsername, username));
        if (userDo == null) {
            throw new OkAuthUserNotExistException(StrUtil.format("账号[{}]不存在", username));
        }

        // 校验密码是否一致
        password = okAuthUserEncryptService.encrypt(password);
        if (!Objects.equals(userDo.getPassword(), password)) {
            throw new OkAuthUserPasswordException("密码错误");
        }

        // 登录
        SaTokenInfo tokenInfo = SaTokenContextMockUtil.setMockContext(() -> {
            StpUtil.login(userDo.getId());
            return StpUtil.getTokenInfo();
        });

        // res
        OkAuthUserLoginResponse.user user = new OkAuthUserLoginResponse.user();
        BeanUtil.copyProperties(userDo, user);

        OkAuthUserLoginResponse.Token token = new OkAuthUserLoginResponse.Token();
        BeanUtil.copyProperties(tokenInfo, token);

        OkAuthUserLoginResponse loginResponse = new OkAuthUserLoginResponse();
        loginResponse.setUser(user);
        loginResponse.setToken(token);

        // 发布登录事件
        SpringUtil.publishEvent(new OkAuthUserLoginEvent(loginResponse));
        return loginResponse;
    }

    @Override
    public OkAuthUserDo getById(Long userId) {
        Assert.notNull(userId, "userId不能为空");
        return okAuthUserMapper.selectOneById(userId);
    }
}
