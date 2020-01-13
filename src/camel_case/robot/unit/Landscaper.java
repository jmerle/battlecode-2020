package camel_case.robot.unit;

import battlecode.common.*;

public class Landscaper extends Unit {
  private RobotInfo hq;

  public Landscaper(RobotController rc) {
    super(rc, RobotType.LANDSCAPER);
  }

  @Override
  public void run() throws GameActionException {
    if (hq == null) {
      hq = senseHQ();
    }

    if (!rc.isReady()) return;

    if (hq == null) {
      tryMoveRandom();
      return;
    }

    if (!isAdjacentTo(hq.getLocation())) {
      moveToHQ();
      return;
    }

    if (rc.getDirtCarrying() == 0) {
      tryDigDirt();
      return;
    }

    if (hq.getTeam() == enemyTeam) {
      tryDepositDirt(directionTowards(hq.getLocation()));
    } else {
      tryDepositDirt(Direction.CENTER);
    }
  }

  private RobotInfo senseHQ() throws GameActionException {
    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1);

    RobotInfo myHQ = null;
    RobotInfo enemyHQ = null;

    for (RobotInfo robotInfo : nearbyRobots) {
      if (robotInfo.getType() == RobotType.HQ) {
        if (robotInfo.getTeam() == myTeam) {
          myHQ = robotInfo;
        } else {
          enemyHQ = robotInfo;
        }
      }
    }

    if (myHQ == null && enemyHQ == null) {
      return null;
    }

    if (myHQ != null && enemyHQ == null) {
      return myHQ;
    }

    //noinspection ConstantConditions
    if (myHQ == null && enemyHQ != null) {
      return enemyHQ;
    }

    for (Direction direction : adjacentDirections) {
      MapLocation targetLocation = enemyHQ.getLocation().add(direction);

      if (rc.canSenseLocation(targetLocation) && rc.senseRobotAtLocation(targetLocation) == null) {
        return enemyHQ;
      }
    }

    return myHQ;
  }

  private void moveToHQ() throws GameActionException {
    MapLocation bestLocation = null;
    int bestDistance = Integer.MAX_VALUE;

    for (Direction direction : adjacentDirections) {
      MapLocation targetLocation = hq.getLocation().add(direction);

      if (rc.canSenseLocation(targetLocation) && rc.senseRobotAtLocation(targetLocation) == null) {
        int distance = rc.getLocation().distanceSquaredTo(targetLocation);

        if (distance < bestDistance) {
          bestLocation = targetLocation;
          bestDistance = distance;
        }
      }
    }

    tryMoveTo(bestLocation != null ? bestLocation : hq.getLocation());
  }

  private void tryDigDirt() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      MapLocation location = rc.getLocation().add(direction);
      if (location.equals(hq.getLocation()) || isAdjacentTo(location, hq.getLocation())) {
        continue;
      }

      RobotInfo robot = rc.senseRobotAtLocation(location);
      if (robot != null && robot.getTeam() == myTeam) {
        continue;
      }

      if (tryDigDirt(direction)) {
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
