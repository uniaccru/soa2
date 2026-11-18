package org.example.killer.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Duration;

/** Настройка HTTP-клиента к сервису 1: URL, таймауты и доверие к его самоподписанному сертификату. */
@Configuration
@PropertySource("classpath:application.properties")
public class Service1ClientConfig {

    @Bean
    public RestClient service1RestClient(
            XmlMapper xmlMapper,
            @Value("${service1.url}") String baseUrl,
            @Value("${service1.truststore.path:}") String trustStorePath,
            @Value("${service1.truststore.password:changeit}") String trustStorePassword,
            @Value("${service1.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${service1.read-timeout-ms:10000}") int readTimeoutMs) throws Exception {

        HttpClient.Builder http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(connectTimeoutMs));
        if (!trustStorePath.isBlank()) {
            http.sslContext(sslContext(trustStorePath, trustStorePassword));
        }

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(http.build());
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .messageConverters(converters -> {
                    converters.clear();
                    converters.add(new MappingJackson2XmlHttpMessageConverter(xmlMapper));
                })
                .defaultHeader("Accept", MediaType.APPLICATION_XML_VALUE)
                .build();
    }

    private SSLContext sslContext(String path, String password) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(Path.of(path))) {
            trustStore.load(in, password.toCharArray());
        }
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(trustStore);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, factory.getTrustManagers(), null);
        return context;
    }
}
