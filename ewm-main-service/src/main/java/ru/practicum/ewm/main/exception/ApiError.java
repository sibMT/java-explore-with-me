package ru.practicum.ewm.main.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private static final DateTimeFormatter DTF =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private List<String> errors;
    private String message;
    private String reason;
    private String status;
    private String timestamp;

    public static ApiError of(HttpStatus httpStatus,
                              String reason,
                              String message,
                              List<String> errors) {
        return ApiError.builder()
                .status(httpStatus.name())
                .reason(reason)
                .message(message)
                .errors(errors == null ? Collections.emptyList() : errors)
                .timestamp(LocalDateTime.now().format(DTF))
                .build();
    }
}
