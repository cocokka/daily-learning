package com.demo.dl.ocr.platform;

import java.io.UnsupportedEncodingException;
import org.junit.jupiter.api.Test;

// @SpringBootTest
class OcrPlatformApplicationTests {

  @Test
  void contextLoads() throws UnsupportedEncodingException {
    System.out.println(
        "1541eb3e8db587c701aef2b148933e3581f89a91b4142ec2e422675e01ed2efd".getBytes("UTF-8").length
            * 8);
  }
}
