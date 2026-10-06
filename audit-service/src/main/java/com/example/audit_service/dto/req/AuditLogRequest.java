package com.example.audit_service.dto.req;

import lombok.Data;

@Data
public class AuditLogRequest {
    private String action;
    private String entity;
    private String entityId;
    private String detail;
    private String username;
    private String userId;
    private String sourceService;
}