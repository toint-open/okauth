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

package cn.toint.okauth.server.openclient.service;

import cn.toint.okauth.server.openclient.model.OpenClientDo;
import cn.toint.okauth.server.openclient.model.OpenClientSaveRequest;
import cn.toint.okauth.server.openclient.model.OpenClientUpdateRequest;

import java.util.List;

/**
 * 开放应用
 *
 * @author Toint
 * @date 2025-06-27
 */
public interface OkAuthOpenClientService {
    /**
     * 加载开放应用
     */
    void load(OpenClientDo openClientDo);

    /**
     * 写在开放应用
     */
    void unload(String clientId);

    /**
     * 添加开放应用
     */
    OpenClientDo save(OpenClientSaveRequest req);

    /**
     * 修改开放应用
     */
    void update(OpenClientUpdateRequest res);

    /**
     * 开放应用是否存在
     */
    boolean existById(Long id);

    /**
     * 列表查询所有开放应用
     */
    List<OpenClientDo> listAll();

    /**
     * 查询客户端
     *
     * @param id 客户端ID
     * @return 客户端对象
     */
    OpenClientDo getById(Long id);
}
