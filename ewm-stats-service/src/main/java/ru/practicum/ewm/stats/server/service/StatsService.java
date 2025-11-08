package ru.practicum.ewm.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.server.model.Hit;
import ru.practicum.ewm.stats.server.model.ViewAgg;
import ru.practicum.ewm.stats.server.repo.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final HitRepository repo;
    private static final DateTimeFormatter DFM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void save(EndpointHitDto dto) {
        Hit h = new Hit();
        h.setApp(dto.getApp());
        h.setUri(dto.getUri());
        h.setIp(dto.getIp());
        h.setTimestamp(LocalDateTime.parse(dto.getTimestamp(), DFM));
        repo.save(h);
    }

    public List<ViewStatsDto> stats(String start, String end, List<String> uris, boolean unique) {
        LocalDateTime s = LocalDateTime.parse(start, DFM);
        LocalDateTime e = LocalDateTime.parse(end, DFM);
        boolean empty = (uris == null || uris.isEmpty());

        List<ViewAgg> rows = unique
                ? repo.aggregateUnique(s, e, empty, empty ? List.of("") : uris)
                : repo.aggregateTotal(s, e, empty, empty ? List.of("") : uris);

        return rows.stream()
                .map(a -> new ViewStatsDto(a.getApp(), a.getUri(), a.getHits()))
                .toList();
    }
}
