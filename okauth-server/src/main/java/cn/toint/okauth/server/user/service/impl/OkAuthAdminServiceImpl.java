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

import cn.dev33.satoken.stp.StpUtil;
import cn.toint.okauth.server.exception.OkAuthUserNotExistException;
import cn.toint.okauth.server.exception.OkAuthUserPasswordException;
import cn.toint.okauth.server.user.mapper.OkAuthUserMapper;
import cn.toint.okauth.server.user.model.OkAuthAdminLoginRequest;
import cn.toint.okauth.server.user.model.OkAuthAdminLoginVo;
import cn.toint.okauth.server.user.model.OkAuthUserDo;
import cn.toint.okauth.server.user.service.OkAuthAdminService;
import cn.toint.okauth.server.user.service.OkAuthUserEncryptService;
import cn.toint.oktool.util.Assert;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Toint
 * @date 2025/6/30
 */
@Service
@Slf4j
public class OkAuthAdminServiceImpl implements OkAuthAdminService {

    @Resource
    private OkAuthUserEncryptService encryptService;

    @Resource
    private OkAuthUserMapper userMapper;

    @Override
    public OkAuthAdminLoginVo login(OkAuthAdminLoginRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 账号密码
        String username = request.getUsername();
        String password = request.getPassword();

        // 校验用户是否存在
        OkAuthUserDo userDo = userMapper.selectOneByQuery(
                QueryWrapper.create().eq(OkAuthUserDo::getUsername, username));
        if (userDo == null) {
            throw new OkAuthUserNotExistException(StrUtil.format("账号[{}]不存在", username));
        }

        // 校验密码是否一致
        password = encryptService.encrypt(password);
        if (!Objects.equals(userDo.getPassword(), password)) {
            throw new OkAuthUserPasswordException("密码错误");
        }

        // 登录
        Long userId = userDo.getId();
        StpUtil.login(userId);
//        if (!StpUtil.isLogin(userId)) {
//            StpUtil.login(userId);
//        }


        // res
        OkAuthAdminLoginVo.Admin admin = new OkAuthAdminLoginVo.Admin();
        BeanUtil.copyProperties(userDo, admin);

        OkAuthAdminLoginVo.Token token = new OkAuthAdminLoginVo.Token();
        BeanUtil.copyProperties(StpUtil.getTokenInfo(), token);

        OkAuthAdminLoginVo adminLoginVo = new OkAuthAdminLoginVo();
        adminLoginVo.setAdmin(admin);
        adminLoginVo.setToken(token);
        return adminLoginVo;
    }
}
