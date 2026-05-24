package com.sqe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

import java.util.List;

/**
 * 学生信息服务
 */
@Service
public class StudentService extends ServiceImpl<StudentInfoMapper, StudentInfo> {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;
    @Autowired
    private StudentParentRelationService parentRelationService;
    @Autowired
    private EvaluationService evaluationService;

    /* 分页查询学生 */
    public IPage<StudentInfo> pageList(int current, int size, String keyword, Long classId) {
        return baseMapper.selectStudentPage(new Page<>(current, size), keyword, classId);
    }

    /* 新增学生，同时创建登录账号并绑定userId */
    @Transactional(rollbackFor = Exception.class)
    public boolean addStudent(StudentInfo studentInfo) {
        Long studentCount = baseMapper.selectCount(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getStudentNo, studentInfo.getStudentNo()));
        if (studentCount > 0) {
            return false;
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, studentInfo.getStudentNo()));
        if (user == null) {
            user = new SysUser();
            user.setUsername(studentInfo.getStudentNo());
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole("student");
            user.setStatus(1);
        } else if (!"student".equals(roleService.getPrimaryRole(user.getId(), user.getRole()))) {
            return false;
        }
        fillUserFromStudent(user, studentInfo);

        if (user.getId() == null) {
            sysUserMapper.insert(user);
        } else {
            sysUserMapper.updateById(user);
        }
        roleService.syncSingleRole(user.getId(), "student");
        studentInfo.setUserId(user.getId());
        boolean saved = this.save(studentInfo);
        if (saved) {
            parentRelationService.syncPrimaryParent(studentInfo.getId(), studentInfo.getParentId());
        }
        return saved;
    }

    /* 修改学生，同时同步用户基础资料 */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStudent(StudentInfo studentInfo) {
        StudentInfo oldStudent = this.getById(studentInfo.getId());
        if (oldStudent == null) {
            return false;
        }
        Long duplicateStudentNo = baseMapper.selectCount(
                new LambdaQueryWrapper<StudentInfo>()
                        .eq(StudentInfo::getStudentNo, studentInfo.getStudentNo())
                        .ne(StudentInfo::getId, studentInfo.getId()));
        if (duplicateStudentNo > 0) {
            return false;
        }
        studentInfo.setUserId(oldStudent.getUserId());
        if (studentInfo.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(studentInfo.getUserId());
            if (user != null) {
                SysUser duplicateUser = sysUserMapper.selectOne(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getUsername, studentInfo.getStudentNo())
                                .ne(SysUser::getId, user.getId()));
                if (duplicateUser != null) {
                    return false;
                }
                user.setUsername(studentInfo.getStudentNo());
                fillUserFromStudent(user, studentInfo);
                sysUserMapper.updateById(user);
            }
        }
        boolean updated = this.updateById(studentInfo);
        if (updated) {
            parentRelationService.syncPrimaryParent(studentInfo.getId(), studentInfo.getParentId());
        }
        return updated;
    }

    /* 删除学生，同时删除对应学生账号 */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteStudent(Long id) {
        StudentInfo studentInfo = this.getById(id);
        if (studentInfo == null) {
            return false;
        }
        boolean removed = this.removeById(id);
        if (removed) {
            evaluationService.deleteByStudentId(id);
            parentRelationService.deleteByStudentId(id);
        }
        if (removed && studentInfo.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(studentInfo.getUserId());
            if (user != null && "student".equals(roleService.getPrimaryRole(user.getId(), user.getRole()))) {
                sysUserMapper.deleteById(user.getId());
            }
        }
        return removed;
    }

    /* 根据用户ID获取学生信息 */
    public StudentInfo getByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, userId));
    }

    /* 根据家长ID获取关联学生 */
    public List<StudentInfo> getByParentId(Long parentId) {
        List<Long> studentIds = parentRelationService.getStudentIdsByParentId(parentId);
        LambdaQueryWrapper<StudentInfo> wrapper = new LambdaQueryWrapper<>();
        if (!studentIds.isEmpty()) {
            wrapper.in(StudentInfo::getId, studentIds).or();
        }
        wrapper.eq(StudentInfo::getParentId, parentId);
        return this.list(wrapper);
    }

    /* 根据班级ID获取学生列表 */
    public List<StudentInfo> getByClassId(Long classId) {
        return this.list(new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getClassId, classId));
    }

    private void fillUserFromStudent(SysUser user, StudentInfo studentInfo) {
        if (StringUtils.hasText(studentInfo.getRealName())) {
            user.setRealName(studentInfo.getRealName());
        } else if (!StringUtils.hasText(user.getRealName())) {
            user.setRealName(studentInfo.getStudentNo());
        }
        user.setPhone(studentInfo.getPhone());
        user.setEmail(studentInfo.getEmail());
        user.setGender(studentInfo.getGender());
    }
}
