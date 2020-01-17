package camel_case_sprint.message.impl;

import battlecode.common.MapLocation;
import camel_case_sprint.message.MessageData;
import camel_case_sprint.message.MessageType;

public class SoupGoneMessage extends MapLocationMessage {
  public SoupGoneMessage(MapLocation location) {
    super(MessageType.SOUP_GONE, location);
  }

  public SoupGoneMessage(MessageData data) {
    super(MessageType.SOUP_GONE, data);
  }
}
