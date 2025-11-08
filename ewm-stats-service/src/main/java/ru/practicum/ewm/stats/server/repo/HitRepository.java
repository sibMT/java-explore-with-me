package ru.practicum.ewm.stats.server.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.server.model.Hit;
import ru.practicum.ewm.stats.server.model.ViewAgg;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query("""
        SELECT new ru.practicum.ewm.stats.server.model.ViewAgg(h.app, h.uri, COUNT(h))
        FROM Hit h
        WHERE h.timestamp BETWEEN :start AND :end
          AND (:urisEmpty = true OR h.uri IN :uris)
        GROUP BY h.app, h.uri
        ORDER BY COUNT(h) DESC
    """)
    List<ViewAgg> aggregateTotal(@Param("start")LocalDateTime start,
                                 @Param("end")LocalDateTime end,
                                 @Param("urisEmpty") boolean urisEmpty,
                                 @Param("uris") List<String> uris);

    @Query("""
        SELECT new ru.practicum.ewm.stats.server.model.ViewAgg(h.app, h.uri, COUNT(DISTINCT h.ip))
        FROM Hit h
        WHERE h.timestamp BETWEEN :start AND :end
          AND (:urisEmpty = true OR h.uri IN :uris)
        GROUP BY h.app, h.uri
        ORDER BY COUNT(DISTINCT h.ip) DESC
    """)
    List<ViewAgg> aggregateUnique(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  @Param("urisEmpty") boolean urisEmpty,
                                  @Param("uris")List<String> uris);
}
