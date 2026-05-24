package com.sqe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqe.entity.StudentInfo;
import com.sqe.entity.SysUser;
import com.sqe.mapper.StudentInfoMapper;
import com.sqe.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户管理服务
 */
@Service
public class UserService extends ServiceImpl<SysUserMapper, SysUser> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StudentInfoMapper studentInfoMapper;
    @Autowired
    private RoleService roleService;
    @Autowired
    private StudentParentRelationService parentRelationService;

    /* 分页查询用户 */
    public Page<SysUser> pageList(int current, int size, String keyword, String role) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = this.page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(this::fillRoles);
        return page;
    }

    /* 新增用户 */
    public boolean addUser(SysUser user) {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            return false;
        }
        /* 设置默认密码 */
        if (StringUtils.hasText(user.getRawPassword())) {
            user.setPassword(passwordEncoder.encode(user.getRawPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        user.setStatus(1);
        boolean saved = this.save(user);
        if (saved) {
            roleService.syncSingleRole(user.getId(), user.getRole());
            fillRoles(user);
        }
        return saved;
    }

    /* 修改用户 */
    public boolean updateUser(SysUser user) {
        if (StringUtils.hasText(user.getRawPassword())) {
            user.setPassword(passwordEncoder.encode(user.getRawPassword()));
        }
        boolean updated = this.updateById(user);
        if (updated && StringUtils.hasText(user.getRole())) {
            roleService.syncSingleRole(user.getId(), user.getRole());
        }
        return updated;
    }

    public SysUser getUserInfo(Long userId) {
        SysUser user = this.getById(userId);
        if (user != null) {
            fillRoles(user);
        }
        return user;
    }

    /* 重置密码 */
    public boolean resetPassword(Long userId, String newPassword) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }

    /* 删除用户，并处理角色关联数据 */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            return false;
        }
        String primaryRole = roleService.getPrimaryRole(user.getId(), user.getRole());
        if ("student".equals(primaryRole)) {
            StudentInfo student = studentInfoMapper.selectOne(
                    new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, id));
            if (student != null) {
                parentRelationService.deleteByStudentId(student.getId());
            }
            studentInfoMapper.delete(new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, id));
        } else if ("parent".equals(primaryRole)) {
            parentRelationService.deleteByParentId(id);
            studentInfoMapper.update(null,
                    new LambdaUpdateWrapper<StudentInfo>()
                            .set(StudentInfo::getParentId, null)
                            .eq(StudentInfo::getParentId, id));
        }
        roleService.deleteUserRoles(id);
        return this.removeById(id);
    }

    private void fillRoles(SysUser user) {
        user.setRoles(roleService.getRoleCodesByUserId(user.getId()));
        user.setRole(roleService.resolvePrimaryRole(user.getRoles(), user.getRole()));
    }
}
