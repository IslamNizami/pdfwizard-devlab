package com.islamnizami.pdfwizarddevlab.config;


import com.islamnizami.pdfwizarddevlab.model.entity.PdfDocument;
import com.islamnizami.pdfwizarddevlab.repository.PdfDocumentRepository;
import com.islamnizami.pdfwizarddevlab.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final PdfDocumentRepository documentRepository;
    private final StorageService storageService;
    private final CacheManager cacheManager;

    @Value("${app.cleanup.retention-days:7}")
    private int retentionDays;

    @Scheduled(cron = "${app.cleanup.cron}")
    @Transactional
    public void cleanupOldDocuments(){
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);

        log.info("deleting operation for old documents is started {}", threshold);

        List<PdfDocument> expiredDocs = documentRepository.findAllByCreatedAtBefore(threshold);

        for(PdfDocument doc : expiredDocs){
            try{
                storageService.deleteFile(doc.getStorageKey());
                documentRepository.delete(doc);

                if(cacheManager.getCache("pdf_metadata") != null){
                    cacheManager.getCache("pdf_metadata").evict(doc.getId());
                }
                log.info("Deleted: {} (ID: {})", doc.getFileName(), doc.getId());
            }catch (Exception e){
                log.error("Error while deleting old document: {}", doc.getId(),e);
            }
        }
    }
}
