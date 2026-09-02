package com.zest.productapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncAuditService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncAuditService.class);

    @Async("taskExecutor")
    public void logAudit(String action, String details, String performedBy) {
        logger.info("AUDIT LOG -> Action: {}, Details: {}, PerformedBy: {}", action, details, performedBy);
    }
}
