package com.demo.dl.ocr.platform.service;

import com.demo.dl.ocr.platform.vo.LicensePlateResult;
import com.demo.dl.ocr.platform.vo.OcrResult;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrRecognitionService {

  private final Scheduler ioScheduler;
  private final S3FileStorageService storageService;
  private final ImagePreprocessService imagePreprocessService;
  private final Tesseract tesseract;

  static {
    OpenCV.loadLocally();
  }

  public Mono<OcrResult> recognizeText(String fileKey) {
    return storageService
        .downloadFile(fileKey)
        .map(
            imageBytes -> {
              Instant start = Instant.now();

              try {
                BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                ImagePreprocessService.PreprocessConfig config =
                    imagePreprocessService.autoConfigure(originalImage);
                BufferedImage processedImage = imagePreprocessService.preprocess(originalImage);

                String text = tesseract.doOCR(processedImage);
                String cleanedText = cleanOcrText(text);

                String[] words = extractKeywords(cleanedText);

                Instant end = Instant.now();
                long processingTimeMs = Duration.between(start, end).toMillis();

                return OcrResult.builder()
                    .text(cleanedText)
                    .confidence(calculateConfidence(cleanedText, originalImage, processedImage))
                    .processingTimeMs(processingTimeMs)
                    .characterCount(cleanedText.length())
                    .language("zh-CN")
                    .detectedWords(words)
                    .build();

              } catch (TesseractException e) {
                log.error("Tesseract识别失败: {}", e.getMessage());
                throw new RuntimeException("OCR识别失败: " + e.getMessage(), e);
              } catch (Exception e) {
                log.error("OCR处理失败: {}", e.getMessage());
                throw new RuntimeException("OCR处理失败: " + e.getMessage(), e);
              }
            })
        .subscribeOn(ioScheduler);
  }

  public Mono<LicensePlateResult> recognizeLicensePlate(String fileUrl, String taskId) {
    return storageService
        .downloadFile(fileUrl)
        .map(
            imageBytes -> {
              Instant start = Instant.now();

              try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

                Mat mat = convertBufferedImageToMat(image);

                String plateNumber = detectAndRecognizePlate(mat);

                LicensePlateResult result = parseLicensePlate(plateNumber);
                result.setProcessingTimeMs(Duration.between(start, Instant.now()).toMillis());
                result.setConfidence(calculatePlateConfidence(plateNumber, mat));

                mat.release();
                return result;

              } catch (Exception e) {
                log.error("车牌识别失败: {}", e.getMessage());
                throw new RuntimeException("车牌识别失败: " + e.getMessage(), e);
              }
            })
        .subscribeOn(ioScheduler);
  }

  private Mat convertBufferedImageToMat(BufferedImage image) {
    Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
    byte[] pixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
    mat.put(0, 0, pixels);
    return mat;
  }

  private String detectAndRecognizePlate(Mat src) {
    Mat gray = new Mat();
    Mat blurred = new Mat();
    Mat edges = new Mat();

    try {
      Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
      Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);
      Imgproc.Canny(blurred, edges, 50, 150);

      Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 5));
      Mat closed = new Mat();
      Imgproc.morphologyEx(edges, closed, Imgproc.MORPH_CLOSE, kernel);

      Mat contoursHierarchy = new Mat();
      List<MatOfPoint> contours = new java.util.ArrayList<>();
      Imgproc.findContours(
          closed, contours, contoursHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

      Rect plateRect = findLargestPlateContour(contours, src.size());

      if (plateRect != null && plateRect.width > 0 && plateRect.height > 0) {
        Mat plateRegion = new Mat(src, plateRect);

        Mat plateGray = new Mat();
        Imgproc.cvtColor(plateRegion, plateGray, Imgproc.COLOR_BGR2GRAY);

        Mat plateBinary = new Mat();
        Imgproc.threshold(
            plateGray, plateBinary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        String recognizedText = recognizePlateCharacters(plateBinary);

        plateGray.release();
        plateBinary.release();
        plateRegion.release();

        return recognizedText;
      }

      return "";

    } finally {
      gray.release();
      blurred.release();
      edges.release();
    }
  }

  private Rect findLargestPlateContour(List<MatOfPoint> contours, Size imageSize) {
    double maxArea = 0;
    Rect largestRect = null;

    double minArea = imageSize.area() * 0.005;
    double maxAreaThreshold = imageSize.area() * 0.15;

    for (MatOfPoint contour : contours) {
      Rect rect = Imgproc.boundingRect(contour);
      double area = rect.area();

      if (area < minArea || area > maxAreaThreshold) {
        continue;
      }

      double aspectRatio = (double) rect.width / rect.height;

      if (aspectRatio >= 2.0 && aspectRatio <= 6.0) {
        if (area > maxArea) {
          maxArea = area;
          largestRect = rect;
        }
      }
    }

    return largestRect;
  }

  private String recognizePlateCharacters(Mat plateBinary) {
    try {
      BufferedImage plateImage = convertMatToBufferedImage(plateBinary);

      String text = tesseract.doOCR(plateImage);

      return cleanPlateNumber(text);

    } catch (Exception e) {
      log.error("车牌字符识别失败: {}", e.getMessage());
      return "";
    }
  }

  private BufferedImage convertMatToBufferedImage(Mat mat) {
    MatOfByte mob = new MatOfByte();
    org.opencv.imgcodecs.Imgcodecs.imencode(".png", mat, mob);
    try {
      return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    } catch (Exception e) {
      log.error("Mat转BufferedImage失败", e);
      return null;
    }
  }

  private String cleanPlateNumber(String rawText) {
    if (rawText == null || rawText.isEmpty()) {
      return "";
    }

    String cleaned = rawText.replaceAll("[^\\u4e00-\\u9fa5A-Z0-9]", "");

    if (cleaned.length() >= 7 && cleaned.length() <= 8) {
      return cleaned;
    }

    return cleaned.replaceAll("\\s+", "");
  }

  private int calculatePlateConfidence(String plateNumber, Mat plateRegion) {
    if (plateNumber == null || plateNumber.length() < 7) {
      return 0;
    }

    int confidence = 70;

    if (plateNumber.matches("[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{5,6}[D F]?")) {
      confidence += 20;
    }

    if (plateRegion != null) {
      Scalar mean = Core.mean(plateRegion);
      double brightness = mean.val[0];
      if (brightness > 50 && brightness < 200) {
        confidence += 10;
      }
    }

    return Math.min(100, confidence);
  }

  private String cleanOcrText(String rawText) {
    if (rawText == null) return "";

    String cleaned = rawText.replaceAll("\\n{3,}", "\n\n");
    cleaned = cleaned.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s\\p{Punct}]", "");
    cleaned = cleaned.replaceAll("\\s+", " ");

    return cleaned.trim();
  }

  private String[] extractKeywords(String text) {
    if (text == null || text.isEmpty()) return new String[0];

    Pattern wordPattern = Pattern.compile("[\\u4e00-\\u9fa5]{2,}|[a-zA-Z]{3,}");
    return wordPattern.matcher(text).results().map(m -> m.group()).limit(10).toArray(String[]::new);
  }

  private int calculateConfidence(String text, BufferedImage original, BufferedImage processed) {
    if (text == null || text.isEmpty()) return 0;

    int baseConfidence = Math.min(100, text.length() * 2);

    ImagePreprocessService.ImageQualityAssessment quality =
        imagePreprocessService.assessQuality(original);

    if (quality.isBlurred()) {
      baseConfidence -= 20;
    }
    if (quality.isTooDark() || quality.isTooBright()) {
      baseConfidence -= 15;
    }

    return Math.max(0, Math.min(100, baseConfidence));
  }

  private LicensePlateResult parseLicensePlate(String plateNumber) {
    if (plateNumber == null || plateNumber.length() < 7) {
      return LicensePlateResult.builder().plateNumber(plateNumber).confidence(0).build();
    }

    String province = plateNumber.substring(0, 1);
    String cityCode = plateNumber.substring(1, 2);

    String plateColor = determinePlateColor(plateNumber);
    String vehicleType = determineVehicleType(plateNumber);
    int plateType = determinePlateType(plateNumber);

    return LicensePlateResult.builder()
        .plateNumber(plateNumber)
        .confidence(85)
        .province(province)
        .cityCode(cityCode)
        .plateColor(plateColor)
        .vehicleType(vehicleType)
        .plateType(plateType)
        .build();
  }

  private String determinePlateColor(String plateNumber) {
    if (plateNumber.contains("D") || plateNumber.contains("F")) {
      return "新能源绿";
    } else if (plateNumber.startsWith("W")) {
      return "白";
    } else {
      return "蓝";
    }
  }

  private String determineVehicleType(String plateNumber) {
    if (plateNumber.length() == 8) {
      return "新能源车";
    } else if (plateNumber.length() == 7) {
      return "小型车";
    } else {
      return "大型车";
    }
  }

  private int determinePlateType(String plateNumber) {
    if (plateNumber.contains("D") || plateNumber.contains("F")) {
      return 2;
    } else if (plateNumber.startsWith("W")) {
      return 3;
    } else {
      return 1;
    }
  }
}
