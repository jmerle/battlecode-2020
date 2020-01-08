package camel_case.message.impl;

import battlecode.common.MapLocation;
import camel_case.message.MessageType;

public class SoupFoundMessage extends MapLocationMessage {
  public SoupFoundMessage(MapLocation location) {
    super(MessageType.SOUP_FOUND, location);
  }

  public SoupFoundMessage(int[] data, int start) {
    super(MessageType.SOUP_FOUND, data, start);
  }
}
