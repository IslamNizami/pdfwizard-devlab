package com.islamnizami.pdfwizarddevlab.repository;

import com.islamnizami.pdfwizarddevlab.model.entity.PdfDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PdfDocumentRepository extends JpaRepository<PdfDocument, UUID>, JpaSpecificationExecutor<PdfDocument> {

    List<PdfDocument> findAllByCreatedAtBefore(LocalDateTime threshold);
}
