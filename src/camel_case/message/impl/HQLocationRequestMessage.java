package camel_case.message.impl;

import camel_case.message.Message;
import camel_case.message.MessageType;

public class HQLocationRequestMessage extends Message {
  public HQLocationRequestMessage() {
    super(MessageType.HQ_LOCATION_REQUEST, 0);
  }

  @Override
  public void write(int[] data, int start) {
    // Do nothing
  }
}
