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
    // TODO: Implement base building logic

    if (!rc.isReady()) return;

    if (tryShootEnemyDrone()) {
      return;
    }

    if (minersSpawned < 10) {
      trySpawnMiner();
      return;
    }
  }

  private void trySpawnMiner() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (tryBuildRobot(RobotType.MINER, direction)) {
        minersSpawned++;
        return;
      }
    }
  }
}
