package ru.practicum.ewm.stats.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "hits")
@Getter
@Setter
@NoArgsConstructor
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String app;
    @Column(nullable = false)
    private String uri;
    @Column(nullable = false, length = 45)
    private String ip;
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
