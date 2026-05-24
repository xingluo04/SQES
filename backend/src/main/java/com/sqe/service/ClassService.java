package com.sqe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqe.entity.ClassInfo;
import com.sqe.entity.StudentInfo;
import com.sqe.entity.SysUser;
import com.sqe.mapper.ClassInfoMapper;
import com.sqe.mapper.StudentInfoMapper;
import com.sqe.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 班级管理服务
 */
@Service
public class ClassService extends ServiceImpl<ClassInfoMapper, ClassInfo> {

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private StudentInfoMapper studentInfoMapper;
    @Autowired
    private RoleService roleService;

    /* 分页查询班级 */
    public Page<ClassInfo> pageList(int current, int size, String keyword) {
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ClassInfo::getClassName, keyword);
        }
        wrapper.orderByDesc(ClassInfo::getCreateTime);
        Page<ClassInfo> page = this.page(new Page<>(current, size), wrapper);
        /* 填充教师姓名和学生数量 */
        page.getRecords().forEach(this::fillExtraInfo);
        return page;
    }

    /* 获取所有班级列表 */
    public List<ClassInfo> listAll() {
        List<ClassInfo> list = this.list();
        list.forEach(this::fillExtraInfo);
        return list;
    }

    public boolean saveClass(ClassInfo classInfo) {
        if (!isTeacher(classInfo.getTeacherId())) {
            return false;
        }
        return this.save(classInfo);
    }

    public boolean updateClass(ClassInfo classInfo) {
        if (!isTeacher(classInfo.getTeacherId())) {
            return false;
        }
        return this.updateById(classInfo);
    }

    /* 填充额外信息 */
    private void fillExtraInfo(ClassInfo classInfo) {
        if (classInfo.getTeacherId() != null) {
            SysUser teacher = userMapper.selectById(classInfo.getTeacherId());
            if (teacher != null) {
                classInfo.setTeacherName(teacher.getRealName());
            }
        }
        Long count = studentInfoMapper.selectCount(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getClassId, classInfo.getId()));
        classInfo.setStudentCount(count.intValue());
    }

    private boolean isTeacher(Long teacherId) {
        if (teacherId == null) {
            return true;
        }
        SysUser teacher = userMapper.selectById(teacherId);
        return teacher != null && "teacher".equals(roleService.getPrimaryRole(teacher.getId(), teacher.getRole()));
    }
}
