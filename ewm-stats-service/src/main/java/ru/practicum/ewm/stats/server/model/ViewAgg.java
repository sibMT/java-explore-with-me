package ru.practicum.ewm.stats.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ViewAgg {
    private String app;
    private String uri;
    private long hits;
}
