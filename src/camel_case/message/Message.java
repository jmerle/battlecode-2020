package camel_case.message;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

public abstract class Message {
  private MessageType type;
  private int size;

  public Message(MessageType type, int size) {
    this.type = type;
    this.size = size;
  }

  public abstract void write(int[] data, int start);

  public MessageType getType() {
    return type;
  }

  public int getSize() {
    return size;
  }

  protected static int mapLocationToInt(MapLocation location) {
    return location.x * GameConstants.MAP_MAX_WIDTH + location.y;
  }

  protected static MapLocation intToMapLocation(int value) {
    int x = value / GameConstants.MAP_MAX_WIDTH;
    int y = value % GameConstants.MAP_MAX_WIDTH;
    return new MapLocation(x, y);
  }
}
