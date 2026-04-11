package com.demo.dl.ocr.platform.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OcrRecognitionServiceTest {

  @Mock private S3FileStorageService storageService;

  @Mock private ImagePreprocessService imagePreprocessService;

  @InjectMocks private OcrRecognitionService ocrService;

  @Test
  void testRecognizeText_Success() {
    byte[] mockImageBytes = new byte[] {1, 2, 3};
    when(storageService.downloadFile(anyString())).thenReturn(Mono.just(mockImageBytes));

    StepVerifier.create(ocrService.recognizeText("test-url"))
        .expectNextMatches(result -> result != null)
        .verifyComplete();
  }

  @Test
  void testRecognizeLicensePlate_Success() {
    byte[] mockImageBytes = new byte[] {1, 2, 3};
    when(storageService.downloadFile(anyString())).thenReturn(Mono.just(mockImageBytes));

    StepVerifier.create(ocrService.recognizeLicensePlate("test-url", "task-456"))
        .expectNextMatches(result -> result != null && result.getPlateNumber() != null)
        .verifyComplete();
  }
}
