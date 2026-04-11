package com.demo.dl.ocr.platform.service;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImagePreprocessService {

  private final Scheduler ioScheduler;

  static {
    // 加载OpenCV native库
    OpenCV.loadLocally();
  }

  /** 完整的图像预处理流程 */
  public BufferedImage preprocess(BufferedImage originalImage) {
    return convertMatToBufferedImage(preprocessMat(convertBufferedImageToMat(originalImage)));
  }

  /** 批量预处理（用于多页PDF或批量图片） */
  public Mono<byte[]> preprocessImageBytes(byte[] imageBytes, PreprocessConfig config) {
    return Mono.fromCallable(
            () -> {
              try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
                BufferedImage original = ImageIO.read(bais);
                BufferedImage processed = applyPreprocessingPipeline(original, config);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(processed, config.getOutputFormat(), baos);
                return baos.toByteArray();
              }
            })
        .subscribeOn(ioScheduler);
  }

  /** 应用完整的预处理管道 */
  private BufferedImage applyPreprocessingPipeline(BufferedImage image, PreprocessConfig config) {
    Mat mat = convertBufferedImageToMat(image);

    // 1. 灰度化
    if (config.isGrayScale()) {
      mat = convertToGray(mat);
    }
    if (config.isEqualizeHist()) {
      mat = equalizeHistogram(mat);
    }
    // 2. 二值化（Otsu自适应阈值）
    if (config.isBinaryThreshold()) {
      mat = applyOtsuThreshold(mat);
    }

    // 3. 降噪（中值滤波/高斯滤波）
    if (config.isDenoise()) {
      mat = applyDenoise(mat, config.getDenoiseType());
    }

    // 4. 倾斜校正
    if (config.isDeskew()) {
      mat = deskew(mat);
    }

    // 5. 锐化
    if (config.isSharpen()) {
      mat = sharpen(mat);
    }

    // 6. 形态学操作（去除噪点、连接文字）
    if (config.isMorphology()) {
      mat = applyMorphology(mat);
    }

    // 7. 缩放（统一尺寸，提高识别率）
    if (config.isResize()) {
      mat = resizeToStandard(mat, config.getTargetWidth(), config.getTargetHeight());
    }

    return convertMatToBufferedImage(mat);
  }

  /** 将BufferedImage转换为OpenCV Mat */
  private Mat convertBufferedImageToMat(BufferedImage image) {
    Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
    byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    mat.put(0, 0, pixels);
    return mat;
  }

  /** 将OpenCV Mat转换为BufferedImage */
  private BufferedImage convertMatToBufferedImage(Mat mat) {
    MatOfByte mob = new MatOfByte();
    Imgcodecs.imencode(".png", mat, mob);
    try {
      return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    } catch (Exception e) {
      log.error("Mat转BufferedImage失败", e);
      return null;
    }
  }

  /** 预处理核心方法（Mat版本） */
  private Mat preprocessMat(Mat src) {
    Mat result = src.clone();

    // 灰度化
    Mat gray = new Mat();
    Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

    // 高斯滤波降噪
    Mat blurred = new Mat();
    Imgproc.GaussianBlur(gray, blurred, new Size(3, 3), 0);

    // 自适应阈值二值化（效果优于全局阈值）
    Mat binary = new Mat();
    Imgproc.adaptiveThreshold(
        blurred, binary, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);

    // 形态学操作：闭运算（填充文字内部空洞）
    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
    Mat closed = new Mat();
    Imgproc.morphologyEx(binary, closed, Imgproc.MORPH_CLOSE, kernel);

    // 降噪：中值滤波
    Mat denoised = new Mat();
    Imgproc.medianBlur(closed, denoised, 3);

    return denoised;
  }

  /** 灰度化 */
  private Mat convertToGray(Mat src) {
    Mat gray = new Mat();
    if (src.channels() == 3) {
      Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
    } else {
      gray = src.clone();
    }
    return gray;
  }

  private Mat equalizeHistogram(Mat src) {
    Mat result = new Mat();
    if (src.channels() == 1) {
      Imgproc.equalizeHist(src, result);
    } else {
      Mat ycrcb = new Mat();
      Imgproc.cvtColor(src, ycrcb, Imgproc.COLOR_BGR2YCrCb);

      java.util.List<Mat> channels = new java.util.ArrayList<>();
      Core.split(ycrcb, channels);

      Imgproc.equalizeHist(channels.get(0), channels.get(0));

      Core.merge(channels, ycrcb);
      Imgproc.cvtColor(ycrcb, result, Imgproc.COLOR_YCrCb2BGR);

      ycrcb.release();
      for (Mat channel : channels) {
        channel.release();
      }
    }
    return result;
  }

  /** Otsu自适应阈值二值化 */
  private Mat applyOtsuThreshold(Mat gray) {
    Mat binary = new Mat();
    Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    return binary;
  }

  /** 降噪处理 */
  private Mat applyDenoise(Mat src, DenoiseType type) {
    Mat result = new Mat();
    switch (type) {
      case GAUSSIAN:
        Imgproc.GaussianBlur(src, result, new Size(3, 3), 0);
        break;
      case MEDIAN:
        Imgproc.medianBlur(src, result, 3);
        break;
      case BILATERAL:
        Imgproc.bilateralFilter(src, result, 9, 75, 75);
        break;
      default:
        result = src.clone();
    }
    return result;
  }

  /** 倾斜校正（通过霍夫线检测） */
  private Mat deskew(Mat src) {
    Mat edges = new Mat();
    Imgproc.Canny(src, edges, 50, 150);

    Mat lines = new Mat();
    Imgproc.HoughLines(edges, lines, 1, Math.PI / 180, 100);

    double angle = 0;
    for (int i = 0; i < lines.rows(); i++) {
      double[] val = lines.get(i, 0);
      double theta = val[1];
      angle += theta;
    }

    if (lines.rows() > 0) {
      angle = angle / lines.rows() - Math.PI / 2;

      // 旋转校正
      Point center = new Point(src.cols() / 2.0, src.rows() / 2.0);
      Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, Math.toDegrees(angle), 1.0);
      Mat rotated = new Mat();
      Imgproc.warpAffine(
          src, rotated, rotationMatrix, src.size(), Imgproc.INTER_CUBIC, Core.BORDER_REPLICATE);
      return rotated;
    }

    return src;
  }

  /** 图像锐化（增强文字边缘） */
  private Mat sharpen(Mat src) {
    Mat sharp = new Mat();
    Mat kernel = new Mat(3, 3, CvType.CV_32F);
    float[] data = {
      0, -1, 0,
      -1, 5, -1,
      0, -1, 0
    };
    kernel.put(0, 0, data);
    Imgproc.filter2D(src, sharp, -1, kernel);
    return sharp;
  }

  /** 形态学操作（去除小噪点，连接断裂文字） */
  private Mat applyMorphology(Mat src) {
    Mat result = src.clone();

    // 腐蚀操作（去除小噪点）
    Mat kernelErode = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
    Imgproc.erode(result, result, kernelErode);

    // 膨胀操作（连接文字）
    Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
    Imgproc.dilate(result, result, kernelDilate);

    return result;
  }

  /** 缩放到标准尺寸（提高OCR识别一致性） */
  private Mat resizeToStandard(Mat src, int targetWidth, int targetHeight) {
    Mat resized = new Mat();
    Size size = new Size(targetWidth, targetHeight);
    Imgproc.resize(src, resized, size, 0, 0, Imgproc.INTER_CUBIC);
    return resized;
  }

  /** 检测图像质量（清晰度、亮度、对比度） */
  public ImageQualityAssessment assessQuality(BufferedImage image) {
    Mat mat = convertBufferedImageToMat(image);
    Mat gray = convertToGray(mat);

    // 计算拉普拉斯方差（评估清晰度）
    Mat laplacian = new Mat();
    Imgproc.Laplacian(gray, laplacian, CvType.CV_64F);
    Scalar mean = Core.mean(laplacian);
    double sharpnessScore = Math.pow(mean.val[0], 2);

    // 计算亮度
    Scalar brightness = Core.mean(gray);

    // 计算对比度（标准差）
    MatOfDouble meanStd = new MatOfDouble();
    MatOfDouble stdDev = new MatOfDouble();
    Core.meanStdDev(gray, meanStd, stdDev);
    double contrastScore = stdDev.toArray()[0];

    return ImageQualityAssessment.builder()
        .sharpnessScore(sharpnessScore)
        .brightness(brightness.val[0])
        .contrastScore(contrastScore)
        .isBlurred(sharpnessScore < 100)
        .isTooDark(brightness.val[0] < 50)
        .isTooBright(brightness.val[0] > 200)
        .build();
  }

  /** 自动选择最佳预处理配置（基于图像质量评估） */
  public PreprocessConfig autoConfigure(BufferedImage image) {
    ImageQualityAssessment quality = assessQuality(image);
    PreprocessConfig config = new PreprocessConfig();

    // 根据图像质量自动调整预处理参数
    if (quality.isBlurred()) {
      config.setSharpen(true);
      config.setDenoise(true);
      config.setDenoiseType(DenoiseType.GAUSSIAN);
    }

    if (quality.isTooDark()) {
      config.setEqualizeHist(true);
    }

    if (quality.getContrastScore() < 30) {
      config.setBinaryThreshold(true);
    }

    config.setGrayScale(true);
    config.setDeskew(true);
    config.setMorphology(true);
    config.setResize(true);
    config.setTargetWidth(1200);
    config.setTargetHeight(1600);

    return config;
  }

  // 配置类和枚举
  public enum DenoiseType {
    GAUSSIAN,
    MEDIAN,
    BILATERAL
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PreprocessConfig {
    @Builder.Default private boolean grayScale = true;
    @Builder.Default private boolean binaryThreshold = false;
    @Builder.Default private boolean denoise = true;
    @Builder.Default private DenoiseType denoiseType = DenoiseType.MEDIAN;
    @Builder.Default private boolean deskew = true;
    @Builder.Default private boolean sharpen = false;
    @Builder.Default private boolean morphology = true;
    @Builder.Default private boolean resize = true;
    @Builder.Default private int targetWidth = 1200;
    @Builder.Default private int targetHeight = 1600;
    @Builder.Default private boolean equalizeHist = false;
    @Builder.Default private String outputFormat = "png";
  }

  @Data
  @Builder
  public static class ImageQualityAssessment {
    private double sharpnessScore;
    private double brightness;
    private double contrastScore;
    private boolean isBlurred;
    private boolean isTooDark;
    private boolean isTooBright;
    private String suggestion;
  }
}
