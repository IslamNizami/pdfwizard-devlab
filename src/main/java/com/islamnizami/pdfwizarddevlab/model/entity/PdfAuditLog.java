package com.islamnizami.pdfwizarddevlab.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pdf_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType action;

    private String performedBy;

    @CreationTimestamp
    private LocalDateTime timestamp;

    public enum ActionType {
        CREATE,
        DOWNLOAD,
        DELETE,
        MERGE,
        SPLIT,
        EMAIL_SENT
    }
}
