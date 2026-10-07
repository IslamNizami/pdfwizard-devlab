package com.islamnizami.pdfwizarddevlab.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePdfRequestDTO {

    @NotBlank(message = "File name can't be null")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+(\\.[pP][dD][fF])?$", message = "File name can only contain letter, digit, - and _.")
    private String fileName;

    @NotBlank(message = "Title can't be null")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters.")
    private String title;

    @NotBlank(message = "Content can't be null")
    @Size(max = 10000, message = "Content can be maximum 10000")
    private String content;

    @Size(max = 50, message = "Watermark text can only be maximum 50 words.")
    private String watermark;

    @Size(max = 500, message = "QR code can be info can be maximum 500.")
    private String qrPayload;
}