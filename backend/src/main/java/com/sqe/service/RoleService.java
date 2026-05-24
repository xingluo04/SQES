package com.sqe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqe.entity.SysRole;
import com.sqe.entity.SysUserRole;
import com.sqe.mapper.SysRoleMapper;
import com.sqe.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户角色服务
 */
@Service
public class RoleService {

    private static final List<String> ROLE_PRIORITY = Arrays.asList("admin", "teacher", "student", "parent");
    private static final Map<String, String> ROLE_NAMES = new HashMap<>();

    static {
        ROLE_NAMES.put("admin", "管理员");
        ROLE_NAMES.put("teacher", "教师");
        ROLE_NAMES.put("student", "学生");
        ROLE_NAMES.put("parent", "家长");
    }

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Autowired
    public RoleService(SysRoleMapper roleMapper, SysUserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    public List<String> getRoleCodesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getStatus, 1))
                .stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }

    public String resolvePrimaryRole(List<String> roles, String fallbackRole) {
        if (roles != null) {
            for (String role : ROLE_PRIORITY) {
                if (roles.contains(role)) {
                    return role;
                }
            }
            if (!roles.isEmpty()) {
                return roles.get(0);
            }
        }
        return StringUtils.hasText(fallbackRole) ? fallbackRole : "student";
    }

    public String getPrimaryRole(Long userId, String fallbackRole) {
        return resolvePrimaryRole(getRoleCodesByUserId(userId), fallbackRole);
    }

    public boolean hasRole(Long userId, String roleCode) {
        return getRoleCodesByUserId(userId).contains(roleCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncSingleRole(Long userId, String roleCode) {
        if (userId == null || !StringUtils.hasText(roleCode)) {
            return;
        }
        SysRole role = findOrCreateRole(roleCode);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRoleMapper.insert(userRole);
    }

    public void deleteUserRoles(Long userId) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
    }

    private SysRole findOrCreateRole(String roleCode) {
        SysRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
        if (role != null) {
            return role;
        }
        role = new SysRole();
        role.setRoleCode(roleCode);
        role.setRoleName(ROLE_NAMES.getOrDefault(roleCode, roleCode));
        role.setStatus(1);
        roleMapper.insert(role);
        return role;
    }
}
