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

import cn.toint.okauth.permission.mapper.DeptMapper;
import cn.toint.okauth.permission.model.DeptDo;
import cn.toint.okauth.permission.model.DeptTreeResponse;
import cn.toint.okauth.permission.service.DeptService;
import cn.toint.oktool.util.ExceptionUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class DeptServiceImpl implements DeptService {

    @Resource
    private DeptMapper deptMapper;

    @Override
    public List<DeptTreeResponse> listDeptTree() {
        // 查询所有部门
        List<DeptDo> deptDos = deptMapper.selectAll();
        if (deptDos.isEmpty()) {
            return new ArrayList<>();
        }

        // 全部对象转换为vo
        List<DeptTreeResponse> deptVos = new ArrayList<>();
        for (DeptDo deptDo : deptDos) {
            DeptTreeResponse deptVo = new DeptTreeResponse();
            BeanUtil.copyProperties(deptDo, deptVo);
            deptVo.setChildren(new ArrayList<>());
            deptVos.add(deptVo);
        }

        // 创建Map方便查找
        Map<Long, DeptTreeResponse> deptVoMap = new HashMap<>();
        for (DeptTreeResponse deptVo : deptVos) {
            deptVoMap.put(deptVo.getId(), deptVo);
        }

        // 检测循环依赖
        for (DeptTreeResponse deptVo : deptVos) {
            Set<Long> path = new HashSet<>();
            Long currentId = deptVo.getId();

            // 沿着parentId链向上查找，直到找到根节点或发现循环
            while (currentId != null && deptVoMap.containsKey(currentId)) {
                if (!path.add(currentId)) {  // add返回false说明已存在，即发现循环
                    throw ExceptionUtil.wrapRuntimeException("部门[{}]存在循环依赖", currentId);
                }
                DeptTreeResponse current = deptVoMap.get(currentId);
                currentId = (current.getParentId() == 0) ? null : current.getParentId();
            }
        }

        // 构建树关系
        List<DeptTreeResponse> roots = new ArrayList<>();
        for (DeptTreeResponse deptVo : deptVos) {
            if (deptVo.getParentId() == 0) {
                // 找到根节点
                roots.add(deptVo);
            } else {
                // 找到父节点，把当前节点加入父节点的children
                DeptTreeResponse parent = deptVoMap.get(deptVo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(deptVo);
                }
            }
        }

        return roots;
    }

    /**
     * 递归收集子部门
     * 如果部门的父部门已在集合中，则将该部门也加入集合
     */
    private void findAndAddChildDepts(DeptTreeResponse deptTree, Set<Long> allDeptIds) {
        if (allDeptIds.contains(deptTree.getParentId())) {
            allDeptIds.add(deptTree.getId());
        }

        // 递归处理子部门
        if (CollUtil.isNotEmpty(deptTree.getChildren())) {
            for (DeptTreeResponse child : deptTree.getChildren()) {
                findAndAddChildDepts(child, allDeptIds);
            }
        }
    }
}
