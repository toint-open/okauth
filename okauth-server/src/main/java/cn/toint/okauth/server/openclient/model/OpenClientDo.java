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

package cn.toint.okauth.server.openclient.model;

import cn.toint.okauth.server.constant.OkAuthConstant;
import cn.toint.okauth.server.model.BaseDo;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 应用
 *
 * @author Toint
 * @date 2025/6/27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "open_client", dataSource = OkAuthConstant.DATA_SOURCE)
public class OpenClientDo extends BaseDo {
    /**
     * 应用名称
     */
    @Column
    private String name;

    /**
     * 主体ID
     */
    @Column
    private String subjectId;

    /**
     * 应用秘钥
     */
    @Column
    private String secret;

    /**
     * 授权回调地址
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private List<String> allowRedirectUris;

    /**
     * 状态
     */
    @Column
    private Integer status;
}
