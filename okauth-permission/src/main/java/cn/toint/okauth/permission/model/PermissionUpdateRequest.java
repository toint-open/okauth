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

package cn.toint.okauth.permission.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionUpdateRequest {
    /**
     * 权限ID
     */
    @NotNull
    private Long id;

    /**
     * 父菜单ID
     */
    @NotNull
    private Long parentId;

    /**
     * 菜单类型
     */
    @NotNull
    private Integer type;

    /**
     * 权限名称
     */
    @NotNull
    private String name;

    /**
     * 访问路径
     */
    private String path;

    /**
     * 前端组件
     */
    private String component;

    /**
     * 组件名称
     */
    private String componentName;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 排序
     */
    @NotNull
    private Integer sort;

    /**
     * 权限标识
     */
    private String code;
}
