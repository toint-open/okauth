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

import cn.toint.okauth.permission.constant.OkAuthPermissionConstant;
import cn.toint.oktool.spring.boot.model.BaseDo;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色关联权限
 *
 * @author Toint
 * @date 2025/6/29
 */
@Table(value = "role_mtm_permission", dataSource = OkAuthPermissionConstant.DATA_SOURCE)
@EqualsAndHashCode(callSuper = true)
@Data
public class RoleMtmPermissionDo extends BaseDo {
    private Long roleId;
    private Long permissionId;
}
