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
        return this.page(new Page<>(current, size), wrapper);
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
        return this.save(user);
    }

    /* 修改用户 */
    public boolean updateUser(SysUser user) {
        if (StringUtils.hasText(user.getRawPassword())) {
            user.setPassword(passwordEncoder.encode(user.getRawPassword()));
        }
        return this.updateById(user);
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
        if ("student".equals(user.getRole())) {
            studentInfoMapper.delete(new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, id));
        } else if ("parent".equals(user.getRole())) {
            studentInfoMapper.update(null,
                    new LambdaUpdateWrapper<StudentInfo>()
                            .set(StudentInfo::getParentId, null)
                            .eq(StudentInfo::getParentId, id));
        }
        return this.removeById(id);
    }
}
