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

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限树
 *
 * @author Toint
 * @date 2025/6/29
 */
@Data
public class PermissionTreeResponse {
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单类型
     */
    private Integer type;

    /**
     * 权限名称
     */
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
    private Integer order;

    /**
     * 权限标识
     */
    private String code;

    private List<PermissionTreeResponse> children;
}
