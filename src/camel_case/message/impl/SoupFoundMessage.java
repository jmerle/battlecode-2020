package camel_case.message.impl;

import battlecode.common.MapLocation;
import camel_case.message.Message;
import camel_case.message.MessageType;

public class SoupFoundMessage extends Message {
  private MapLocation location;

  public SoupFoundMessage(MapLocation location) {
    super(MessageType.SOUP_FOUND, 1);

    this.location = location;
  }

  public SoupFoundMessage(int[] data, int start) {
    this(intToMapLocation(data[start]));
  }

  @Override
  public void write(int[] data, int start) {
    data[start] = mapLocationToInt(location);
  }

  public MapLocation getLocation() {
    return location;
  }
}
