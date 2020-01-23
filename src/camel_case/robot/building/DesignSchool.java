package camel_case.robot.building;

import battlecode.common.*;

public class DesignSchool extends Building {
  private MapLocation hq;

  private int landscapersSpawned = 0;

  public DesignSchool(RobotController rc) {
    super(rc, RobotType.DESIGN_SCHOOL);
  }

  @Override
  public void run() throws GameActionException {
    if (hq == null) {
      hq = senseHQ();
    }

    if (!rc.isReady()) return;

    if (!isHQSurrounded(hq)) {
      if (landscapersSpawned < 8 && trySpawnMinerTowardsHQ()) {
        landscapersSpawned++;
      }

      return;
    }
  }

  private boolean trySpawnMinerTowardsHQ() throws GameActionException {
    Direction forward = directionTowards(hq);

    if (tryBuildRobot(RobotType.LANDSCAPER, forward)) {
      return true;
    }

    for (int i = 1; i <= 3; i++) {
      Direction left = forward;
      Direction right = forward;

      for (int j = 0; j < i; j++) {
        left = left.rotateLeft();
        right = right.rotateRight();
      }

      if (tryBuildRobot(RobotType.LANDSCAPER, left)) {
        return true;
      }

      if (tryBuildRobot(RobotType.LANDSCAPER, right)) {
        return true;
      }
    }

    return tryBuildRobot(RobotType.LANDSCAPER, forward.opposite());
  }
}
