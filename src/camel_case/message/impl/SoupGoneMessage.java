package camel_case.message.impl;

import battlecode.common.MapLocation;
import camel_case.message.MessageData;
import camel_case.message.MessageType;

public class SoupGoneMessage extends MapLocationMessage {
  public SoupGoneMessage(MapLocation location) {
    super(MessageType.SOUP_GONE, location);
  }

  public SoupGoneMessage(MessageData data) {
    super(MessageType.SOUP_GONE, data);
  }
}
