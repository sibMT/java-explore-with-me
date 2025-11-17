package ru.practicum.ewm.stats.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.server.service.StatsService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private static final DateTimeFormatter DTF =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHit(@Valid @RequestBody EndpointHitDto dto) {
        service.save(dto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> stats(@RequestParam String start,
                                    @RequestParam String end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(defaultValue = "false") boolean unique) {

        String s = URLDecoder.decode(start, StandardCharsets.UTF_8);
        String e = URLDecoder.decode(end, StandardCharsets.UTF_8);
        LocalDateTime startDt = LocalDateTime.parse(s, DTF);
        LocalDateTime endDt = LocalDateTime.parse(e, DTF);

        if (endDt.isBefore(startDt)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "end must be after start"
            );
        }
        return service.stats(s, e, uris, unique);
    }
}
