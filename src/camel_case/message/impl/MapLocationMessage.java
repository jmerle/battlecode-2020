package camel_case.message.impl;

import battlecode.common.MapLocation;
import camel_case.message.Message;
import camel_case.message.MessageData;
import camel_case.message.MessageType;

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
