package camel_case.robot.building;

import battlecode.common.*;
import camel_case.robot.Robot;
import camel_case.util.Color;

import java.util.Arrays;
import java.util.Comparator;

public abstract class Building extends Robot {
  public Building(RobotController rc, RobotType type) {
    super(rc, type);
  }

  protected boolean tryShootUnit(int id) throws GameActionException {
    if (rc.canShootUnit(id)) {
      drawLine(rc.senseRobot(id).getLocation(), Color.RED);
      rc.shootUnit(id);
      return true;
    }

    return false;
  }

  protected boolean tryShootEnemyDrone() throws GameActionException {
    RobotInfo[] nearbyEnemies =
        rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, enemyTeam);

    MapLocation myLocation = rc.getLocation();
    int myX = myLocation.x;
    int myY = myLocation.y;

    Arrays.sort(
        nearbyEnemies,
        Comparator.comparingInt(
            robot -> {
              int priority = 0;

              if (robot.isCurrentlyHoldingUnit()) {
                priority += 1000;
              }

              MapLocation robotLocation = robot.getLocation();

              if (robotLocation.x == myX) {
                priority += 2000;
              }

              if (robotLocation.y == myY) {
                priority += 2000;
              }

              int distance = rc.getLocation().distanceSquaredTo(robot.getLocation());
              return -priority + distance;
            }));

    for (RobotInfo robotInfo : nearbyEnemies) {
      if (tryShootUnit(robotInfo.getID())) {
        return true;
      }
    }

    return false;
  }

  protected boolean isHQSurrounded(MapLocation hq) throws GameActionException {
    for (Direction direction : adjacentDirections) {
      RobotInfo robot = rc.senseRobotAtLocation(hq.add(direction));

      if (robot == null) {
        return false;
      }

      if (robot.getTeam() == myTeam && rc.getType() != RobotType.LANDSCAPER) {
        return false;
      }
    }

    return true;
  }
}
