package com.kslj.mannam.match;

import com.kslj.mannam.domain.match.dto.MatchFilterRequestDto;
import com.kslj.mannam.domain.match.dto.MatchFilterResponseDto;
import com.kslj.mannam.domain.match.service.HttpMatchScoringGateway;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpMatchScoringGatewayTest {

    @Test
    void postsScoringRequestAndReturnsResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpMatchScoringGateway gateway = gateway(builder, 1);
        UUID requestId = UUID.randomUUID();

        server.expect(requestTo("http://scoring/api/matches/score"))
                .andExpect(method(POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"requestId\":\"" + requestId + "\"}"))
                .andRespond(withSuccess("{\"requestId\":\"" + requestId + "\",\"filterResults\":[]}",
                        MediaType.APPLICATION_JSON));

        MatchFilterResponseDto response = gateway.score(MatchFilterRequestDto.builder().requestId(requestId).build());

        assertThat(response.getRequestId()).isEqualTo(requestId);
        assertThat(response.getFilterResults()).isEmpty();
        server.verify();
    }

    @Test
    void retriesServerFailuresUpToConfiguredAttemptCount() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpMatchScoringGateway gateway = gateway(builder, 3);
        UUID requestId = UUID.randomUUID();

        server.expect(ExpectedCount.twice(), requestTo("http://scoring/api/matches/score"))
                .andRespond(withServerError());
        server.expect(requestTo("http://scoring/api/matches/score"))
                .andRespond(withSuccess("{\"requestId\":\"" + requestId + "\",\"filterResults\":[]}",
                        MediaType.APPLICATION_JSON));

        MatchFilterResponseDto response = gateway.score(MatchFilterRequestDto.builder().requestId(requestId).build());

        assertThat(response.getRequestId()).isEqualTo(requestId);
        server.verify();
    }

    @Test
    void retriesConnectionFailuresAndFailsAfterLastAttempt() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpMatchScoringGateway gateway = gateway(builder, 3);

        server.expect(ExpectedCount.times(3), requestTo("http://scoring/api/matches/score"))
                .andRespond(request -> { throw new ResourceAccessException("timeout"); });

        assertThatThrownBy(() -> gateway.score(MatchFilterRequestDto.builder().build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attempt=3");
        server.verify();
    }

    @Test
    void doesNotRetryClientErrors() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpMatchScoringGateway gateway = gateway(builder, 3);

        server.expect(ExpectedCount.once(), requestTo("http://scoring/api/matches/score"))
                .andRespond(withBadRequest());

        assertThatThrownBy(() -> gateway.score(MatchFilterRequestDto.builder().build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("거부");
        server.verify();
    }

    private HttpMatchScoringGateway gateway(RestClient.Builder builder, int maxAttempts) {
        return new HttpMatchScoringGateway(builder.baseUrl("http://scoring").build(),
                "/api/matches/score", maxAttempts);
    }
}
