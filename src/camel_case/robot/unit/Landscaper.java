package camel_case.robot.unit;

import battlecode.common.*;

public class Landscaper extends Unit {
  private MapLocation hq;
  private MapLocation campSpot;

  public Landscaper(RobotController rc) {
    super(rc, RobotType.LANDSCAPER);
  }

  @Override
  public void run() throws GameActionException {
    if (hq == null) {
      senseHQ();
    }

    if (campSpot == null) {
      // TODO: Receive camp spot from HQ
      return;
    }

    if (!rc.isReady()) return;

    if (!campSpot.equals(rc.getLocation())) {
      tryMoveTo(campSpot);
      return;
    }

    if (tryDigDirt(directionTowards(hq))) return;
    if (tryDepositDirt(Direction.CENTER)) return;

    for (Direction direction : adjacentDirections) {
      if (hq.isAdjacentTo(rc.getLocation().add(direction))) {
        continue;
      }

      if (tryDigDirt(direction)) {
        return;
      }
    }
  }

  private void senseHQ() {
    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, myTeam);

    for (RobotInfo robotInfo : nearbyRobots) {
      if (robotInfo.getType() == RobotType.HQ) {
        hq = robotInfo.getLocation();
        return;
      }
    }
  }

  private boolean tryDigDirt(Direction direction) throws GameActionException {
    if (rc.canDigDirt(direction)) {
      rc.digDirt(direction);
      return true;
    }

    return false;
  }

  private boolean tryDepositDirt(Direction direction) throws GameActionException {
    if (rc.canDepositDirt(direction)) {
      rc.depositDirt(direction);
      return true;
    }

    return false;
  }
}
