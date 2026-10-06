package com.example.audit_service.repository;

import com.example.audit_service.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByActionContainingIgnoreCaseOrEntityContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String action, String entity, String username, Pageable pageable);
}