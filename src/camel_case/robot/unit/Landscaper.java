package camel_case.robot.unit;

import battlecode.common.*;
import camel_case.message.impl.HQLocationMessage;
import camel_case.message.impl.HQLocationRequestMessage;

public class Landscaper extends Unit {
  private MapLocation hq = null;
  private MapLocation campSpot = null;

  private boolean hqLocationRequested = false;

  public Landscaper(RobotController rc) {
    super(rc, RobotType.LANDSCAPER);
  }

  @Override
  public void run() throws GameActionException {
    if (hq == null) {
      senseHQ();
    }

    if (hq == null) {
      if (!hqLocationRequested) {
        dispatchMessage(new HQLocationRequestMessage());
      }

      return;
    }

    if (campSpot == null) {
      chooseCampSpot();
    }

    if (!rc.isReady()) return;

    if (campSpot == null) {
      tryMoveTo(hq);
      return;
    }

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

  @Override
  public void onMessage(HQLocationMessage message) {
    hq = message.getLocation();
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

  private void chooseCampSpot() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (!rc.canSenseLocation(hq.add(direction))) {
        return;
      }
    }

    for (Direction direction : adjacentDirections) {
      MapLocation potentialLocation = hq.add(direction);

      if (rc.senseRobotAtLocation(potentialLocation) == null) {
        campSpot = potentialLocation;
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
