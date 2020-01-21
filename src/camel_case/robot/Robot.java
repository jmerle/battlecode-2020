package camel_case.robot;

import battlecode.common.*;
import camel_case.message.Message;
import camel_case.message.MessageDispatcher;

public abstract class Robot {
  protected RobotController rc;

  protected RobotType me;
  protected MessageDispatcher messageDispatcher;

  protected Team myTeam;
  protected Team enemyTeam;

  protected Direction[] adjacentDirections = {
    Direction.NORTH,
    Direction.EAST,
    Direction.SOUTH,
    Direction.WEST,
    Direction.NORTHEAST,
    Direction.SOUTHEAST,
    Direction.SOUTHWEST,
    Direction.NORTHWEST
  };

  public Robot(RobotController rc, RobotType type) {
    this.rc = rc;

    me = type;
    messageDispatcher = new MessageDispatcher(rc, this);

    myTeam = rc.getTeam();
    enemyTeam = myTeam.opponent();
  }

  public abstract void run() throws GameActionException;

  protected boolean tryBuildRobot(RobotType type, Direction direction) throws GameActionException {
    if (rc.canBuildRobot(type, direction)) {
      rc.buildRobot(type, direction);
      return true;
    }

    return false;
  }

  protected Direction directionTowards(MapLocation from, MapLocation to) {
    if (from.x < to.x && from.y < to.y) {
      return Direction.NORTHEAST;
    } else if (from.x < to.x && from.y > to.y) {
      return Direction.SOUTHEAST;
    } else if (from.x > to.x && from.y < to.y) {
      return Direction.NORTHWEST;
    } else if (from.x > to.x && from.y > to.y) {
      return Direction.SOUTHWEST;
    } else if (from.x < to.x) {
      return Direction.EAST;
    } else if (from.x > to.x) {
      return Direction.WEST;
    } else if (from.y < to.y) {
      return Direction.NORTH;
    } else if (from.y > to.y) {
      return Direction.SOUTHWEST;
    } else {
      return Direction.CENTER;
    }
  }

  protected Direction directionTowards(MapLocation to) {
    return directionTowards(rc.getLocation(), to);
  }

  protected boolean isAdjacentTo(MapLocation a, MapLocation b) {
    return !a.equals(b) && a.isAdjacentTo(b);
  }

  protected boolean isAdjacentTo(MapLocation location) {
    return isAdjacentTo(rc.getLocation(), location);
  }

  protected boolean isOnTheMap(MapLocation location) {
    return location.x >= 0
        && location.x < rc.getMapWidth()
        && location.y >= 0
        && location.y < rc.getMapHeight();
  }

  public void dispatchMessage(Message message) {
    messageDispatcher.addToBatch(message);
  }

  public MessageDispatcher getMessageDispatcher() {
    return messageDispatcher;
  }
}
