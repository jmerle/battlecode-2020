package camel_case_sprint.robot.building;

import battlecode.common.*;

public class DesignSchool extends Building {
  private MapLocation hq;
  private int requiredUnits = -1;

  public DesignSchool(RobotController rc) {
    super(rc, RobotType.DESIGN_SCHOOL);
  }

  @Override
  public void run() throws GameActionException {
    if (requiredUnits == -1) {
      initRequiredUnits();
    }

    if (!rc.isReady()) return;

    if (requiredUnits <= 0) {
      return;
    }

    tryBuildLandscaper();
  }

  private void initRequiredUnits() throws GameActionException {
    requiredUnits = 0;

    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1);
    for (RobotInfo robotInfo : nearbyRobots) {
      if (robotInfo.getType() == RobotType.HQ) {
        requiredUnits += getEmptyAdjacentLocations(robotInfo.getLocation());
      }
    }
  }

  private int getEmptyAdjacentLocations(MapLocation location) throws GameActionException {
    int emptyLocations = 0;

    for (Direction direction : adjacentDirections) {
      MapLocation adjacentLocation = location.add(direction);

      if (rc.canSenseLocation(adjacentLocation) && !rc.senseFlooding(adjacentLocation)) {
        RobotInfo robot = rc.senseRobotAtLocation(adjacentLocation);
        if (robot == null || !robot.getType().isBuilding()) {
          emptyLocations++;
        }
      } else if (!rc.canSenseLocation(adjacentLocation)) {
        emptyLocations++;
      }
    }

    return emptyLocations;
  }

  private void tryBuildLandscaper() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (tryBuildRobot(RobotType.LANDSCAPER, direction)) {
        requiredUnits--;
        return;
      }
    }
  }
}
