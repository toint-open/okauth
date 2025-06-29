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

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单权限
 *
 * @author Toint
 * @date 2025/6/29
 */
@Table("okauth_permission")
@EqualsAndHashCode(callSuper = true)
@Data
public class OkAuthPermissionDo extends BaseDo {
    /**
     * 父菜单ID
     */
    @Column
    private Long parentId;

    /**
     * 菜单类型
     */
    @Column
    private Integer type;

    /**
     * 权限名称
     */
    @Column
    private String name;

    /**
     * 访问路径
     */
    @Column
    private String path;

    /**
     * 前端组件
     */
    @Column
    private String component;

    /**
     * 组件名称
     */
    @Column
    private String componentName;

    /**
     * 菜单图标
     */
    @Column
    private String icon;

    /**
     * 排序
     */
    @Column
    private Integer order;

    /**
     * 权限标识
     */
    @Column
    private String code;
}
