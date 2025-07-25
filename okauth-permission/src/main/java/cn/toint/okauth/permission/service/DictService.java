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

package cn.toint.okauth.permission.service;

import cn.toint.okauth.permission.model.DictCreateRequest;
import cn.toint.okauth.permission.model.DictDo;
import cn.toint.okauth.permission.model.DictUpdateRequest;
import cn.toint.oktool.spring.boot.model.PageRequest;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

public interface DictService {
    /**
     * 查询所有列表
     * 内置缓存
     */
    List<DictDo> listAll();

    /**
     * 根据type查询列表
     */
    List<DictDo> listByType(String type);

    /**
     * 根据type和key查询
     */
    DictDo getByTypeAndKey(String type, String key);

    void create(DictCreateRequest request);

    void update(DictUpdateRequest request);

    void delete(List<Long> ids);

    DictDo getById(Long id);

    Page<DictDo> page(PageRequest pageRequest);
}
