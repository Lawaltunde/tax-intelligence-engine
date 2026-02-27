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

    /** Maximum accepted file size: 20 MB. Rejects abnormally large uploads before reading. */
    private static final long MAX_FILE_BYTES = 20L * 1024 * 1024;

    /** PDF magic header bytes (%PDF-). */
    private static final byte[] PDF_MAGIC = {'%', 'P', 'D', 'F', '-'};

    /**
     * Extracts all text content from the supplied PDF multipart file.
     *
     * @param file uploaded PDF
     * @return extracted plain text (never null, never empty)
     * @throws DocumentExtractionException if the file is not a valid PDF or contains no text
     */
    public String extract(MultipartFile file) {
        validateContentType(file);
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new DocumentExtractionException(
                    "File '%s' exceeds the maximum allowed size of 20 MB (%d bytes)."
                            .formatted(file.getOriginalFilename(), file.getSize()));
        }
        try (InputStream is = file.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            validatePdfSignature(bytes, file.getOriginalFilename());
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

    /**
     * Verifies the PDF magic header (%PDF-) as a second-layer defence against
     * content-type spoofing (client-controlled header cannot be trusted alone).
     */
    private void validatePdfSignature(byte[] bytes, String filename) {
        if (bytes.length < PDF_MAGIC.length) {
            throw new DocumentExtractionException(
                    "File '%s' is too small to be a valid PDF.".formatted(filename));
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (bytes[i] != PDF_MAGIC[i]) {
                throw new DocumentExtractionException(
                        "File '%s' does not start with the PDF magic header — not a valid PDF.".formatted(filename));
            }
        }
    }
}
