package com.sqe.service;

import com.sqe.mapper.AcademicEvaluationMapper;
import com.sqe.mapper.ArtEvaluationMapper;
import com.sqe.mapper.ComprehensiveEvaluationMapper;
import com.sqe.mapper.MoralEvaluationMapper;
import com.sqe.mapper.PhysicalEvaluationMapper;
import com.sqe.mapper.PracticeEvaluationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EvaluationServiceTest {

    private final MoralEvaluationMapper moralMapper = mock(MoralEvaluationMapper.class);
    private final AcademicEvaluationMapper academicMapper = mock(AcademicEvaluationMapper.class);
    private final PhysicalEvaluationMapper physicalMapper = mock(PhysicalEvaluationMapper.class);
    private final ArtEvaluationMapper artMapper = mock(ArtEvaluationMapper.class);
    private final PracticeEvaluationMapper practiceMapper = mock(PracticeEvaluationMapper.class);
    private final ComprehensiveEvaluationMapper comprehensiveMapper = mock(ComprehensiveEvaluationMapper.class);
    private final EvaluationService service = new EvaluationService();

    @Test
    void deleteByStudentIdRemovesAllStudentEvaluations() {
        ReflectionTestUtils.setField(service, "moralMapper", moralMapper);
        ReflectionTestUtils.setField(service, "academicMapper", academicMapper);
        ReflectionTestUtils.setField(service, "physicalMapper", physicalMapper);
        ReflectionTestUtils.setField(service, "artMapper", artMapper);
        ReflectionTestUtils.setField(service, "practiceMapper", practiceMapper);
        ReflectionTestUtils.setField(service, "comprehensiveMapper", comprehensiveMapper);

        service.deleteByStudentId(7L);

        verify(moralMapper).delete(any());
        verify(academicMapper).delete(any());
        verify(physicalMapper).delete(any());
        verify(artMapper).delete(any());
        verify(practiceMapper).delete(any());
        verify(comprehensiveMapper).delete(any());
    }
}
