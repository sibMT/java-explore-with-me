package ru.practicum.ewm.main.event.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLocationDto {

    private Double lat;

    private Double lon;
}
