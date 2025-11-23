package ru.practicum.ewm.main.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequest {

    private List<Long> events;

    @Size(min = 1, max = 50)
    private String title;

    private Boolean pinned;

}
