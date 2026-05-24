package com.sqe.service;

import com.sqe.entity.SysRole;
import com.sqe.entity.SysUserRole;
import com.sqe.mapper.SysRoleMapper;
import com.sqe.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
    private final RoleService roleService = new RoleService(roleMapper, userRoleMapper);

    @Test
    void resolvePrimaryRoleUsesPriorityOrder() {
        Assertions.assertEquals("admin",
                roleService.resolvePrimaryRole(Arrays.asList("parent", "admin", "teacher"), "parent"));
        Assertions.assertEquals("teacher",
                roleService.resolvePrimaryRole(Arrays.asList("parent", "teacher"), "parent"));
        Assertions.assertEquals("student",
                roleService.resolvePrimaryRole(Collections.emptyList(), "student"));
    }

    @Test
    void syncSingleRoleReplacesExistingRelationsAndCreatesMissingRole() {
        when(roleMapper.selectOne(any())).thenReturn(null);
        when(roleMapper.insert(any(SysRole.class))).thenAnswer(invocation -> {
            SysRole role = invocation.getArgument(0);
            role.setId(8L);
            return 1;
        });

        roleService.syncSingleRole(3L, "teacher");

        verify(userRoleMapper).delete(any());
        ArgumentCaptor<SysRole> roleCaptor = ArgumentCaptor.forClass(SysRole.class);
        verify(roleMapper).insert(roleCaptor.capture());
        Assertions.assertEquals("teacher", roleCaptor.getValue().getRoleCode());

        ArgumentCaptor<SysUserRole> relationCaptor = ArgumentCaptor.forClass(SysUserRole.class);
        verify(userRoleMapper).insert(relationCaptor.capture());
        Assertions.assertEquals(3L, relationCaptor.getValue().getUserId());
        Assertions.assertEquals(8L, relationCaptor.getValue().getRoleId());
    }
}
