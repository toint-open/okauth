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

package cn.toint.okauth.server.openclient.service.impl;

import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import cn.toint.okauth.server.oauth2.manager.OkAuthOauth2Manager;
import cn.toint.okauth.server.openclient.mapper.OkAuthOpenClientMapper;
import cn.toint.okauth.server.openclient.model.OkAuthOpenClientDo;
import cn.toint.okauth.server.openclient.model.OkAuthOpenClientSaveRequest;
import cn.toint.okauth.server.openclient.model.OkAuthOpenClientStatusEnum;
import cn.toint.okauth.server.openclient.model.OkAuthOpenClientUpdateRequest;
import cn.toint.okauth.server.openclient.service.OkAuthOpenClientService;
import cn.toint.oktool.util.Assert;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.SqlUtil;
import com.mybatisflex.core.util.UpdateEntity;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OkAuthOpenClientServiceImpl implements OkAuthOpenClientService {

    @Resource
    private OkAuthOpenClientMapper openClientMapper;

    @Resource
    private OkAuthOauth2Manager oauth2Manager;

    /**
     * 初始化Bean, 加载所有开放应用到sa-token
     */
    @PostConstruct
    private void loadAll() {
        // 获取所有状态正常的应用加载进satoken
        QueryWrapper queryWrapper = QueryWrapper.create().eq(OkAuthOpenClientDo::getStatus, 1);
        List<OkAuthOpenClientDo> openClientDos = openClientMapper.selectListByQuery(queryWrapper);
        oauth2Manager.getServerConfig().getClients().clear();
        openClientDos.forEach(this::load);
        log.info("开放应用模块初始化成功, 加载{}个开放应用", openClientDos.size());
    }

    @Override
    public void load(OkAuthOpenClientDo openClientDo) {
        checkLoadOpenClient(openClientDo);
        SaClientModel saClientModel = new SaClientModel();
        saClientModel.setClientId(String.valueOf(openClientDo.getId()));
        saClientModel.setClientSecret(openClientDo.getSecret());
        saClientModel.setSubjectId(openClientDo.getSubjectId());
        saClientModel.setAllowGrantTypes(openClientDo.getAllowGrantTypes());
        saClientModel.setAllowRedirectUris(openClientDo.getAllowRedirectUris()); // 允许授权回调的url, 可使用通配符*
        oauth2Manager.getServerConfig().addClient(saClientModel);
        log.info("加载开放应用[{}]成功", openClientDo.getId());
    }

    /**
     * 校验加载开放应用信息
     */
    private void checkLoadOpenClient(OkAuthOpenClientDo openClientDo) {
        Assert.notNull(openClientDo, "应用不能为空");
        Assert.notBlank(openClientDo.getSecret(), "应用密钥不能为空");
        Assert.notBlank(openClientDo.getSubjectId(), "主体ID不能为空");
        Assert.notNull(openClientDo.getId(), "应用ID不能为空");
        Assert.notEmpty(openClientDo.getAllowGrantTypes(), "授权方式不能为空");
    }

    @Override
    public OkAuthOpenClientDo save(OkAuthOpenClientSaveRequest req) {
        Assert.notNull(req, "请求参数不能为空");
        Assert.validate(req);

        OkAuthOpenClientDo openClientDo = BeanUtil.copyProperties(req, new OkAuthOpenClientDo());
        openClientDo.init();
        openClientDo.setStatus(OkAuthOpenClientStatusEnum.ENABLE.getValue());
        int inserted = openClientMapper.insert(openClientDo);
        Assert.isTrue(SqlUtil.toBool(inserted), "开放应用保存失败");

        // 加载至satoken
        load(openClientDo);

        return openClientDo;
    }

    @Override
    public void update(OkAuthOpenClientUpdateRequest res) {
        Assert.notNull(res, "请求参数不能为空");
        Assert.validate(res);

        Long id = res.getId();
        boolean existById = existById(id);
        Assert.isTrue(existById, "开放应用[{}]不存在", id);

        OkAuthOpenClientDo openClientDo = UpdateEntity.of(OkAuthOpenClientDo.class, id);
        BeanUtil.copyProperties(res, openClientDo);
        openClientDo.freshUpdateTime();
        int updated = openClientMapper.update(openClientDo);
        Assert.isTrue(SqlUtil.toBool(updated), "开放应用更新失败");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean existById(Long id) {
        Assert.notNull(id, "查询的客户端ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(OkAuthOpenClientDo::getId)
                .eq(OkAuthOpenClientDo::getId, id);
        OkAuthOpenClientDo res = openClientMapper.selectOneByQuery(queryWrapper);
        return res != null;
    }

    @Override
    public List<OkAuthOpenClientDo> listAll() {
        return openClientMapper.selectAll();
    }

    @Override
    public OkAuthOpenClientDo getById(Long id) {
        Assert.notNull(id, "查询的客户端ID不能为空");
        return openClientMapper.selectOneById(id);
    }
}
