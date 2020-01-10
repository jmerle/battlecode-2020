package camel_case.message.impl;

import camel_case.message.Message;
import camel_case.message.MessageData;
import camel_case.message.MessageType;

public class OrderCompletedMessage extends Message {
  private int id;

  public OrderCompletedMessage(int id) {
    super(MessageType.ORDER_COMPLETED, 1);

    this.id = id;
  }

  public OrderCompletedMessage(MessageData data) {
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
