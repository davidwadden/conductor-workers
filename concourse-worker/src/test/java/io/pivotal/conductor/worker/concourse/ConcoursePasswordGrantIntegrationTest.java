package io.pivotal.conductor.worker.concourse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@ContextConfiguration(classes = {
    ConcoursePasswordGrantIntegrationTest.ContextConfiguration.class,
})
@ExtendWith(SpringExtension.class)
class ConcoursePasswordGrantIntegrationTest {

    private static final Logger logger =
        LoggerFactory.getLogger(ConcoursePasswordGrantIntegrationTest.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ConcourseProperties properties;
    @Autowired
    private OAuth2RestOperations concourseOAuth2RestOperations;

    WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void passwordGrant() throws IOException {
        logger.info("Setting base URL: {}", wireMockServer.baseUrl());
        properties.setApiHost(wireMockServer.baseUrl());

        Map<String, Object> responseDto = Map.of(
            "access_token", "some-access-token",
            "token_type", "Bearer",
            "expiry", "2019-05-29T00:11:26Z"
        );
        String responseBody = objectMapper.writeValueAsString(responseDto);

        wireMockServer.stubFor(post(urlEqualTo("/sky/token"))
            .willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(responseBody)));

        wireMockServer.stubFor(get(urlEqualTo("/some-api-endpoint"))
            .willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody("{\"key\":\"value\"}")));

        RequestEntity<Void> requestEntity = RequestEntity
            .get(URI.create(wireMockServer.baseUrl() + "/some-api-endpoint"))
            .build();

        concourseOAuth2RestOperations.exchange(requestEntity, Void.class);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>() {{
            add("grant_type", "password");
            add("username", "some-username");
            add("password", "some-password");
            add("scope", "openid profile email federated:id groups");
        }};
        TestFormHttpMessageConverter messageConverter = new TestFormHttpMessageConverter();
        String requestBody = messageConverter.serializeFormData(formData, Charset.forName("UTF-8"));

        wireMockServer
            .verify(postRequestedFor(urlEqualTo("/sky/token"))
                .withHeader(HttpHeaders.CONTENT_TYPE,
                    matching(String.format("^%s;.*$", MediaType.APPLICATION_FORM_URLENCODED_VALUE)))
                .withBasicAuth(new BasicCredentials("fly", "Zmx5"))
                .withRequestBody(equalTo(requestBody))
            );
        wireMockServer
            .verify(getRequestedFor(urlEqualTo("/some-api-endpoint"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer some-access-token")));
    }

    @Import(ConcourseWorkerConfig.class)
    @Configuration
    public static class ContextConfiguration {

        @Bean
        public ConcourseProperties ConcourseProperties() {
            ConcourseProperties properties = new ConcourseProperties();

            properties.setApiHost("http://localhost:8888");
            properties.setTeamName("some-team-name");
            properties.setUsername("some-username");
            properties.setPassword("some-password");
            properties.setShouldExposePipeline("false");

            return properties;
        }
    }

    public static class TestFormHttpMessageConverter extends FormHttpMessageConverter {

        public String serializeFormData(MultiValueMap<String, Object> formData, Charset charset) {
            return serializeForm(formData, charset);
        }
    }
}
