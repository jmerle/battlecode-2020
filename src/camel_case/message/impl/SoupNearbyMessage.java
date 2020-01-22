package camel_case.message.impl;

import battlecode.common.MapLocation;
import camel_case.message.MessageData;
import camel_case.message.MessageType;

public class SoupNearbyMessage extends MapLocationMessage {
  public SoupNearbyMessage(MapLocation location) {
    super(MessageType.SOUP_NEARBY, location);
  }

  public SoupNearbyMessage(MessageData data) {
    super(MessageType.SOUP_NEARBY, data);
  }
}
