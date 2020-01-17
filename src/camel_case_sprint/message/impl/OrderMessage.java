package camel_case_sprint.message.impl;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import camel_case_sprint.message.Message;
import camel_case_sprint.message.MessageData;
import camel_case_sprint.message.MessageType;

public class OrderMessage extends Message {
  private static final RobotType[] robotTypes = RobotType.values();

  private int id;
  private RobotType robotType;
  private MapLocation location;

  public OrderMessage(int id, RobotType robotType, MapLocation location) {
    super(MessageType.ORDER, 3);

    this.id = id;
    this.robotType = robotType;
    this.location = location;
  }

  public OrderMessage(MessageData data) {
    this(data.readInt(), robotTypes[data.readInt()], data.readLocation());
  }

  @Override
  public void write(MessageData data) {
    data.writeInt(id);
    data.writeInt(robotType.ordinal());
    data.writeLocation(location);
  }

  public int getId() {
    return id;
  }

  public RobotType getRobotType() {
    return robotType;
  }

  public MapLocation getLocation() {
    return location;
  }
}
