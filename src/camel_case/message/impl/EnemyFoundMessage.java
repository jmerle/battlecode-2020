package camel_case.message.impl;

import battlecode.common.MapLocation;
import camel_case.message.MessageData;
import camel_case.message.MessageType;

public class EnemyFoundMessage extends MapLocationMessage {
  public EnemyFoundMessage(MapLocation location) {
    super(MessageType.ENEMY_FOUND, location);
  }

  public EnemyFoundMessage(MessageData data) {
    super(MessageType.ENEMY_FOUND, data);
  }
}
