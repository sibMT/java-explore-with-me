package ru.practicum.ewm.stats.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        } catch (Exception ex) {
            System.err.println("Не удалось отправить статистику: " + ex.getMessage());
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
            ResponseEntity<ViewStatsDto[]> resp =
                    restTemplate.getForEntity(uri, ViewStatsDto[].class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(resp.getBody());
        } catch (Exception ex) {
            System.err.println("Не удалось получить статистику: " + ex.getMessage());
            return Collections.emptyList();
        }
    }

}
