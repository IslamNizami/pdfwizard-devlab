package com.islamnizami.pdfwizarddevlab.repository;

import com.islamnizami.pdfwizarddevlab.model.entity.PdfAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PdfAuditLogRepository extends JpaRepository<PdfAuditLog, UUID> {
}
