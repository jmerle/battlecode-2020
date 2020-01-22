package camel_case.message.impl;

import camel_case.message.Message;
import camel_case.message.MessageData;
import camel_case.message.MessageType;

public class AllMinersSpawnedMessage extends Message {
  public AllMinersSpawnedMessage() {
    super(MessageType.ALL_MINERS_SPAWNED, 0);
  }

  public AllMinersSpawnedMessage(MessageData data) {
    this();
  }

  @Override
  public void write(MessageData data) {
    // Do nothing
  }
}
