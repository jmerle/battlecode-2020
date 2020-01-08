package camel_case.robot;

import battlecode.common.*;
import camel_case.message.Message;
import camel_case.message.MessageDispatcher;
import camel_case.message.impl.SoupFoundMessage;
import camel_case.message.impl.SoupGoneMessage;

import java.util.Random;

public abstract class Robot {
  protected RobotController rc;

  protected RobotType me;
  protected MessageDispatcher messageDispatcher;

  protected Team myTeam;
  protected Team enemyTeam;

  protected Direction[] adjacentDirections = {
    Direction.NORTH,
    Direction.NORTHEAST,
    Direction.EAST,
    Direction.SOUTHEAST,
    Direction.SOUTH,
    Direction.SOUTHWEST,
    Direction.WEST,
    Direction.NORTHWEST
  };

  protected Random random = new Random();

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

  protected Direction randomAdjacentDirection() {
    return adjacentDirections[random.nextInt(adjacentDirections.length)];
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

  public void dispatchMessage(Message message) {
    messageDispatcher.addToBatch(message);
  }

  public MessageDispatcher getMessageDispatcher() {
    return messageDispatcher;
  }
}
