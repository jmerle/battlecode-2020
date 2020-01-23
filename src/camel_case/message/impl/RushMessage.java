package camel_case.message.impl;

import camel_case.message.Message;
import camel_case.message.MessageData;
import camel_case.message.MessageType;

public class RushMessage extends Message {
  private int id;

  public RushMessage(int id) {
    super(MessageType.RUSH, 1);

    this.id = id;
  }

  public RushMessage(MessageData data) {
    this(data.readInt());
  }

  @Override
  public void write(MessageData data) {
    data.writeInt(id);
  }

  public int getId() {
    return id;
  }
}
