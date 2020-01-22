package camel_case.robot;

import battlecode.common.*;
import camel_case.message.Message;
import camel_case.message.MessageDispatcher;
import camel_case.message.impl.OrderMessage;
import camel_case.message.impl.RemoveOrderMessage;
import camel_case.message.impl.SoupNearbyMessage;
import camel_case.util.Color;

import java.util.Comparator;
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
    orders.add(message);
  }

  public void onMessage(RemoveOrderMessage message) {
    orders.removeIf(order -> order.getId() == message.getId());
  }

  protected boolean tryBuildRobot(RobotType type, Direction direction) throws GameActionException {
    if (rc.canBuildRobot(type, direction)) {
      rc.buildRobot(type, direction);
      return true;
    }

    return false;
  }

  protected boolean tryCompleteOrder() throws GameActionException {
    OrderMessage order = orders.peek();

    if (order == null) {
      return false;
    }

    if (order.getRobotType().cost > rc.getTeamSoup()) {
      return false;
    }

    MapLocation orderLocation = order.getLocation();

    if (!isAdjacentTo(orderLocation)) {
      return false;
    }

    if (tryBuildRobot(order.getRobotType(), directionTowards(orderLocation))) {
      dispatchMessage(new RemoveOrderMessage(order.getId()));
      return true;
    } else {
      RobotInfo blockingRobot = rc.senseRobotAtLocation(orderLocation);

      if (blockingRobot.getTeam() == enemyTeam) {
        dispatchMessage(new RemoveOrderMessage(order.getId()));
      }
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
