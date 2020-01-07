package camel_case.robot.building;

import battlecode.common.*;

public class HQ extends Building {
  public HQ(RobotController rc) {
    super(rc, RobotType.HQ);
  }

  @Override
  public void run() throws GameActionException {
    RobotInfo[] nearbyEnemies =
        rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, enemyTeam);

    for (RobotInfo robotInfo : nearbyEnemies) {
      if (robotInfo.type == RobotType.DELIVERY_DRONE) {
        rc.shootUnit(robotInfo.getID());
      }
    }
  }
}
