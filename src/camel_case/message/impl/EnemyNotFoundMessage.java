package camel_case.message.impl;

import camel_case.message.Message;
import camel_case.message.MessageData;
import camel_case.message.MessageType;

public class EnemyNotFoundMessage extends Message {
  public EnemyNotFoundMessage() {
    super(MessageType.ENEMY_NOT_FOUND, 1);
  }

  public EnemyNotFoundMessage(MessageData data) {
    this();
  }

  @Override
  public void write(MessageData data) {
    // Do nothing
  }
}
