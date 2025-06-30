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

import cn.toint.okauth.permission.model.OkAuthRoleDo;
import cn.toint.okauth.permission.service.OkAuthPermissionService;
import cn.toint.okauth.server.user.service.OkAuthAccessPermissionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Toint
 * @date 2025/6/30
 */
@Service
public class OkAuthAccessPermissionServiceImpl implements OkAuthAccessPermissionService {
    @Resource
    private OkAuthPermissionService permissionService;

    /**
     * 返回指定账号id所拥有的权限码集合
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        Set<String> codes = permissionService.listPermissionCode(userId);
        return new ArrayList<>(codes);
    }

    /**
     * 返回指定账号id所拥有的角色标识集合
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<OkAuthRoleDo> roleDos = permissionService.listRole(userId);
        return roleDos.stream().map(OkAuthRoleDo::getCode).toList();
    }
}
