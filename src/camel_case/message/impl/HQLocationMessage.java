package camel_case.message.impl;

import battlecode.common.MapLocation;
import camel_case.message.MessageType;

public class HQLocationMessage extends MapLocationMessage {
  public HQLocationMessage(MapLocation location) {
    super(MessageType.HQ_LOCATION, location);
  }

  public HQLocationMessage(int[] data, int start) {
    super(MessageType.HQ_LOCATION, data, start);
  }
}
