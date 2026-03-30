package com.demo.dl.jrebel.license.server.service;

import com.demo.dl.jrebel.license.server.util.ByteUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JrebelSign {

  private String signature;
  private final JRebelPrivateKeyService privateKeyService;

  public JrebelSign(JRebelPrivateKeyService privateKeyService) {
    this.privateKeyService = privateKeyService;
  }

  public void toLeaseCreateJson(
      String clientRandomness, String guid, boolean offline, String validFrom, String validUntil) {
    var serverRandomness = "H2ulzLlh7E0=";
    var installationGuidString = guid;

    var s2 =
        offline
            ? StringUtils.join(
                new String[] {
                  clientRandomness,
                  serverRandomness,
                  installationGuidString,
                  String.valueOf(offline),
                  validFrom,
                  validUntil
                },
                ';')
            : StringUtils.join(
                new String[] {
                  clientRandomness,
                  serverRandomness,
                  installationGuidString,
                  String.valueOf(offline)
                },
                ';');

    log.info("s2: {}", s2);

    var signedData = privateKeyService.sign(s2.getBytes());
    this.signature = ByteUtil.encodeBase64(signedData);
  }

  public String getSignature() {
    return signature;
  }
}
