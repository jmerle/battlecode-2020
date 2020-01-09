package camel_case.robot.building;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class HQ extends Building {
  private int minersSpawned = 0;

  public HQ(RobotController rc) {
    super(rc, RobotType.HQ);
  }

  @Override
  public void run() throws GameActionException {
    // TODO: Implement smart base building logic
    // TODO: Periodically send HQ location and base building data to other robots

    if (!rc.isReady()) return;

    if (minersSpawned < 10) {
      for (Direction direction : adjacentDirections) {
        if (tryBuildRobot(RobotType.MINER, direction)) {
          minersSpawned++;
          return;
        }
      }
    }

    tryShootEnemyDrone();
  }
}
