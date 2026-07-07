package com.vivekai.studio.usage.repository;

import com.vivekai.studio.usage.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, UUID> {

    List<UsageLog> findByUserId(UUID userId);
}
