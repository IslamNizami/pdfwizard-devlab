package com.islamnizami.pdfwizarddevlab.controller;

import com.islamnizami.pdfwizarddevlab.model.dto.CreatePdfRequestDTO;
import com.islamnizami.pdfwizarddevlab.model.dto.PdfDocumentResponseDTO;
import com.islamnizami.pdfwizarddevlab.model.dto.PdfSearchCriteriaDTO;
import com.islamnizami.pdfwizarddevlab.model.dto.SendEmailRequestDTO;
import com.islamnizami.pdfwizarddevlab.model.entity.PdfDocument;
import com.islamnizami.pdfwizarddevlab.service.EmailService;
import com.islamnizami.pdfwizarddevlab.service.PdfEngineService;
import com.islamnizami.pdfwizarddevlab.service.PdfManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfEngineService engineService;
    private final PdfManagementService managementService;
    private final EmailService emailService;

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PdfDocumentResponseDTO> createPdf(
            @Valid @RequestBody CreatePdfRequestDTO request,
            @RequestHeader(value = "X-User", defaultValue = "system_user") String user) throws Exception {

        byte[] pdfBytes = engineService.generatePdf(
                request.getTitle(),
                request.getContent(),
                request.getWatermark(),
                request.getQrPayload()
        );

        PdfDocumentResponseDTO response = managementService.saveDocument(request.getFileName(), pdfBytes, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PdfDocumentResponseDTO> getMetadata(@PathVariable UUID id) {
        return ResponseEntity.ok(managementService.getMetadata(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User", defaultValue = "system_user") String user) {

        PdfDocumentResponseDTO metadata = managementService.getMetadata(id);
        byte[] content = managementService.download(id, user);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                .body(content);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User", defaultValue = "system_user") String user) {

        managementService.deleteDocument(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<Page<PdfDocumentResponseDTO>> listDocuments(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(managementService.getAllDocuments(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PdfDocumentResponseDTO>> searchDocuments(
            @ModelAttribute PdfSearchCriteriaDTO criteria,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(managementService.searchDocuments(criteria, pageable));
    }

    @PostMapping(value = "/merge", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PdfDocumentResponseDTO> merge(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "merged_document.pdf") String outputFileName,
            @RequestHeader(value = "X-User", defaultValue = "system_user") String user) throws Exception {

        if (files.isEmpty()) {
            throw new IllegalArgumentException("To merge at least 1 pdf file should be sent.");
        }

        byte[] mergedBytes = engineService.mergePdfs(files);
        PdfDocumentResponseDTO response = managementService.saveDocument(outputFileName, mergedBytes, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/split", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PdfDocumentResponseDTO>> split(
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-User", defaultValue = "system_user") String user) throws Exception {

        List<byte[]> pages = engineService.splitPdf(file);
        List<PdfDocumentResponseDTO> responses = new ArrayList<>();
        int pageIndex = 1;

        String baseName = file.getOriginalFilename() != null ? file.getOriginalFilename().replace(".pdf", "") : "doc";

        for (byte[] pageBytes : pages) {
            String pageFileName = baseName + "_page_" + pageIndex + ".pdf";
            responses.add(managementService.saveDocument(pageFileName, pageBytes, user));
            pageIndex++;
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PostMapping(value = "/send-email", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendEmail(
            @Valid @RequestBody SendEmailRequestDTO request,
            @RequestHeader(value = "X-User", defaultValue = "system_user") String user) throws Exception {

        PdfDocument doc = managementService.getDocumentEntity(request.getDocumentId());
        byte[] pdfBytes = managementService.download(request.getDocumentId(), user);

        emailService.sendPdfEmail(
                request.getToEmail(),
                request.getSubject(),
                request.getBody(),
                doc.getFileName(),
                pdfBytes
        );

        return ResponseEntity.ok("Email sent successfully with attachment: " + request.getToEmail());
    }
}