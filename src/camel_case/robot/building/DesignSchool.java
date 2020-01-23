package camel_case.robot.building;

import battlecode.common.*;

public class DesignSchool extends Building {
  private MapLocation hq;

  private int landscapersSpawned = 0;
  private int requiredInRingOne = -1;

  public DesignSchool(RobotController rc) {
    super(rc, RobotType.DESIGN_SCHOOL);
  }

  @Override
  public void run() throws GameActionException {
    if (hq == null) {
      hq = senseOwnRobot(RobotType.HQ);
    }

    if (hq == null) {
      return;
    }

    if (requiredInRingOne == -1) {
      requiredInRingOne = getAvailableLocationsAround(hq).size();
    }

    if (!rc.isReady()) return;

    if (!orders.isEmpty()) {
      return;
    }

    if (!isLocationSurrounded(hq)) {
      if (landscapersSpawned < requiredInRingOne) {
        if (trySpawnLandscaperTowards(hq)) {
          landscapersSpawned++;
        }
      }
    }
  }

  private boolean trySpawnLandscaperTowards(MapLocation location) throws GameActionException {
    Direction forward = directionTowards(location);

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
