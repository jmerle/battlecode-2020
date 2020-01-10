package camel_case.message;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

public class MessageData {
  private int[] data;
  private int currentIndex;

  public MessageData(int[] data) {
    this.data = data;
    currentIndex = 0;
  }

  public MessageData(int hash) {
    data = new int[GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH];
    data[0] = hash;
    currentIndex = 1;
  }

  public int readInt() {
    return data[currentIndex++];
  }

  public void writeInt(int value) {
    data[currentIndex++] = value;
  }

  public MapLocation readLocation() {
    int value = readInt();
    int x = value / GameConstants.MAP_MAX_WIDTH;
    int y = value % GameConstants.MAP_MAX_WIDTH;
    return new MapLocation(x, y);
  }

  public void writeLocation(MapLocation location) {
    writeInt(location.x * GameConstants.MAP_MAX_WIDTH + location.y);
  }

  public boolean hasValuesLeft() {
    return currentIndex < data.length;
  }

  public boolean hasSpace(int cells) {
    return currentIndex + cells < data.length;
  }

  public int[] getData() {
    return data;
  }
}
