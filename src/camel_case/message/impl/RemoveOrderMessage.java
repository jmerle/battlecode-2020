package camel_case.message.impl;

import camel_case.message.Message;
import camel_case.message.MessageData;
import camel_case.message.MessageType;

public class RemoveOrderMessage extends Message {
  private int id;

  public RemoveOrderMessage(int id) {
    super(MessageType.REMOVE_ORDER, 1);

    this.id = id;
  }

  public RemoveOrderMessage(MessageData data) {
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
