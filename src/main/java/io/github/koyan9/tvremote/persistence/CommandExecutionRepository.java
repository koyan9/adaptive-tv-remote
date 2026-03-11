package io.github.koyan9.tvremote.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandExecutionRepository extends JpaRepository<CommandExecutionEntity, String> {

    List<CommandExecutionEntity> findTop12ByOrderByExecutedAtDesc();
}
