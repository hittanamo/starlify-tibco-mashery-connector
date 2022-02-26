package com.entiros.starlify.connector.config;

import com.entiros.starlify.connector.verifier.CustomHostnameVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.Collections;

@Configuration
public class Config {
    @Bean
    @Primary
    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    @Bean
    @Primary
    public RestTemplate createRestTemplate() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        return restTemplateBuilder.interceptors(
                (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
                    request.getHeaders().setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    return execution.execute(request, body);
                }
        ).build();
    }

    @Bean("masheryRestTemplate")
    public RestTemplate createMasheryRestTemplate() throws Exception {
        System.setProperty("jdk.tls.allowUnsafeServerCertChange", "true");
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new CustomHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);

        RestTemplateBuilder builder = restTemplateBuilder.interceptors(
                (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
                    request.getHeaders().setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    return execution.execute(request, body);
                }
        ).requestFactory(() -> requestFactory);
        RestTemplate restTemplate = builder.build();
        return restTemplate;
    }
}