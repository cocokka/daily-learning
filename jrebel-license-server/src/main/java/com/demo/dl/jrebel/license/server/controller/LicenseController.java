package com.demo.dl.jrebel.license.server.controller;

import com.demo.dl.jrebel.license.server.service.JrebelSign;
import com.demo.dl.jrebel.license.server.service.RsaSignService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import tools.jackson.databind.ObjectMapper;

@Controller
public class LicenseController {

  private final JrebelSign jrebelSign;
  private final RsaSignService rsaSignService;
  private final ObjectMapper objectMapper;

  public LicenseController(
      JrebelSign jrebelSign, RsaSignService rsaSignService, ObjectMapper objectMapper) {
    this.jrebelSign = jrebelSign;
    this.rsaSignService = rsaSignService;
    this.objectMapper = objectMapper;
  }

  @RequestMapping("/")
  @ResponseBody
  public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html; charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);

    var licenseUrl =
        request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

    var html =
        new StringBuffer(
            """
                <h3>使用说明（Instructions for use）</h3>
                <hr/>
                <h1>Hello, This is a Jrebel & JetBrains License Server!</h1>
                """);

    html.append("<p>License Server started at ").append(licenseUrl);
    html.append("<p>JetBrains Activation address was: <span style='color:red'>")
        .append(licenseUrl)
        .append("/");
    html.append(
            "<p>JRebel 7.1 and earlier version Activation address was: <span style='color:red'>")
        .append(licenseUrl)
        .append("/{tokenname}")
        .append("</span>, with any email.");
    html.append("<p>JRebel 2018.1 and later version Activation address was: ")
        .append(licenseUrl)
        .append("/{guid}")
        .append("(eg:<span style='color:red'>")
        .append(licenseUrl)
        .append("/")
        .append(UUID.randomUUID())
        .append("</span>), with any email.");

    html.append("<hr/>");
    html.append("<h1>Hello，此地址是 Jrebel & JetBrains License Server!</h1>");
    html.append("<p>JetBrains 许可服务器激活地址 ").append(licenseUrl);
    html.append("<p>JetBrains 激活地址是：<span style='color:red'>").append(licenseUrl).append("/");
    html.append("<p>JRebel 7.1 及旧版本激活地址：<span style='color:red'>")
        .append(licenseUrl)
        .append("/{tokenname}")
        .append("</span>, 以及任意邮箱地址。");
    html.append("<p>JRebel 2018.1+ 版本激活地址：")
        .append(licenseUrl)
        .append("/{guid}")
        .append("(例如：<span style='color:red'>")
        .append(licenseUrl)
        .append("/")
        .append(UUID.randomUUID())
        .append("</span>, 以及任意邮箱地址。");

    response.getWriter().println(html);
  }

  @RequestMapping({"/jrebel/leases", "/agent/leases"})
  @ResponseBody
  public void jrebelLeases(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    jrebelLeasesHandler(request, response);
  }

  @RequestMapping({"/jrebel/leases/1", "/agent/leases/1"})
  @ResponseBody
  public void jrebelLeases1(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    jrebelLeases1Handler(request, response);
  }

  @RequestMapping("/jrebel/validate-connection")
  @ResponseBody
  public void jrebelValidate(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json; charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);

    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("serverVersion", "3.2.4");
    responseMap.put("serverProtocolVersion", "1.1");
    responseMap.put("serverGuid", "a1b4aea8-b031-4302-b602-670a990272cb");
    responseMap.put("groupType", "managed");
    responseMap.put("statusCode", "SUCCESS");
    responseMap.put("company", "Administrator");
    responseMap.put("canGetLease", true);
    responseMap.put("licenseType", 1);
    responseMap.put("evaluationLicense", false);
    responseMap.put("seatPoolType", "standalone");

    response.getWriter().print(objectMapper.writeValueAsString(responseMap));
  }

  @RequestMapping("/rpc/ping.action")
  @ResponseBody
  public void ping(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html; charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);

    var salt = request.getParameter("salt");
    if (salt == null) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    var xmlContent =
        "<PingResponse><message></message><responseCode>OK</responseCode><salt>"
            + salt
            + "</salt></PingResponse>";
    var xmlSignature = rsaSignService.sign(xmlContent);
    var body = "<!-- " + xmlSignature + " -->\n" + xmlContent;
    response.getWriter().print(body);
  }

  @RequestMapping("/rpc/obtainTicket.action")
  @ResponseBody
  public void obtainTicket(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html; charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);

    var fm = new SimpleDateFormat("EEE,d MMM yyyy hh:mm:ss Z", Locale.ENGLISH);
    var date = fm.format(new Date()) + " GMT";
    var salt = request.getParameter("salt");
    var username = request.getParameter("userName");
    var prolongationPeriod = "607875500";

    if (salt == null || username == null) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    var xmlContent =
        "<ObtainTicketResponse><message></message><prolongationPeriod>"
            + prolongationPeriod
            + "</prolongationPeriod><responseCode>OK</responseCode><salt>"
            + salt
            + "</salt><ticketId>1</ticketId><ticketProperties>licensee="
            + username
            + "\tlicenseType=0\t</ticketProperties></ObtainTicketResponse>";

    var xmlSignature = rsaSignService.sign(xmlContent);
    var body = "<!-- " + xmlSignature + " -->\n" + xmlContent;
    response.getWriter().print(body);
  }

  @RequestMapping("/rpc/releaseTicket.action")
  @ResponseBody
  public void releaseTicket(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html; charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);

    var salt = request.getParameter("salt");
    if (salt == null) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    var xmlContent =
        "<ReleaseTicketResponse><message></message><responseCode>OK</responseCode><salt>"
            + salt
            + "</salt></ReleaseTicketResponse>";
    var xmlSignature = rsaSignService.sign(xmlContent);
    var body = "<!-- " + xmlSignature + " -->\n" + xmlContent;
    response.getWriter().print(body);
  }

  private void jrebelLeases1Handler(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json; charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);

    var username = request.getParameter("username");
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("serverVersion", "3.2.4");
    responseMap.put("serverProtocolVersion", "1.1");
    responseMap.put("serverGuid", "a1b4aea8-b031-4302-b602-670a990272cb");
    responseMap.put("groupType", "managed");
    responseMap.put("statusCode", "SUCCESS");
    responseMap.put("msg", null);
    responseMap.put("statusMessage", null);

    if (username != null) {
      responseMap.put("company", username);
    }
    response.getWriter().print(objectMapper.writeValueAsString(responseMap));
  }

  private void jrebelLeasesHandler(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json; charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);

    var clientRandomness = request.getParameter("randomness");
    var username = request.getParameter("username");
    var guid = request.getParameter("guid");
    var offline = Boolean.parseBoolean(request.getParameter("offline"));

    String validFrom = "null";
    String validUntil = "null";

    if (offline) {
      var clientTime = request.getParameter("clientTime");
      var offlineDays = request.getParameter("offlineDays");
      var clinetTimeUntil = Long.parseLong(clientTime) + 180L * 24 * 60 * 60 * 1000;
      validFrom = clientTime;
      validUntil = String.valueOf(clinetTimeUntil);
    }

    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("serverVersion", "3.2.4");
    responseMap.put("serverProtocolVersion", "1.1");
    responseMap.put("serverGuid", "a1b4aea8-b031-4302-b602-670a990272cb");
    responseMap.put("groupType", "managed");
    responseMap.put("id", 1);
    responseMap.put("licenseType", 1);
    responseMap.put("evaluationLicense", false);
    responseMap.put(
        "signature",
        "OJE9wGg2xncSb+VgnYT+9HGCFaLOk28tneMFhCbpVMKoC/Iq4LuaDKPirBjG4o394/UjCDGgTBpIrzcXNPdVxVr8PnQzpy7ZSToGO8wv/KIWZT9/ba7bDbA8/RZ4B37YkCeXhjaixpmoyz/CIZMnei4q7oWR7DYUOlOcEWDQhiY=");
    responseMap.put("serverRandomness", "H2ulzLlh7E0=");
    responseMap.put("seatPoolType", "standalone");
    responseMap.put("statusCode", "SUCCESS");
    responseMap.put("offline", offline);
    responseMap.put("validFrom", validFrom);
    responseMap.put("validUntil", validUntil);
    responseMap.put("company", "Administrator");
    responseMap.put("orderId", "");
    responseMap.put("zeroIds", new java.util.ArrayList<>());
    responseMap.put("licenseValidFrom", 1490544001000L);
    responseMap.put("licenseValidUntil", 1691839999000L);

    if (clientRandomness == null || username == null || guid == null) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    jrebelSign.toLeaseCreateJson(clientRandomness, guid, offline, validFrom, validUntil);
    var signature = jrebelSign.getSignature();
    responseMap.put("signature", signature);
    responseMap.put("company", username);

    response.getWriter().print(objectMapper.writeValueAsString(responseMap));
  }
}
