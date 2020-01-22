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

    Arrays.sort(
        nearbyEnemies,
        Comparator.comparingInt(
            robot -> {
              int distance = rc.getLocation().distanceSquaredTo(robot.getLocation());

              if (robot.isCurrentlyHoldingUnit()) {
                return -1000 - distance;
              }

              return -distance;
            }));

    for (RobotInfo robotInfo : nearbyEnemies) {
      if (tryShootUnit(robotInfo.getID())) {
        return true;
      }
    }

    return false;
  }
}
