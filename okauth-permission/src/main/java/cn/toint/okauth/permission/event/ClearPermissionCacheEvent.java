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

package cn.toint.okauth.permission.event;

import lombok.Getter;
import org.dromara.hutool.extra.spring.SpringUtil;
import org.springframework.context.ApplicationEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Toint
 * @date 2025/7/20
 */
public class ClearPermissionCacheEvent extends ApplicationEvent {

    public ClearPermissionCacheEvent(Detail source) {
        super(source);
    }

    @Override
    public Detail getSource() {
        return (Detail) super.getSource();
    }

    public static ClearPermissionCacheEvent of() {
        return new ClearPermissionCacheEvent(new Detail());
    }

    public ClearPermissionCacheEvent adduserId(Long userId) {
        getSource().adduserId(userId);
        return this;
    }

    public ClearPermissionCacheEvent addRoleId(Long roleId) {
        getSource().addRoleId(roleId);
        return this;
    }

    public ClearPermissionCacheEvent addPermissionId(Long permissionId) {
        getSource().addPermissionId(permissionId);
        return this;
    }

    public void publishEvent() {
        SpringUtil.publishEvent(this);
    }

    @Getter
    public static class Detail {
        private final Set<Long> userIds = new HashSet<>();
        private final Set<Long> roleIds = new HashSet<>();
        private final Set<Long> permissionIds = new HashSet<>();

        public Detail adduserId(Long userId) {
            if (userId != null) {
                userIds.add(userId);
            }
            return this;
        }

        public Detail addRoleId(Long roleId) {
            if (roleId != null) {
                roleIds.add(roleId);
            }
            return this;
        }

        public Detail addPermissionId(Long permissionId) {
            if (permissionId != null) {
                permissionIds.add(permissionId);
            }
            return this;
        }
    }
}
