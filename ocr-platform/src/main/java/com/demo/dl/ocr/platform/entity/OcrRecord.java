package com.demo.dl.ocr.platform.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ocr_record")
public class OcrRecord {
  @Id private Long id;
  private String taskId;
  private String fileName;
  private String filePath;
  private Long fileSize;
  private String fileType;
  private String recognizedText;
  private String ocrType; // TEXT, LICENSE_PLATE, ID_CARD
  private String licensePlateNumber;
  private String pdfUrl;
  private String status; // PENDING, PROCESSING, SUCCESS, FAILED
  private String errorMessage;
  private Integer confidence;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
