package camel_case.robot;

import battlecode.common.*;
import camel_case.message.Message;
import camel_case.message.MessageDispatcher;
import camel_case.message.impl.*;
import camel_case.util.Color;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

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

  protected PriorityQueue<OrderMessage> orders =
      new PriorityQueue<>(Comparator.comparingInt(OrderMessage::getId));

  public Robot(RobotController rc, RobotType type) {
    this.rc = rc;

    me = type;
    messageDispatcher = new MessageDispatcher(rc, this);

    myTeam = rc.getTeam();
    enemyTeam = myTeam.opponent();
  }

  public abstract void run() throws GameActionException;

  public void onMessage(SoupNearbyMessage message) {
    // Let implementations override this
  }

  public void onMessage(OrderMessage message) {
    removeOrder(message.getId());
    orders.add(message);
  }

  public void onMessage(OrderCompletedMessage message) {
    removeOrder(message.getId());
  }

  public void onMessage(OrderCanceledMessage message) {
    removeOrder(message.getId());
  }

  public void onMessage(AllMinersSpawnedMessage message) {
    // Let implementations override this
  }

  protected boolean tryBuildRobot(RobotType type, Direction direction) throws GameActionException {
    if (rc.canBuildRobot(type, direction)) {
      rc.buildRobot(type, direction);
      return true;
    }

    return false;
  }

  protected OrderMessage getOrder(int id) {
    for (OrderMessage order : orders) {
      if (order.getId() == id) {
        return order;
      }
    }

    return null;
  }

  protected void removeOrder(int id) {
    Iterator<OrderMessage> it = orders.iterator();

    while (it.hasNext()) {
      OrderMessage order = it.next();

      if (order.getId() == id) {
        it.remove();
        return;
      }
    }
  }

  protected boolean canDispatchOrderAt(
      MapLocation location, MapLocation hq, int maxElevationDifference) throws GameActionException {
    if (!isOnTheMap(location)) {
      return false;
    }

    if (hq != null && hq.distanceSquaredTo(location) <= 4) {
      return false;
    }

    if (rc.senseFlooding(location)) {
      return false;
    }

    int myElevation = rc.senseElevation(rc.getLocation());
    int orderElevation = rc.senseElevation(location);

    if (Math.abs(myElevation - orderElevation) > maxElevationDifference) {
      return false;
    }

    RobotInfo robot = rc.senseRobotAtLocation(location);

    if (robot == null) {
      return true;
    }

    if (robot.getTeam() == enemyTeam) {
      return false;
    }

    return !robot.getType().isBuilding();
  }

  protected MapLocation senseHQ() {
    for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
      if (robot.getType() == RobotType.HQ) {
        return robot.getLocation();
      }
    }

    return null;
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
      return Direction.SOUTH;
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

  protected int stepsTo(MapLocation from, MapLocation to) {
    int dx = Math.abs(from.x - to.x);
    int dy = Math.abs(from.y - to.y);
    return (dx + dy) - Math.min(dx, dy);
  }

  protected int stepsTo(MapLocation to) {
    return stepsTo(rc.getLocation(), to);
  }

  protected boolean isOnTheMap(MapLocation location) {
    return location.x >= 0
        && location.x < rc.getMapWidth()
        && location.y >= 0
        && location.y < rc.getMapHeight();
  }

  protected MapLocation getClosestLocation(Iterable<MapLocation> locations) {
    MapLocation bestLocation = null;
    int bestDistance = Integer.MAX_VALUE;

    MapLocation myLocation = rc.getLocation();

    for (MapLocation location : locations) {
      int distance = myLocation.distanceSquaredTo(location);

      if (distance < bestDistance) {
        bestLocation = location;
        bestDistance = distance;
      }
    }

    return bestLocation;
  }

  protected void drawLine(MapLocation from, MapLocation to, Color color) {
    rc.setIndicatorLine(from, to, color.getR(), color.getG(), color.getB());
  }

  protected void drawLine(MapLocation to, Color color) {
    drawLine(rc.getLocation(), to, color);
  }

  protected void drawDot(MapLocation location, Color color) {
    rc.setIndicatorDot(location, color.getR(), color.getG(), color.getB());
  }

  public void dispatchMessage(Message message) {
    messageDispatcher.addToBatch(message);
  }

  public MessageDispatcher getMessageDispatcher() {
    return messageDispatcher;
  }
}
