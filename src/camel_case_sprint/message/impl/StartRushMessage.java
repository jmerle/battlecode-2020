package camel_case_sprint.message.impl;

import camel_case_sprint.message.Message;
import camel_case_sprint.message.MessageData;
import camel_case_sprint.message.MessageType;

public class StartRushMessage extends Message {
  private int robotId;

  public StartRushMessage(int robotId) {
    super(MessageType.START_RUSH, 1);

    this.robotId = robotId;
  }

  public StartRushMessage(MessageData data) {
    this(data.readInt());
  }

  @Override
  public void write(MessageData data) {
    data.writeInt(robotId);
  }

  public int getRobotId() {
    return robotId;
  }
}
