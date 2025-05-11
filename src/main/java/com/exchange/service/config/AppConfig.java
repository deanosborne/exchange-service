package com.exchange.service.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Value("${http.client.connect-timeout:500}")
    private int connectTimeout;

    @Value("${http.client.socket-timeout:700}")
    private int socketTimeout;

    @Value("${http.client.request-timeout:500}")
    private int requestTimeout;

    @Value("${http.client.max-total-connections:100}")
    private int maxTotalConnections;

    @Value("${http.client.max-per-route-connections:20}")
    private int maxPerRouteConnections;

    /**
     * Creates RestTemplate with connection pooling.
     *
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(maxPerRouteConnections);

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(requestTimeout)
                .build();

        final CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setBufferRequestBody(false);

        return new RestTemplate(requestFactory);
    }

}
