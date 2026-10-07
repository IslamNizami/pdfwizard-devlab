package com.islamnizami.pdfwizarddevlab.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendEmailRequestDTO {

    @NotNull(message = "Document ID must be inserted.")
    private UUID documentId;

    @NotBlank(message = "Receiving email can't be null.")
    @Email(message = "Valid email format should be implemented.")
    private String toEmail;

    @NotBlank(message = "Email subject can't be null.")
    @Size(max = 200, message = "Email subject can be maximum 200 words.")
    private String subject;

    @NotBlank(message = "Email body can't be null.")
    private String body;
}