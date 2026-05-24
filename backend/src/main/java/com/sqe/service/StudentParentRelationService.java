package com.sqe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqe.entity.StudentParentRelation;
import com.sqe.mapper.StudentParentRelationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生家长关系服务
 */
@Service
public class StudentParentRelationService {

    private final StudentParentRelationMapper mapper;

    @Autowired
    public StudentParentRelationService(StudentParentRelationMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncPrimaryParent(Long studentId, Long parentUserId) {
        if (studentId == null) {
            return;
        }
        mapper.delete(new LambdaQueryWrapper<StudentParentRelation>()
                .eq(StudentParentRelation::getStudentId, studentId)
                .eq(StudentParentRelation::getIsPrimaryContact, 1));
        if (parentUserId == null) {
            return;
        }
        StudentParentRelation relation = new StudentParentRelation();
        relation.setStudentId(studentId);
        relation.setParentUserId(parentUserId);
        relation.setRelationType("guardian");
        relation.setIsPrimaryContact(1);
        mapper.insert(relation);
    }

    public List<Long> getStudentIdsByParentId(Long parentUserId) {
        if (parentUserId == null) {
            return Collections.emptyList();
        }
        return mapper.selectList(new LambdaQueryWrapper<StudentParentRelation>()
                        .eq(StudentParentRelation::getParentUserId, parentUserId))
                .stream()
                .map(StudentParentRelation::getStudentId)
                .collect(Collectors.toList());
    }

    public void deleteByStudentId(Long studentId) {
        mapper.delete(new LambdaQueryWrapper<StudentParentRelation>()
                .eq(StudentParentRelation::getStudentId, studentId));
    }

    public void deleteByParentId(Long parentUserId) {
        mapper.delete(new LambdaQueryWrapper<StudentParentRelation>()
                .eq(StudentParentRelation::getParentUserId, parentUserId));
    }
}
