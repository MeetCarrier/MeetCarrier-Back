package com.kslj.mannam.domain.test.service;

import com.kslj.mannam.domain.test.dto.TestRequestDto;
import com.kslj.mannam.domain.test.dto.TestResponseDto;
import com.kslj.mannam.domain.test.entity.Test;
import com.kslj.mannam.domain.test.repository.TestRepository;
import com.kslj.mannam.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TestService {

    private final TestRepository testRepository;

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
        return savedTest.getId();
    }

    // 테스트 결과 목록
    @Transactional
    public List<TestResponseDto> getTestByUserId(User user) {
        List<Test> tests = testRepository.findTop10ByUserOrderByCreatedAtDesc(user);

        return tests.stream()
                .map(TestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 테스트 결과 삭제
    @Transactional
    public long deleteTestByTestId(Long testId) {
        Optional<Test> targetTest = testRepository.findById(testId);

        if (targetTest.isEmpty()) {
            throw new RuntimeException("테스트 데이터를 찾을 수 없습니다. testId = " + testId);
        } else {
            testRepository.deleteById(testId);
            return testId;
        }
    }
}
