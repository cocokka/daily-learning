package com.demo.dl.ocr.platform.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

  private final Scheduler ioScheduler;
  private final S3FileStorageService storageService;

  public Mono<String> generatePdfFromText(String text, String taskId, String fileName) {
    return Mono.fromCallable(
            () -> {
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              PdfWriter writer = new PdfWriter(baos);
              PdfDocument pdfDoc = new PdfDocument(writer);
              Document document = new Document(pdfDoc);

              document.add(new Paragraph("OCR识别结果报告").setFontSize(16));
              document.add(new Paragraph("\n"));
              document.add(new Paragraph("生成时间: " + LocalDateTime.now()).setFontSize(10));
              document.add(new Paragraph("原始文件: " + fileName).setFontSize(10));
              document.add(new Paragraph("\n"));
              document.add(new Paragraph("识别内容:").setFontSize(12));
              document.add(new Paragraph("\n"));

              if (text != null && !text.isEmpty()) {
                String[] lines = text.split("\n");
                for (String line : lines) {
                  document.add(new Paragraph(line).setFontSize(11));
                }
              } else {
                document.add(new Paragraph("无识别内容").setFontSize(11));
              }

              document.close();

              byte[] pdfBytes = baos.toByteArray();
              String pdfFileName = fileName.replaceAll("\\.[^.]*$", "") + "_ocr_result.pdf";

              log.info(
                  "PDF生成成功，任务ID: {}, 文件名: {}, 大小: {} bytes", taskId, pdfFileName, pdfBytes.length);

              return new PdfData(pdfBytes, pdfFileName);
            })
        .subscribeOn(ioScheduler)
        .flatMap(pdfData -> uploadPdfToStorage(pdfData.bytes, taskId, pdfData.fileName));
  }

  private Mono<String> uploadPdfToStorage(byte[] pdfBytes, String taskId, String fileName) {
    return storageService
        .uploadBytes(pdfBytes, fileName, "ocr-pdf", taskId)
        .subscribeOn(ioScheduler)
        .map(
            fileKey -> {
              log.info("PDF上传成功，文件路径: {}", fileKey);
              return fileKey;
            })
        .onErrorResume(
            e -> {
              log.error("PDF上传失败: {}", e.getMessage(), e);
              return Mono.just(String.format("upload_failed_%s_%s", taskId, fileName));
            });
  }

  private static class PdfData {
    private final byte[] bytes;
    private final String fileName;

    public PdfData(byte[] bytes, String fileName) {
      this.bytes = bytes;
      this.fileName = fileName;
    }
  }
}
