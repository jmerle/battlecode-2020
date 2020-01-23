package camel_case.robot.unit;

import battlecode.common.*;

public class Landscaper extends Unit {
  MapLocation hq;

  public Landscaper(RobotController rc) {
    super(rc, RobotType.LANDSCAPER);
  }

  @Override
  public void run() throws GameActionException {
    if (hq == null) {
      hq = senseHQ();
    }

    if (!rc.isReady()) return;

    if (isAdjacentTo(hq)) {
      if (tryDigDirt(directionTowards(hq))) {
        return;
      }
    }

    if (rc.getDirtCarrying() < me.dirtLimit) {
      tryDigDirt();
      return;
    }

    if (rc.getDirtCarrying() == me.dirtLimit) {
      tryDepositDirt();
      return;
    }

    tryMoveRandom();
  }

  private MapLocation senseHQ() {
    for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
      if (robot.getType() == RobotType.HQ) {
        return robot.getLocation();
      }
    }

    return null;
  }

  private boolean tryDigDirt(Direction direction) throws GameActionException {
    if (rc.canDigDirt(direction)) {
      rc.digDirt(direction);
      return true;
    }

    return false;
  }

  private void tryDigDirt() throws GameActionException {
    int myStepsToHQ = stepsTo(hq);

    for (Direction direction : adjacentDirections) {
      MapLocation location = rc.adjacentLocation(direction);

      if (stepsTo(location, hq) <= myStepsToHQ) {
        continue;
      }

      if (location.x != hq.x && location.y != hq.y) {
        continue;
      }

      if (tryDigDirt(direction)) {
        return;
      }
    }

    for (Direction direction : adjacentDirections) {
      MapLocation location = rc.adjacentLocation(direction);

      if (stepsTo(location, hq) <= myStepsToHQ) {
        continue;
      }

      if (tryDigDirt(direction)) {
        return;
      }
    }
  }

  private void tryDepositDirt() throws GameActionException {
    MapLocation myLocation = rc.getLocation();
    int myDistanceToHQ = myLocation.distanceSquaredTo(hq);

    Direction bestDirection = Direction.CENTER;
    int bestSoup = rc.senseSoup(myLocation);

    for (Direction direction : adjacentDirections) {
      MapLocation location = myLocation.add(direction);

      if (myLocation.x != location.x && myLocation.y != location.y) {
        continue;
      }

      if (location.distanceSquaredTo(hq) > myDistanceToHQ) {
        continue;
      }

      int soup = rc.senseSoup(location);
      if (soup < bestSoup && rc.canDepositSoup(direction)) {
        bestDirection = direction;
        bestSoup = soup;
      }
    }

    rc.depositDirt(bestDirection);
  }
}
