package com.islamnizami.pdfwizarddevlab.service;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfEngineService {


    public byte[] generatePdf(String title, String content, String watermark, String qrPayload) throws Exception{

        try(PDDocument document = new PDDocument()){
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try(PDPageContentStream cs = new PDPageContentStream(document,page)){
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),18);
                cs.newLineAtOffset(50,750);
                cs.showText(title);
                cs.endText();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 710);
                cs.showText(content);
                cs.endText();
            }

            if(qrPayload != null && !qrPayload.isBlank()){
                BufferedImage qrImage = generateQrImage(qrPayload,120,120);
                PDImageXObject pdImage = JPEGFactory.createFromImage(document,qrImage);
                try(PDPageContentStream cs = new PDPageContentStream(document,page,PDPageContentStream.AppendMode.APPEND,true,true)){
                    cs.drawImage(pdImage,420,700,120,120);
                }
            }

            if(watermark != null && !watermark.isBlank()){
                applyWatermark(document,watermark);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }

    }

    public byte[] mergePdfs(List<MultipartFile> files) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        merger.setDestinationStream(outputStream);

        List<PDDocument> loadedDocs = new ArrayList<>();
        try{
            for(MultipartFile file : files){
                PDDocument doc = Loader.loadPDF(file.getBytes());
                loadedDocs.add(doc);
                merger.addSource(new org.apache.pdfbox.io.RandomAccessReadBuffer(file.getBytes()));
            }
            merger.mergeDocuments(null);
            return outputStream.toByteArray();
        }finally {
            for(PDDocument doc : loadedDocs){
                doc.close();
            }
        }
    }

    public List<byte[]> splitPdf(MultipartFile file) throws IOException {
        List<byte[]> splitPages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            Splitter splitter = new Splitter();
            List<PDDocument> pages = splitter.split(document);

            for (PDDocument pageDoc : pages) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                pageDoc.save(baos);
                pageDoc.close();
                splitPages.add(baos.toByteArray());
            }
        }
        return splitPages;
    }

    private void applyWatermark(PDDocument doc, String watermarkText) throws IOException {
        for (PDPage p : doc.getPages()) {
            try (PDPageContentStream cs = new PDPageContentStream(doc, p, PDPageContentStream.AppendMode.APPEND, true, true)) {
                PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                gs.setNonStrokingAlphaConstant(0.2f);
                cs.setGraphicsStateParameters(gs);

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 50);
                cs.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(45), 200, 350));
                cs.showText(watermarkText);
                cs.endText();
            }
        }
    }

    private BufferedImage generateQrImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

}
