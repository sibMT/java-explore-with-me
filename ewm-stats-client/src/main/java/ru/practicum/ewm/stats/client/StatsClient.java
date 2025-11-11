package ru.practicum.ewm.stats.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class StatsClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats.base-url:http://localhost:9090}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void postHit(String app, String uri, String ip, LocalDateTime ts) {
        EndpointHitDto dto = new EndpointHitDto(app, uri, ip, ts.format(DTF));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHitDto> entity = new HttpEntity<>(dto, headers);
        String url = baseUrl + "/hit";

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.debug("Статистика успешно отправлена: app={}, uri={}, ip={}, ts={}", app, uri, ip, ts);
        } catch (HttpStatusCodeException ex) {
            log.error("Ошибка при отправке статистики (POST /hit). Код ответа: {}, тело: {}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new StatsClientException("Не удалось отправить статистику: " + ex.getStatusCode(), ex);
        } catch (ResourceAccessException ex) {
            log.error("Не удалось подключиться к серверу статистики по адресу {}", url, ex);
            throw new StatsClientException("Не удалось подключиться к серверу статистики", ex);
        } catch (Exception ex) {
            log.error("Неожиданная ошибка при отправке статистики", ex);
            throw new StatsClientException("Неожиданная ошибка при отправке статистики", ex);
        }
    }


    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, boolean unique) {
        String s = start.format(DTF);
        String e = end.format(DTF);

        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/stats")
                .queryParam("start", s)
                .queryParam("end", e)
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            for (String u : uris) b.queryParam("uris", u);
        }

        URI uri = b.build(true)
                .encode(StandardCharsets.UTF_8)
                .toUri();

        try {
            ResponseEntity<ViewStatsDto[]> resp = restTemplate.getForEntity(uri, ViewStatsDto[].class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.error("Запрос статистики (GET {}) вернул ошибку: {}", uri, resp.getStatusCode());
                throw new StatsClientException("Ошибка при получении статистики: " + resp.getStatusCode(), null);
            }

            List<ViewStatsDto> result = Arrays.asList(resp.getBody());
            log.debug("Получена статистика: {} записей", result.size());
            return result;

        } catch (HttpStatusCodeException ex) {
            log.error("Ошибка при запросе статистики (GET {}). Код ответа: {}, тело: {}",
                    uri, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new StatsClientException("Не удалось получить статистику: " + ex.getStatusCode(), ex);
        } catch (ResourceAccessException ex) {
            log.error("Ошибка подключения к серверу статистики при запросе {}", uri, ex);
            throw new StatsClientException("Не удалось подключиться к серверу статистики", ex);
        } catch (Exception ex) {
            log.error("Неожиданная ошибка при получении статистики", ex);
            throw new StatsClientException("Неожиданная ошибка при получении статистики", ex);
        }
    }

}
