package com.demo.dl.jrebel.license.server.service;

import com.demo.dl.jrebel.license.server.util.ByteUtil;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

@Service
public class JRebelPrivateKeyService {

  private static final String ENCODED_PRIVATE_KEY =
      "MIICXAIBAAKBgQDQ93CP6SjEneDizCF1P/MaBGf582voNNFcu8oMhgdTZ/N6qa6O7XJDr1FSCyaDdKSsPCdxPK7Y4Usq/fOPas2kCgYcRS/iebrtPEFZ/7TLfk39HLuTEjzo0/CNvjVsgWeh9BYznFaxFDLx7fLKqCQ6w1OKScnsdqwjpaXwXqiulwIDAQABAoGATOQvvBSMVsTNQkbgrNcqKdGjPNrwQtJkk13aO/95ZJxkgCc9vwPqPrOdFbZappZeHa5IyScOI2nLEfe+DnC7V80K2dBtaIQjOeZQt5HoTRG4EHQaWoDh27BWuJoip5WMrOd+1qfkOtZoRjNcHl86LIAh/+3vxYyebkug4UHNGPkCQQD+N4ZUkhKNQW7mpxX6eecitmOdN7Yt0YH9UmxPiW1LyCEbLwduMR2tfyGfrbZALiGzlKJize38shGC1qYSMvZFAkEA0m6psWWiTUWtaOKMxkTkcUdigalZ9xFSEl6jXFB94AD+dlPS3J5gNzTEmbPLc14VIWJFkO+UOrpl77w5uF2dKwJAaMpslhnsicvKMkv31FtBut5iK6GWeEafhdPfD94/bnidpP362yJl8Gmya4cI1GXvwH3pfj8S9hJVA5EFvgTB3QJBAJP1O1uAGp46X7Nfl5vQ1M7RYnHIoXkWtJ417Kb78YWPLVwFlD2LHhuy/okT4fk8LZ9LeZ5u1cp1RTdLIUqAiAECQC46OwOm87L35yaVfpUIjqg/1gsNwNsj8HvtXdF/9d30JIM3GwdytCvNRLqP35Ciogb9AO8ke8L6zY83nxPbClM=";

  private final PrivateKey privateKey;
  private final BouncyCastleProvider bouncyCastleProvider;

  public JRebelPrivateKeyService() {
    this.bouncyCastleProvider = new BouncyCastleProvider();
    this.privateKey = loadPrivateKey();
  }

  private PrivateKey loadPrivateKey() {
    try {
      var keyBytes = ByteUtil.decodeBase64(ENCODED_PRIVATE_KEY);
      var keySpec = new PKCS8EncodedKeySpec(keyBytes);
      return KeyFactory.getInstance("RSA", bouncyCastleProvider).generatePrivate(keySpec);
    } catch (GeneralSecurityException ex) {
      throw new RuntimeException("Failed to load private key", ex);
    }
  }

  public byte[] sign(byte[] data) {
    try {
      var signature = Signature.getInstance("SHA1withRSA", bouncyCastleProvider);
      signature.initSign(privateKey);
      signature.update(data);
      return signature.sign();
    } catch (GeneralSecurityException ex) {
      throw new RuntimeException("License Server installation error 0000000F2", ex);
    }
  }

  public String signAndEncodeToString(String data) {
    var signedData = sign(data.getBytes());
    return ByteUtil.encodeBase64(signedData);
  }
}
