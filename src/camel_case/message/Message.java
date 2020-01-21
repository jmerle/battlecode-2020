package camel_case.message;

public abstract class Message {
  private MessageType type;
  private int size;

  public Message(MessageType type, int size) {
    this.type = type;
    this.size = size;
  }

  public abstract void write(MessageData data);

  public MessageType getType() {
    return type;
  }

  public int getSize() {
    return size;
  }
}
