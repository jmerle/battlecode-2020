package camel_case_sprint.message.impl;

import battlecode.common.MapLocation;
import camel_case_sprint.message.MessageData;
import camel_case_sprint.message.MessageType;

public class SoupFoundMessage extends MapLocationMessage {
  public SoupFoundMessage(MapLocation location) {
    super(MessageType.SOUP_FOUND, location);
  }

  public SoupFoundMessage(MessageData data) {
    super(MessageType.SOUP_FOUND, data);
  }
}
