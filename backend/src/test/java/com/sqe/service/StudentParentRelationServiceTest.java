package com.sqe.service;

import com.sqe.entity.StudentParentRelation;
import com.sqe.mapper.StudentParentRelationMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentParentRelationServiceTest {

    private final StudentParentRelationMapper mapper = mock(StudentParentRelationMapper.class);
    private final StudentParentRelationService service = new StudentParentRelationService(mapper);

    @Test
    void syncPrimaryParentReplacesPrimaryContactAndKeepsLegacyRelationShape() {
        service.syncPrimaryParent(11L, 22L);

        verify(mapper).delete(any());
        ArgumentCaptor<StudentParentRelation> captor = ArgumentCaptor.forClass(StudentParentRelation.class);
        verify(mapper).insert(captor.capture());

        StudentParentRelation relation = captor.getValue();
        Assertions.assertEquals(11L, relation.getStudentId());
        Assertions.assertEquals(22L, relation.getParentUserId());
        Assertions.assertEquals("guardian", relation.getRelationType());
        Assertions.assertEquals(1, relation.getIsPrimaryContact());
    }

    @Test
    void syncPrimaryParentWithNullClearsExistingPrimaryContactOnly() {
        service.syncPrimaryParent(11L, null);

        verify(mapper).delete(any());
        verify(mapper, never()).insert(any());
    }
}
