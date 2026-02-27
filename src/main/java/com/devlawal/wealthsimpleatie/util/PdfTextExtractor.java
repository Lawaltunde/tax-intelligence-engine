package com.devlawal.wealthsimpleatie.util;

import com.devlawal.wealthsimpleatie.exception.DocumentExtractionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Pure utility component that converts a PDF byte stream to plain text.
 * Has no dependency on AI or business logic — it is testable in isolation.
 */
@Component
public class PdfTextExtractor {

    /**
     * Extracts all text content from the supplied PDF multipart file.
     *
     * @param file uploaded PDF
     * @return extracted plain text (never null, never empty)
     * @throws DocumentExtractionException if the file is not a valid PDF or contains no text
     */
    public String extract(MultipartFile file) {
        validateContentType(file);
        try (InputStream is = file.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            try (PDDocument doc = Loader.loadPDF(bytes)) {

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(doc);

                if (text == null || text.isBlank()) {
                    throw new DocumentExtractionException(
                            "PDF '%s' contains no extractable text.".formatted(file.getOriginalFilename()));
                }
                return text;
            }
        } catch (DocumentExtractionException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new DocumentExtractionException(
                    "Failed to read PDF '%s': %s".formatted(file.getOriginalFilename(), ex.getMessage()), ex);
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new DocumentExtractionException(
                    "Unsupported file type '%s'. Only PDF files are accepted.".formatted(contentType));
        }
    }
}
