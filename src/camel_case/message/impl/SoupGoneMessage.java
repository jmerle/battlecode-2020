package camel_case.message.impl;

import battlecode.common.MapLocation;
import camel_case.message.MessageType;

public class SoupGoneMessage extends MapLocationMessage {
  public SoupGoneMessage(MapLocation location) {
    super(MessageType.SOUP_GONE, location);
  }

  public SoupGoneMessage(int[] data, int start) {
    super(MessageType.SOUP_GONE, data, start);
  }
}
