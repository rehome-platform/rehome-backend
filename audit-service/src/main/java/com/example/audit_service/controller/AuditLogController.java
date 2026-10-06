package com.example.audit_service.controller;

import com.example.audit_service.dto.req.AuditLogRequest;
import com.example.audit_service.entity.AuditLog;
import com.example.audit_service.repository.AuditLogRepository;
import com.example.audit_service.service.AuditLogConsumerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "API ghi nhận và truy vấn audit log")
@SecurityRequirement(name = "X-API-KEY")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogConsumerService auditLogService;

    @GetMapping
    @Operation(summary = "Lấy danh sách audit log", description = "Hỗ trợ phân trang và tìm kiếm theo action/entity/username")
    public Page<AuditLog> getLogs(
            @Parameter(description = "Số trang, bắt đầu từ 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Từ khóa tìm kiếm trong action/entity/username") @RequestParam(defaultValue = "") String search
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "receivedAt"));

        if (search == null || search.isBlank()) {
            return auditLogRepository.findAll(pageable);
        }
        return auditLogRepository
                .findByActionContainingIgnoreCaseOrEntityContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                        search, search, search, pageable);
    }

    @PostMapping
    @Operation(summary = "Ghi nhận một audit log mới", description = "Được gọi bởi các service khác (vd: PRM) sau khi thực hiện hành động cần audit")
    public ResponseEntity<Void> createLog(@RequestBody AuditLogRequest request) {
        auditLogService.createLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}