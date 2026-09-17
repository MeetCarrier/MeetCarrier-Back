package com.kslj.mannam.domain.match.service;

import com.kslj.mannam.domain.match.dto.MatchFilterRequestDto;
import com.kslj.mannam.domain.match.dto.MatchFilterResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Slf4j
@Service
public class HttpMatchScoringGateway implements MatchScoringGateway {
    private final RestClient restClient;
    private final String scoringPath;
    private final int maxAttempts;

    public HttpMatchScoringGateway(
            @Qualifier("matchScoringRestClient") RestClient restClient,
            @Value("${matching.scoring.path:/api/matches/score}") String scoringPath,
            @Value("${matching.scoring.max-attempts:3}") int maxAttempts) {
        if (maxAttempts < 1) throw new IllegalArgumentException("matching.scoring.max-attempts must be at least 1");
        this.restClient = restClient;
        this.scoringPath = scoringPath;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public MatchFilterResponseDto score(MatchFilterRequestDto request) {
        if (request.getRequestId() == null) request.setRequestId(UUID.randomUUID());
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                MatchFilterResponseDto response = restClient.post()
                        .uri(scoringPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(MatchFilterResponseDto.class);
                if (response == null) {
                    throw new ResourceAccessException("매칭 점수 응답 본문이 비어 있습니다.");
                }
                if (response.getRequestId() != null && !request.getRequestId().equals(response.getRequestId())) {
                    throw new IllegalStateException("매칭 점수 응답 requestId가 요청과 일치하지 않습니다.");
                }
                return response;
            } catch (HttpClientErrorException e) {
                throw new IllegalStateException("매칭 점수 요청이 거부되었습니다. status=" + e.getStatusCode(), e);
            } catch (HttpServerErrorException | ResourceAccessException e) {
                lastFailure = new IllegalStateException("매칭 점수 계산 실패 (attempt=" + attempt + ")", e);
                log.warn("Matching score HTTP request failed. requestId={}, attempt={}",
                        request.getRequestId(), attempt, e);
            } catch (RestClientException e) {
                throw new IllegalStateException("매칭 점수 응답을 처리할 수 없습니다.", e);
            }
        }

        throw lastFailure == null ? new IllegalStateException("매칭 점수 계산 실패") : lastFailure;
    }
}
