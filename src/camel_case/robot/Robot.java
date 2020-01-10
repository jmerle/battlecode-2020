package camel_case.robot;

import battlecode.common.*;
import camel_case.message.Message;
import camel_case.message.MessageDispatcher;
import camel_case.message.impl.OrderCompletedMessage;
import camel_case.message.impl.OrderMessage;
import camel_case.message.impl.SoupFoundMessage;
import camel_case.message.impl.SoupGoneMessage;
import camel_case.util.BetterRandom;

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

  public void onMessage(SoupFoundMessage message) {
    // Let implementations override this
  }

  public void onMessage(SoupGoneMessage message) {
    // Let implementations override this
  }

  public void onMessage(OrderMessage message) {
    // Let implementations override this
  }

  public void onMessage(OrderCompletedMessage message) {
    // Let implementations override this
  }

  protected Direction randomAdjacentDirection() {
    return adjacentDirections[BetterRandom.nextInt(adjacentDirections.length)];
  }

  protected Direction directionTowards(MapLocation from, MapLocation to) {
    if (from.x < to.x && from.y < to.y) {
      return Direction.NORTHEAST;
    }

    if (from.x < to.x && from.y > to.y) {
      return Direction.SOUTHEAST;
    }

    if (from.x > to.x && from.y < to.y) {
      return Direction.NORTHWEST;
    }

    if (from.x > to.x && from.y > to.y) {
      return Direction.SOUTHWEST;
    }

    if (from.x < to.x) {
      return Direction.EAST;
    }

    if (from.x > to.x) {
      return Direction.WEST;
    }

    if (from.y < to.y) {
      return Direction.NORTH;
    }

    if (from.y > to.y) {
      return Direction.SOUTHWEST;
    }

    return Direction.CENTER;
  }

  protected Direction directionTowards(MapLocation to) {
    return directionTowards(rc.getLocation(), to);
  }

  public void dispatchMessage(Message message) {
    messageDispatcher.addToBatch(message);
  }

  public MessageDispatcher getMessageDispatcher() {
    return messageDispatcher;
  }
}
