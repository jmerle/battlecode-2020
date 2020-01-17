package camel_case_sprint.message.impl;

import camel_case_sprint.message.Message;
import camel_case_sprint.message.MessageData;
import camel_case_sprint.message.MessageType;

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
