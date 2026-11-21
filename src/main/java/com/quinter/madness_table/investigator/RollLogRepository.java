package com.quinter.madness_table.investigator;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RollLogRepository extends JpaRepository<RollLog, Long> {

    @EntityGraph(attributePaths = "rolledBy")
    List<RollLog> findTop50ByInvestigatorIdOrderByCreatedAtDesc(Long investigatorId);
}
