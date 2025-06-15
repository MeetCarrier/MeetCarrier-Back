package com.kslj.mannam.domain.test.service;

import com.kslj.mannam.domain.test.dto.TestRequestDto;
import com.kslj.mannam.domain.test.dto.TestResponseDto;
import com.kslj.mannam.domain.test.entity.Test;
import com.kslj.mannam.domain.test.repository.TestRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.ActionType;
import com.kslj.mannam.domain.user.service.UserActionLogService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TestService {

    private final TestRepository testRepository;
    private final UserActionLogService userActionLogService;

    // 테스트 결과 등록
    @Transactional
    public long createTest(TestRequestDto requestDto, User user) {
        Test newTest = Test.builder()
                .depressionScore(requestDto.getDepressionScore())
                .relationshipScore(requestDto.getRelationshipScore())
                .efficacyScore(requestDto.getEfficacyScore())
                .user(user)
                .build();

        Test savedTest = testRepository.save(newTest);
        userActionLogService.logUserAction(user, ActionType.TEST_DONE);
        return savedTest.getId();
    }

    // 테스트 결과 목록
    @Transactional(readOnly = true)
    public List<TestResponseDto> getTestByUserId(User user) {
        List<Test> tests = testRepository.findTop10ByUserOrderByCreatedAtDesc(user);

        return tests.stream()
                .map(TestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 테스트 결과 삭제
    @Transactional
    public void deleteTestByTestId(Long testId, User user) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("테스트 데이터를 찾을 수 없습니다. testId = " + testId));

        if (!test.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 테스트 결과를 삭제할 권한이 없습니다.");
        }

        testRepository.delete(test);
    }
}
