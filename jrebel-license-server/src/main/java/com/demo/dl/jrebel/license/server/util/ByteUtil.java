package com.demo.dl.jrebel.license.server.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base64;

public class ByteUtil {

  private ByteUtil() {}

  private static final SecureRandom secureRandom = new SecureRandom();
  private static final Charset UTF_8 = StandardCharsets.UTF_8;

  public static String encodeBase64(byte[] binaryData) {
    return binaryData == null ? null : new String(Base64.encodeBase64(binaryData), UTF_8);
  }

  public static byte[] decodeBase64(String s) {
    return s == null ? null : Base64.decodeBase64(s.getBytes(UTF_8));
  }

  public static byte[] generateRandomBytes(int length) {
    var array = new byte[length];
    secureRandom.nextBytes(array);
    return array;
  }
}
