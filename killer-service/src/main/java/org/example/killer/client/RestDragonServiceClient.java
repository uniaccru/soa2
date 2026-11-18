package org.example.killer.client;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.killer.dto.DragonPageDto;
import org.example.killer.dto.ErrorDto;
import org.example.killer.dto.TeamDto;
import org.example.killer.exception.InvalidDataException;
import org.example.killer.exception.UpstreamBadResponseException;
import org.example.killer.exception.UpstreamTimeoutException;
import org.example.killer.exception.UpstreamUnavailableException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.function.Supplier;

@Component
public class RestDragonServiceClient implements DragonServiceClient {

    private static final Log LOG = LogFactory.getLog(RestDragonServiceClient.class);

    private final RestClient client;
    private final XmlMapper xmlMapper;

    public RestDragonServiceClient(RestClient service1RestClient, XmlMapper xmlMapper) {
        this.client = service1RestClient;
        this.xmlMapper = xmlMapper;
    }

    @Override
    public DragonPageDto findPageSortedByCaveDepth(int page, int size) {
        return call(() -> client.get()
                .uri(uri -> uri.path("/dragons")
                        .queryParam("sortBy", "caveDepth")
                        .queryParam("sortOrder", "asc")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(DragonPageDto.class));
    }

    @Override
    public TeamDto createTeam(TeamDto team) {
        return call(() -> client.post()
                .uri("/teams")
                .contentType(MediaType.APPLICATION_XML)
                .body(team)
                .retrieve()
                .body(TeamDto.class));
    }

    private <T> T call(Supplier<T> request) {
        try {
            T body = request.get();
            if (body == null) {
                throw new UpstreamBadResponseException("Dragon service returned an empty response");
            }
            return body;
        } catch (RestClientResponseException e) {
            throw fromResponse(e);
        } catch (ResourceAccessException e) {
            throw fromIoError(e);
        } catch (RestClientException e) {
            LOG.warn("Cannot read response of dragon service", e);
            throw new UpstreamBadResponseException("Dragon service returned an unreadable response");
        }
    }

    private RuntimeException fromResponse(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String message = upstreamMessage(e);
        return switch (status) {
            case 400 -> new InvalidDataException(message);
            case 503 -> new UpstreamUnavailableException(message);
            case 504 -> new UpstreamTimeoutException(message);
            default -> new UpstreamBadResponseException(
                    "Dragon service responded with status " + status + ": " + message);
        };
    }

    private RuntimeException fromIoError(ResourceAccessException e) {
        LOG.warn("Dragon service call failed", e);
        for (Throwable t = e; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof HttpConnectTimeoutException) {
                return new UpstreamUnavailableException("Dragon service is unavailable");
            }
            if (t instanceof HttpTimeoutException || t instanceof SocketTimeoutException) {
                return new UpstreamTimeoutException("Dragon service did not respond in time");
            }
        }
        return new UpstreamUnavailableException("Dragon service is unavailable");
    }

    private String upstreamMessage(RestClientResponseException e) {
        try {
            ErrorDto error = xmlMapper.readValue(e.getResponseBodyAsByteArray(), ErrorDto.class);
            if (error.getMessage() != null) {
                return error.getMessage();
            }
        } catch (Exception ignored) {
            // тело не в формате <error> — используем общий текст
        }
        return "no details";
    }
}
