package com.demo.dl.ocr.platform.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractConfig {

  @Bean
  public Tesseract tesseract() {
    Tesseract tesseract = new Tesseract();
    tesseract.setDatapath("/usr/share/tessdata");
    tesseract.setLanguage("chi_sim+eng");
    return tesseract;
  }
}
