package com.sqe.service;

import com.sqe.entity.StudentInfo;
import com.sqe.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class StudentServiceTest {

    private final SysUserMapper sysUserMapper = mock(SysUserMapper.class);
    private final RoleService roleService = mock(RoleService.class);
    private final StudentParentRelationService parentRelationService = mock(StudentParentRelationService.class);
    private final EvaluationService evaluationService = mock(EvaluationService.class);
    private final StudentService service = spy(new StudentService());

    @Test
    void deleteStudentRemovesRelatedEvaluations() {
        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setId(7L);
        doReturn(studentInfo).when(service).getById(7L);
        doReturn(true).when(service).removeById(7L);
        ReflectionTestUtils.setField(service, "sysUserMapper", sysUserMapper);
        ReflectionTestUtils.setField(service, "roleService", roleService);
        ReflectionTestUtils.setField(service, "parentRelationService", parentRelationService);
        ReflectionTestUtils.setField(service, "evaluationService", evaluationService);

        service.deleteStudent(7L);

        verify(evaluationService).deleteByStudentId(7L);
        verify(parentRelationService).deleteByStudentId(7L);
    }
}
