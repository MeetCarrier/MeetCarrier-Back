package com.kslj.mannam.test;

import com.kslj.mannam.TestUtils;
import com.kslj.mannam.domain.test.dto.TestRequestDto;
import com.kslj.mannam.domain.test.dto.TestResponseDto;
import com.kslj.mannam.domain.test.service.TestService;
import com.kslj.mannam.domain.user.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class TestServiceTest {

    @Autowired
    private TestService testService;

    @Autowired
    private TestUtils testUtils;

    // 테스트 결과 데이터 생성 메서드
    private TestRequestDto createTestRequest(int depressionScore, int relationshipScore) {
        return TestRequestDto.builder()
                .depressionScore(depressionScore)
                .relationshipScore(relationshipScore)
                .build();
    }

    // 새로운 테스트 결과 추가 및 조회 테스트
    @Test
    public void testCreateTest() {
        // given
        User testUser = testUtils.createAndGetTestUser();
        TestRequestDto testRequest = createTestRequest(15, 80);

        // when
        long testId = testService.createTest(testRequest, testUser);
        List<TestResponseDto> testByUserId = testService.getTestByUserId(testUser);

        // then
        System.out.println("testRequest.getDepressionScore() = " + testRequest.getDepressionScore());
        System.out.println("testByUserId.get(0).getDepressionScore() = " + testByUserId.get(0).getDepressionScore());
        Assertions.assertThat(testByUserId.get(0).getDepressionScore()).isEqualTo(testRequest.getDepressionScore());

    }

    // 테스트 결과 조회 개수 확인 (최대 10개)
    @Test
    public void testGetAllTests() {
        // given
        User testUser = testUtils.createAndGetTestUser();
        for (int i = 0; i < 15; i++) {
            TestRequestDto testRequest = createTestRequest(i, 100 - i);
            testService.createTest(testRequest, testUser);
        }

        // when
        List<TestResponseDto> testByUserId = testService.getTestByUserId(testUser);

        // then
        for (TestResponseDto test : testByUserId) {
            System.out.println("score = " + test.getDepressionScore() + ", " + test.getRelationshipScore());
        }
        Assertions.assertThat(testByUserId.size()).isEqualTo(10);
    }

    // 테스트 결과 삭제 테스트
    @Test
    public void testDeleteTest() {
        // given
        User testUser = testUtils.createAndGetTestUser();
        TestRequestDto testRequest1 = createTestRequest(15, 80);
        TestRequestDto testRequest2 = createTestRequest(15, 80);
        TestRequestDto testRequest3 = createTestRequest(15, 80);

        long testId1 = testService.createTest(testRequest1, testUser);
        long testId2 = testService.createTest(testRequest2, testUser);
        long testId3 = testService.createTest(testRequest3, testUser);

        // when
        testService.deleteTestByTestId(testId3);
        List<TestResponseDto> testByUserId = testService.getTestByUserId(testUser);

        // then
        Assertions.assertThat(testByUserId.size()).isEqualTo(2);
    }
}
