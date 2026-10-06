package com.example.audit_service.service;

import com.example.audit_service.dto.req.AuditLogRequest;

public interface AuditLogConsumerService {
    public void createLog(AuditLogRequest auditLogRequest);
}
