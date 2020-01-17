package camel_case_sprint.message.impl;

import battlecode.common.MapLocation;
import camel_case_sprint.message.Message;
import camel_case_sprint.message.MessageData;
import camel_case_sprint.message.MessageType;

public class MapLocationMessage extends Message {
  private MapLocation location;

  public MapLocationMessage(MessageType type, MapLocation location) {
    super(type, 1);

    this.location = location;
  }

  public MapLocationMessage(MessageType type, MessageData data) {
    this(type, data.readLocation());
  }

  @Override
  public void write(MessageData data) {
    data.writeLocation(location);
  }

  public MapLocation getLocation() {
    return location;
  }
}
