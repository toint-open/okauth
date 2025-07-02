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

package cn.toint.okauth.server.user.model;

import cn.toint.okauth.server.constant.OkAuthConstant;
import cn.toint.okauth.server.model.BaseDo;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户
 *
 * @author Toint
 * @date 2025/6/30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "okauth_user", dataSource = OkAuthConstant.DATA_SOURCE)
public class OkAuthUserDo extends BaseDo {
    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String name;

    @Column
    private String phone;
}
