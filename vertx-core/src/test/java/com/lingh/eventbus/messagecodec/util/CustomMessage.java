package com.lingh.eventbus.messagecodec.util;

public record CustomMessage(int statusCode, String resultCode, String summary) {

  @Override
  public String toString() {
    return "CustomMessage{" + "statusCode=" + statusCode +
            ", resultCode='" + resultCode + '\'' +
            ", summary='" + summary + '\'' +
            '}';
  }
}
