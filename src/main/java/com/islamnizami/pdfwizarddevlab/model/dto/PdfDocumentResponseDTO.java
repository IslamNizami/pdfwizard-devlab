package com.islamnizami.pdfwizarddevlab.model.dto;

import com.islamnizami.pdfwizarddevlab.model.entity.PdfDocument;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfDocumentResponseDTO implements Serializable {

    private UUID id;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String createdBy;
    private LocalDateTime createdAt;

    public static PdfDocumentResponseDTO fromEntity(PdfDocument doc) {
        return PdfDocumentResponseDTO.builder()
                .id(doc.getId())
                .fileName(doc.getFileName())
                .fileSize(doc.getFileSize())
                .contentType(doc.getContentType())
                .createdBy(doc.getCreatedBy())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
