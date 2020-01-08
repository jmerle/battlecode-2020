package camel_case.message.impl;

import battlecode.common.MapLocation;
import camel_case.message.Message;
import camel_case.message.MessageType;

public class MapLocationMessage extends Message {
  private MapLocation location;

  public MapLocationMessage(MessageType type, MapLocation location) {
    super(type, 1);

    this.location = location;
  }

  public MapLocationMessage(MessageType type, int[] data, int start) {
    this(type, intToMapLocation(data[start]));
  }

  @Override
  public void write(int[] data, int start) {
    data[start] = mapLocationToInt(location);
  }

  public MapLocation getLocation() {
    return location;
  }
}
