package com.demo.dl.jrebel.license.server.service;

import com.demo.dl.jrebel.license.server.util.ByteUtil;
import com.demo.dl.jrebel.license.server.util.Hex;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.springframework.stereotype.Service;

@Service
public class RsaSignService {

  private static final String ASN_KEY =
      """
            -----BEGIN RSA PRIVATE KEY-----\r
            MIIBOgIBAAJBALecq3BwAI4YJZwhJ+snnDFj3lF3DMqNPorV6y5ZKXCiCMqj8OeOmxk4YZW9aaV9\r
            ckl/zlAOI0mpB3pDT+Xlj2sCAwEAAQJAW6/aVD05qbsZHMvZuS2Aa5FpNNj0BDlf38hOtkhDzz/h\r
            kYb+EBYLLvldhgsD0OvRNy8yhz7EjaUqLCB0juIN4QIhAOeCQp+NXxfBmfdG/S+XbRUAdv8iHBl+\r
            F6O2wr5fA2jzAiEAywlDfGIl6acnakPrmJE0IL8qvuO3FtsHBrpkUuOnXakCIQCqdr+XvADI/UTh\r
            TuQepuErFayJMBSAsNe3NFsw0cUxAQIgGA5n7ZPfdBi3BdM4VeJWb87WrLlkVxPqeDSbcGrCyMkC\r
            IFSs5JyXvFTreWt7IQjDssrKDRIPmALdNjvfETwlNJyY\r
            -----END RSA PRIVATE KEY-----""";

  private static final String PKCS8_KEY =
      """
            -----BEGIN PRIVATE KEY-----\r
            MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAt5yrcHAAjhglnCEn\r
            6yecMWPeUXcMyo0+itXrLlkpcKIIyqPw546bGThhlb1ppX1ySX/OUA4jSakHekNP\r
            5eWPawIDAQABAkBbr9pUPTmpuxkcy9m5LYBrkWk02PQEOV/fyE62SEPPP+GRhv4Q\r
            Fgsu+V2GCwPQ69E3LzKHPsSNpSosIHSO4g3hAiEA54JCn41fF8GZ90b9L5dtFQB2\r
            /yIcGX4Xo7bCvl8DaPMCIQDLCUN8YiXppydqQ+uYkTQgvyq+47cW2wcGumRS46dd\r
            qQIhAKp2v5e8AMj9ROFO5B6m4SsVrIkwFICw17c0WzDRxTEBAiAYDmftk990GLcF\r
            0zhV4lZvztasuWRXE+p4NJtwasLIyQIgVKzknJe8VOt5a3shCMOyysoNEg+YAt02\r
            O98RPCU0nJg=\r
            -----END PRIVATE KEY-----""";

  private static final String KEY_22 =
      """
            MIIBOgIBAAJBALecq3BwAI4YJZwhJ+snnDFj3lF3DMqNPorV6y5ZKXCiCMqj8OeOmxk4YZW9aaV9\
            ckl/zlAOI0mpB3pDT+Xlj2sCAwEAAQJAW6/aVD05qbsZHMvZuS2Aa5FpNNj0BDlf38hOtkhDzz/h\
            kYb+EBYLLvldhgsD0OvRNy8yhz7EjaUqLCB0juIN4QIhAOeCQp+NXxfBmfdG/S+XbRUAdv8iHBl+\
            F6O2wr5fA2jzAiEAywlDfGIl6acnakPrmJE0IL8qvuO3FtsHBrpkUuOnXakCIQCqdr+XvADI/UTh\
            TuQepuErFayJMBSAsNe3NFsw0cUxAQIgGA5n7ZPfdBi3BdM4VeJWb87WrLlkVxPqeDSbcGrCyMkC\
            IFSs5JyXvFTreWt7IQjDssrKDRIPmALdNjvfETwlNJyY""";

  private static final String KEY_33 =
      """
            MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAt5yrcHAAjhglnCEn\
            6yecMWPeUXcMyo0+itXrLlkpcKIIyqPw546bGThhlb1ppX1ySX/OUA4jSakHekNP\
            5eWPawIDAQABAkBbr9pUPTmpuxkcy9m5LYBrkWk02PQEOV/fyE62SEPPP+GRhv4Q\
            Fgsu+V2GCwPQ69E3LzKHPsSNpSosIHSO4g3hAiEA54JCn41fF8GZ90b9L5dtFQB2\
            /yIcGX4Xo7bCvl8DaPMCIQDLCUN8YiXppydqQ+uYkTQgvyq+47cW2wcGumRS46dd\
            qQIhAKp2v5e8AMj9ROFO5B6m4SsVrIkwFICw17c0WzDRxTEBAiAYDmftk990GLcF\
            0zhV4lZvztasuWRXE+p4NJtwasLIyQIgVKzknJe8VOt5a3shCMOyysoNEg+YAt02\
            O98RPCU0nJg=""";

  public String sign(String content) {
    return signWithASN(content.getBytes(), KEY_22);
  }

  public String signWithPKCS8(String content) {
    return signWithPKCS8(content.getBytes(), KEY_33);
  }

  private String signWithASN(byte[] content, String privateKey) {
    try {
      var keyBytes = ByteUtil.decodeBase64(privateKey);

      try (var in = new ASN1InputStream(keyBytes)) {
        var obj = in.readObject();
        var keyInfo = PrivateKeyInfo.getInstance(obj);
        var rsaPrivKey = RSAPrivateKey.getInstance(keyInfo.parsePrivateKey());
        var spec = new RSAPrivateKeySpec(rsaPrivKey.getModulus(), rsaPrivKey.getPrivateExponent());
        var keyFactory = KeyFactory.getInstance("RSA");
        var priKey = keyFactory.generatePrivate(spec);

        var signature = java.security.Signature.getInstance("MD5WithRSA");
        signature.initSign(priKey);
        signature.update(content);
        var signed = signature.sign();

        return Hex.bytesToHexString(signed);
      }
    } catch (Exception e) {
      throw new RuntimeException("ASN signing failed", e);
    }
  }

  private String signWithPKCS8(byte[] content, String privateKey) {
    try {
      var priPKCS8 = new PKCS8EncodedKeySpec(ByteUtil.decodeBase64(privateKey));
      var keyf = KeyFactory.getInstance("RSA");
      var priKey = keyf.generatePrivate(priPKCS8);

      var signature = java.security.Signature.getInstance("MD5WithRSA");
      signature.initSign(priKey);
      signature.update(content);
      var signed = signature.sign();

      return Hex.bytesToHexString(signed);
    } catch (Exception e) {
      throw new RuntimeException("PKCS8 signing failed", e);
    }
  }
}
