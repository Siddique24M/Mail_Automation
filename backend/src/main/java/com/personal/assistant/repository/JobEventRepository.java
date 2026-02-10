package com.personal.assistant.repository;

import com.personal.assistant.entity.JobEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobEventRepository extends JpaRepository<JobEvent, Long> {
    List<JobEvent> findByEventDateAfterOrderByEventDateAsc(LocalDateTime date);

    void deleteByCreatedAtBefore(LocalDateTime expiryDate);
}
