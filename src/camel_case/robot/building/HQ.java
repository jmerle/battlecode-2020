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
    // TODO(jmerle): Implement non-action logic

    if (!rc.isReady()) return;

    if (minersSpawned < 5) {
      if (trySpawnMiner()) {
        minersSpawned++;
      } else {
        tryShootEnemyDrone();
      }

      return;
    }

    tryShootEnemyDrone();
  }

  private boolean trySpawnMiner() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (tryBuildRobot(RobotType.MINER, direction)) {
        return true;
      }
    }

    return false;
  }
}
