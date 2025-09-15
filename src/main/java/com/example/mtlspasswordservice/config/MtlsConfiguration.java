package com.example.mtlspasswordservice.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import javax.net.ssl.HostnameVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import java.time.Duration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class MtlsConfiguration {

    @Value("${mtls.client.keystore.path:}")
    private String clientKeystorePath;

    @Value("${mtls.client.keystore.password:}")
    private String clientKeystorePassword;

    @Value("${mtls.client.keystore.type:PKCS12}")
    private String clientKeystoreType;

    @Value("${mtls.truststore.path:}")
    private String truststorePath;

    @Value("${mtls.truststore.password:}")
    private String truststorePassword;

    @Value("${mtls.truststore.type:JKS}")
    private String truststoreType;

    @Value("${mtls.verify-hostname:true}")
    private boolean verifyHostname;

    @Bean
    public SSLContext sslContext() throws Exception {
        SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();

        // Configure client certificate (mTLS)
        if (clientKeystorePath != null && !clientKeystorePath.isEmpty()) {
            KeyStore clientKeyStore = KeyStore.getInstance(clientKeystoreType);
            try (InputStream keystoreInputStream = new FileInputStream(clientKeystorePath)) {
                clientKeyStore.load(keystoreInputStream, clientKeystorePassword.toCharArray());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, clientKeystorePassword.toCharArray());
            sslContextBuilder.loadKeyMaterial(clientKeyStore, clientKeystorePassword.toCharArray());
        }

        // Configure trust store
        if (truststorePath != null && !truststorePath.isEmpty()) {
            KeyStore trustStore = KeyStore.getInstance(truststoreType);
            try (InputStream truststoreInputStream = new FileInputStream(truststorePath)) {
                trustStore.load(truststoreInputStream, truststorePassword.toCharArray());
            }

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            sslContextBuilder.loadTrustMaterial(trustStore, null);
        } else {
            // For development/testing - use insecure trust manager
            // In production, always use a proper trust store
            sslContextBuilder.loadTrustMaterial(null, new TrustAllStrategy());
        }

        return sslContextBuilder.build();
    }

    @Bean
    public CloseableHttpClient httpClient(SSLContext sslContext) {
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{"TLSv1.2", "TLSv1.3"},
                null,
                verifyHostname ? null : (hostname, session) -> true
        );

        return HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setSSLSocketFactory(sslSocketFactory)
                                .setMaxConnTotal(100)
                                .setMaxConnPerRoute(20)
                                .build()
                )
                .build();
    }

    @Bean
    public RestTemplate restTemplate(CloseableHttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(Duration.ofSeconds(30));
        requestFactory.setConnectionRequestTimeout(Duration.ofSeconds(30));

        return new RestTemplate(requestFactory);
    }
}