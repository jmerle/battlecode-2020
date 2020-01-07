package camel_case.robot.building;

import battlecode.common.*;

public class HQ extends Building {
  public HQ(RobotController rc) {
    super(rc, RobotType.HQ);
  }

  @Override
  public void run() throws GameActionException {
    // TODO: Implement smart base building logic
    // TODO: Periodically send HQ location and base building data to other robots

    int sensorRadius = (int) Math.ceil(Math.sqrt(me.sensorRadiusSquared));
    MapLocation myLocation = rc.getLocation();

    for (int y = -sensorRadius; y < sensorRadius; y++) {
      for (int x = -sensorRadius; x < sensorRadius; x++) {
        /*MapLocation location = new MapLocation(myLocation.x + x, myLocation.y + y);

        if (rc.canSenseLocation(location)) {
          rc.setIndicatorDot(location, 255, 0, 0);
        }*/
      }
    }

    if (!rc.isReady()) return;

    // TODO: Implement intelligent spawning

    for (Direction direction : adjacentDirections) {
      if (tryBuildRobot(RobotType.MINER, direction)) {
        return;
      }
    }

    tryShootEnemyDrone();
  }
}
