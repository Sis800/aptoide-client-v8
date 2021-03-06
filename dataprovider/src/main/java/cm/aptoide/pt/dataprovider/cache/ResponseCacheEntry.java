/*
 * Copyright (c) 2016.
 * Modified on 28/04/2016.
 */

package cm.aptoide.pt.dataprovider.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class ResponseCacheEntry {

  private static final String DEFAULT_CHARSET = "UTF-8";

  // response data
  private int code;
  private String message;
  private String protocol;
  private String body;
  private String bodyMediaType;
  private Map<String, List<String>> headers;

  // meta data
  private long validity;

  public ResponseCacheEntry() {
  }

  public ResponseCacheEntry(Response response, int secondsToPersist) {

    this.validity = System.currentTimeMillis() + (secondsToPersist * 1000);

    this.code = response.code();
    this.message = response.message();
    this.protocol = response.protocol()
        .toString();
    this.headers = response.headers()
        .toMultimap();

    final ResponseBody responseBody = response.body();
    this.bodyMediaType = responseBody.contentType()
        .toString();

    Charset charset = Charset.forName(DEFAULT_CHARSET);
    charset = responseBody.contentType()
        .charset(charset);

    try {
      // snippet taken from https://github.com/square/okhttp/blob/master/okhttp-logging-interceptor/src/main/java/okhttp3/logging/HttpLoggingInterceptor.java
      BufferedSource source = responseBody.source();
      source.request(Long.MAX_VALUE);
      Buffer buffer = source.buffer();
      this.body = buffer.clone()
          .readString(charset);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String getDefaultCharset() {
    return DEFAULT_CHARSET;
  }

  //@JsonIgnore
  public Response getResponse(Request request) {
    Response.Builder builder = new Response.Builder();

    builder.code(code);
    builder.message(message);

    try {
      builder.protocol(Protocol.get(protocol));
    } catch (IOException e) {
      e.printStackTrace();
    }

    ResponseBody responseBody = ResponseBody.create(MediaType.parse(this.bodyMediaType), body);
    builder.body(responseBody);

    Headers.Builder headersBuilders = new Headers.Builder();
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      for (String entryValue : entry.getValue()) {
        headersBuilders.add(entry.getKey(), entryValue);
      }
    }
    builder.headers(headersBuilders.build());

    builder.request(request);

    return builder.build();
  }

  @JsonIgnore boolean isValid() {
    return System.currentTimeMillis() <= validity;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getBodyMediaType() {
    return bodyMediaType;
  }

  public void setBodyMediaType(String bodyMediaType) {
    this.bodyMediaType = bodyMediaType;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  public long getValidity() {
    return validity;
  }

  public void setValidity(long validity) {
    this.validity = validity;
  }
}
