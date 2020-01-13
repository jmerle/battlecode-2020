package camel_case.robot.building;

import battlecode.common.*;
import camel_case.robot.Robot;

public abstract class Building extends Robot {
  public Building(RobotController rc, RobotType type) {
    super(rc, type);
  }

  protected boolean tryShootEnemyDrone() throws GameActionException {
    RobotInfo[] nearbyEnemies =
        rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, enemyTeam);

    for (RobotInfo robotInfo : nearbyEnemies) {
      int robotId = robotInfo.getID();

      if (rc.canShootUnit(robotId)) {
        rc.shootUnit(robotId);
        return true;
      }
    }

    return false;
  }
}
