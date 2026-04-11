package com.demo.dl.ocr.platform.service;

import com.demo.dl.ocr.platform.config.StorageConfigProperties;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy/MM/dd");

  private final Scheduler ioScheduler;
  private final S3Client s3Client;
  private final StorageConfigProperties storageConfig;

  public Mono<String> uploadFile(MultipartFile file, String prefix, String taskId) {
    String originalFilename = file.getOriginalFilename();
    String fileKey = generateFileKey(originalFilename, prefix, taskId);
    return Mono.fromCallable(
            () -> {
              try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putRequest =
                    PutObjectRequest.builder()
                        .bucket(storageConfig.getBucket())
                        .key(fileKey)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build();

                s3Client.putObject(
                    putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
                log.info("File uploaded to S3: {}", fileKey);
                return fileKey;
              } catch (Exception e) {
                log.error("Failed to upload file to S3", e);
                throw new RuntimeException("Upload failed", e);
              }
            })
        .subscribeOn(ioScheduler);
  }

  public Mono<String> uploadBytes(byte[] content, String fileName, String prefix, String taskId) {
    String fileKey = generateFileKey(fileName, prefix, taskId);
    return Mono.fromCallable(
            () -> {
              try {
                String contentType = determineContentType(fileName);

                PutObjectRequest putRequest =
                    PutObjectRequest.builder()
                        .bucket(storageConfig.getBucket())
                        .key(fileKey)
                        .contentType(contentType)
                        .contentLength((long) content.length)
                        .build();

                s3Client.putObject(putRequest, RequestBody.fromBytes(content));
                log.info("Bytes uploaded to S3: {}", fileKey);
                return fileKey;
              } catch (Exception e) {
                log.error("Failed to upload bytes to S3", e);
                throw new RuntimeException("Upload failed", e);
              }
            })
        .subscribeOn(ioScheduler);
  }

  private String determineContentType(String fileName) {
    if (fileName == null) {
      return "application/octet-stream";
    }

    String lowerName = fileName.toLowerCase();
    if (lowerName.endsWith(".pdf")) {
      return "application/pdf";
    } else if (lowerName.endsWith(".png")) {
      return "image/png";
    } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (lowerName.endsWith(".gif")) {
      return "image/gif";
    } else if (lowerName.endsWith(".txt")) {
      return "text/plain";
    } else {
      return "application/octet-stream";
    }
  }

  public Mono<byte[]> downloadFile(String fileKey) {
    return Mono.fromCallable(
            () -> {
              if (!fileExists(fileKey)) {
                throw new RuntimeException("文件不存在: " + fileKey);
              }

              try {
                GetObjectRequest getRequest =
                    GetObjectRequest.builder()
                        .bucket(storageConfig.getBucket())
                        .key(fileKey)
                        .build();
                return s3Client.getObjectAsBytes(getRequest).asByteArray();
              } catch (S3Exception e) {
                log.error("下载文件失败: {} - {}", fileKey, e.getMessage(), e);
                throw new RuntimeException("文件下载失败: " + e.getMessage());
              }
            })
        .subscribeOn(ioScheduler);
  }

  /** 检查文件是否存在 */
  public boolean fileExists(String fileKey) {
    try {
      HeadObjectRequest headRequest =
          HeadObjectRequest.builder().bucket(storageConfig.getBucket()).key(fileKey).build();
      s3Client.headObject(headRequest);
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    } catch (S3Exception e) {
      log.warn("检查文件存在性失败: {} - {}", fileKey, e.getMessage());
      return false;
    }
  }

  public Mono<Void> deleteFile(String fileKey) {
    return Mono.fromRunnable(
            () -> {
              if (fileKey == null || fileKey.isEmpty()) {
                log.debug("文件键为空，跳过删除");
                return;
              }

              if (!fileExists(fileKey)) {
                log.warn("文件不存在，跳过删除: {}", fileKey);
                return;
              }

              DeleteObjectRequest deleteRequest =
                  DeleteObjectRequest.builder()
                      .bucket(storageConfig.getBucket())
                      .key(fileKey)
                      .build();
              s3Client.deleteObject(deleteRequest);
              log.info("文件删除成功: {}", fileKey);
            })
        .subscribeOn(ioScheduler)
        .then();
  }

  /** 生成文件键 */
  private String generateFileKey(String originalFilename, String prefix, String taskId) {
    LocalDateTime now = LocalDateTime.now();
    String datePath = now.format(DATE_TIME_FORMATTER);
    String safeName = sanitizeFilename(originalFilename);
    return String.format("%s/%s/%s_%s", prefix, datePath, taskId, safeName);
  }

  /**
   * 清理文件名，移除不安全的字符
   *
   * <p>汉字转换为大驼峰拼音，保留字母、数字、点号、下划线和连字符， 其他字符统一替换为下划线，防止 S3 存储出现问题。
   *
   * @param filename 原始文件名
   * @return 清理后的安全文件名
   */
  private String sanitizeFilename(String filename) {
    if (filename == null || filename.isEmpty()) {
      return "unknown";
    }
    return convertToPinyin(filename);
  }

  /**
   * 将字符串中的汉字转换为大驼峰拼音，非汉字字符保持不变
   *
   * @param input 输入字符串
   * @return 转换后的字符串
   */
  private String convertToPinyin(String input) {
    HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
    format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

    StringBuilder result = new StringBuilder();
    for (char ch : input.toCharArray()) {
      try {
        String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(ch, format);
        if (pinyins != null && pinyins.length > 0) {
          result.append(capitalize(pinyins[0]));
        } else {
          result.append(sanitizeChar(ch));
        }
      } catch (BadHanyuPinyinOutputFormatCombination e) {
        result.append(sanitizeChar(ch));
      }
    }
    return result.toString();
  }

  /** 处理单个字符，保留安全字符，其他替换为下划线 */
  private char sanitizeChar(char ch) {
    if ((ch >= 'a' && ch <= 'z')
        || (ch >= 'A' && ch <= 'Z')
        || (ch >= '0' && ch <= '9')
        || ch == '.'
        || ch == '_'
        || ch == '-') {
      return ch;
    }
    return '_';
  }

  /** 首字母大写 */
  private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  public boolean isStorageAccessible() {
    try {
      s3Client.headBucket(builder -> builder.bucket(storageConfig.getBucket()));
      return true;
    } catch (Exception e) {
      log.warn("存储连接检查失败: {}", e.getMessage());
      return false;
    }
  }
}
