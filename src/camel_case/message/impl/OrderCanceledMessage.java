package camel_case.message.impl;

import camel_case.message.Message;
import camel_case.message.MessageData;
import camel_case.message.MessageType;

public class OrderCanceledMessage extends Message {
  private int id;

  public OrderCanceledMessage(int id) {
    super(MessageType.ORDER_CANCELED, 1);

    this.id = id;
  }

  public OrderCanceledMessage(MessageData data) {
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
