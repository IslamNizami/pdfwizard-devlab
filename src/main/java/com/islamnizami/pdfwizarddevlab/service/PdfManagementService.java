package com.islamnizami.pdfwizarddevlab.service;

import com.islamnizami.pdfwizarddevlab.config.PdfSpecification;
import com.islamnizami.pdfwizarddevlab.exception.ResourceNotFoundException;
import com.islamnizami.pdfwizarddevlab.model.dto.PdfDocumentResponseDTO;
import com.islamnizami.pdfwizarddevlab.model.dto.PdfSearchCriteriaDTO;
import com.islamnizami.pdfwizarddevlab.model.entity.PdfAuditLog;
import com.islamnizami.pdfwizarddevlab.model.entity.PdfDocument;
import com.islamnizami.pdfwizarddevlab.repository.PdfAuditLogRepository;
import com.islamnizami.pdfwizarddevlab.repository.PdfDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfManagementService {

    private final PdfDocumentRepository documentRepository;
    private final PdfAuditLogRepository auditLogRepository;
    private final StorageService storageService;

    @Transactional
    public PdfDocumentResponseDTO saveDocument(String fileName, byte[] content, String user) {
        String cleanFileName = fileName.endsWith(".pdf") ? fileName : fileName + ".pdf";
        String storageKey = UUID.randomUUID() + "_" + cleanFileName;

        storageService.uploadFile(storageKey, content, "application/pdf");

        PdfDocument document = PdfDocument.builder()
                .fileName(cleanFileName)
                .storageKey(storageKey)
                .fileSize((long) content.length)
                .contentType("application/pdf")
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build();

        PdfDocument saved = documentRepository.save(document);

        auditLogRepository.save(PdfAuditLog.builder()
                .documentId(saved.getId())
                .action(PdfAuditLog.ActionType.CREATE)
                .performedBy(user)
                .build());

        return PdfDocumentResponseDTO.fromEntity(saved);
    }

    @Cacheable(value = "pdf_metadata", key = "#id")
    public PdfDocumentResponseDTO getMetadata(UUID id) {
        PdfDocument document = getDocumentEntity(id);
        return PdfDocumentResponseDTO.fromEntity(document);
    }

    public byte[] download(UUID id, String user) {
        PdfDocument doc = getDocumentEntity(id);
        byte[] data = storageService.downloadFile(doc.getStorageKey());

        auditLogRepository.save(PdfAuditLog.builder()
                .documentId(id)
                .action(PdfAuditLog.ActionType.DOWNLOAD)
                .performedBy(user)
                .build());

        return data;
    }

    @Transactional
    @CacheEvict(value = "pdf_metadata", key = "#id")
    public void deleteDocument(UUID id, String user) {
        PdfDocument doc = getDocumentEntity(id);

        storageService.deleteFile(doc.getStorageKey());
        documentRepository.delete(doc);

        auditLogRepository.save(PdfAuditLog.builder()
                .documentId(id)
                .action(PdfAuditLog.ActionType.DELETE)
                .performedBy(user)
                .build());
    }

    public Page<PdfDocumentResponseDTO> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable).map(PdfDocumentResponseDTO::fromEntity);
    }

    public Page<PdfDocumentResponseDTO> searchDocuments(PdfSearchCriteriaDTO criteria, Pageable pageable) {
        return documentRepository.findAll(PdfSpecification.withCriteria(criteria), pageable)
                .map(PdfDocumentResponseDTO::fromEntity);
    }

    public PdfDocument getDocumentEntity(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document couldn't found with id: " + id));
    }
}