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

package cn.toint.okauth.permission.service.impl;

import cn.toint.okauth.permission.mapper.DictMapper;
import cn.toint.okauth.permission.model.DictCreateRequest;
import cn.toint.okauth.permission.model.DictDo;
import cn.toint.okauth.permission.model.DictUpdateRequest;
import cn.toint.okauth.permission.properties.OkAuthPermissionProperties;
import cn.toint.okauth.permission.service.DictService;
import cn.toint.oktool.spring.boot.cache.Cache;
import cn.toint.oktool.spring.boot.model.PageRequest;
import cn.toint.oktool.util.Assert;
import cn.toint.oktool.util.JacksonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DictServiceImpl implements DictService {

    @Resource
    private DictMapper dictMapper;

    @Resource
    private Cache cache;

    @Resource
    private OkAuthPermissionProperties okAuthPermissionProperties;

    private final String dictCacheKey = "dict";

    @Override
    public List<DictDo> listAll() {
        // 检查缓存
        String cacheValue = cache.get(dictCacheKey);
        if (StringUtils.isNotBlank(cacheValue)) {
            return JacksonUtil.readValue(cacheValue, new TypeReference<>() {
            });
        }

        // 查询所有字典
        List<DictDo> dictDos = dictMapper.selectListByQuery(QueryWrapper.create()
                .orderBy(DictDo::getType, true));

        // 缓存
        cacheValue = JacksonUtil.writeValueAsString(dictDos);
        cache.put(dictCacheKey, cacheValue, okAuthPermissionProperties.getCacheTimeout());
        return dictDos;
    }

    @Override
    public List<DictDo> listByType(String type) {
        Assert.notBlank(type, "type must not be null");
        return dictMapper.selectListByQuery(QueryWrapper.create()
                .eq(DictDo::getType, type));
    }

    @Override
    public DictDo getByTypeAndKey(String type, String key) {
        Assert.notBlank(type, "type must not be null");
        Assert.notBlank(key, "key must not be null");
        return dictMapper.selectOneByQuery(QueryWrapper.create()
                .eq(DictDo::getType, type)
                .eq(DictDo::getKey, key));
    }

    @Override
    public void create(DictCreateRequest request) {
        String type = request.getType();
        String key = request.getKey();

        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 不能重复
        DictDo dictDo = dictMapper.selectOneByQuery(QueryWrapper.create()
                .eq(DictDo::getType, type)
                .eq(DictDo::getKey, key));
        Assert.isNull(dictDo, "字典已存在");

        // 数据落库
        dictDo = new DictDo();
        dictDo.init();
        BeanUtils.copyProperties(request, dictDo);
        dictMapper.insert(dictDo, false);

        // 清除缓存
        clearCache();
    }

    @Override
    public void update(DictUpdateRequest request) {
        String type = request.getType();
        String key = request.getKey();
        Long id = request.getId();

        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 不能重复
        DictDo dictDo = dictMapper.selectOneByQuery(QueryWrapper.create()
                .eq(DictDo::getType, type)
                .eq(DictDo::getKey, key)
                .ne(DictDo::getId, id));
        Assert.isNull(dictDo, "字典已存在");

        // 数据落库
        dictDo = dictMapper.selectOneById(id);
        Assert.notNull(dictDo, "字典不存在");
        dictDo.freshUpdateTime();
        BeanUtils.copyProperties(request, dictDo);
        dictMapper.update(dictDo, false);

        // 清除缓存
        clearCache();
    }

    @Override
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;
        ids.removeIf(Objects::isNull);
        if (CollUtil.isEmpty(ids)) return;
        dictMapper.deleteBatchByIds(ids);
        // 清除缓存
        clearCache();
    }

    @Override
    public DictDo getById(Long id) {
        Assert.notNull(id, "字典ID不能为空");
        return dictMapper.selectOneById(id);
    }

    @Override
    public Page<DictDo> page(PageRequest pageRequest) {
        Assert.notNull(pageRequest, "请求参数不能为空");
        Assert.validate(pageRequest);

        Long pageNumber = pageRequest.getPageNumber();
        Long pageSize = pageRequest.getPageSize();
        return dictMapper.paginate(pageNumber, pageSize, QueryWrapper.create()
                .orderBy(DictDo::getType, true));
    }

    private void clearCache() {
        cache.delete(dictCacheKey);
    }
}
