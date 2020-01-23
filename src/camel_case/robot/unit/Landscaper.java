package camel_case.robot.unit;

import battlecode.common.*;
import camel_case.util.Color;

import java.util.List;

public class Landscaper extends Unit {
  private MapLocation hq;
  private MapLocation designSchool;

  public Landscaper(RobotController rc) {
    super(rc, RobotType.LANDSCAPER);
  }

  @Override
  public void run() throws GameActionException {
    if (hq == null) {
      hq = senseOwnRobot(RobotType.HQ);
    }

    if (designSchool == null) {
      designSchool = senseOwnRobot(RobotType.DESIGN_SCHOOL);
    }

    if (!rc.isReady()) return;

    if (isAdjacentTo(hq)) {
      if (tryDigDirt(directionTowards(hq))) {
        return;
      }
    }

    if (isAdjacentTo(hq)) {
      if (tryMoveAwayFromDesignSchool()) {
        return;
      }
    } else {
      List<MapLocation> availableRingOneLocations = getAvailableLocationsAround(hq);
      if (!availableRingOneLocations.isEmpty()) {
        tryMoveTo(availableRingOneLocations.get(0));
      }

      return;
    }

    if (rc.getDirtCarrying() > 0) {
      tryDepositDirt();
      return;
    }

    tryDigDirt();
  }

  private boolean tryDigDirt(Direction direction) throws GameActionException {
    if (rc.canDigDirt(direction)) {
      drawLine(rc.adjacentLocation(direction), Color.GREEN);
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

  private boolean tryMoveAwayFromDesignSchool() throws GameActionException {
    Direction bestDirection = null;
    int bestDistance = rc.getLocation().distanceSquaredTo(designSchool);

    for (Direction direction : adjacentDirections) {
      MapLocation location = hq.add(direction);

      if (rc.canSenseLocation(location) && rc.senseRobotAtLocation(location) == null) {
        int distance = hq.add(direction).distanceSquaredTo(designSchool);

        if (distance > bestDistance) {
          bestDirection = direction;
          bestDistance = distance;
        }
      }
    }

    return bestDirection != null && tryMoveTo(hq.add(bestDirection));
  }

  private boolean tryDepositDirt(Direction direction) throws GameActionException {
    if (rc.canDepositDirt(direction)) {
      if (direction == Direction.CENTER) {
        drawDot(rc.getLocation(), Color.BLUE);
      } else {
        drawLine(rc.adjacentLocation(direction), Color.BLUE);
      }

      rc.depositDirt(direction);
      return true;
    }

    return false;
  }

  private void tryDepositDirt() throws GameActionException {
    Direction bestDirection = Direction.CENTER;
    int bestElevation = rc.senseElevation(rc.getLocation());

    int myStepsToHQ = stepsTo(hq);

    for (Direction direction : adjacentDirections) {
      MapLocation location = rc.adjacentLocation(direction);

      RobotInfo robot = rc.senseRobotAtLocation(location);
      if (robot != null && robot.getTeam() == enemyTeam && robot.getType().isBuilding()) {
        tryDepositDirt(direction);
        return;
      }

      if (stepsTo(location, hq) != myStepsToHQ) {
        continue;
      }

      int elevation = rc.senseElevation(location);
      if (elevation < bestElevation) {
        bestDirection = direction;
        bestElevation = elevation;
      }
    }

    tryDepositDirt(bestDirection);
  }

  @Override
  protected boolean tryMoveTo(MapLocation target) throws GameActionException {
    if (!isAdjacentTo(target)) {
      return super.tryMoveTo(target);
    }

    Direction forward = directionTowards(target);

    if (tryMove(forward)) {
      return true;
    }

    if (rc.isLocationOccupied(target)) {
      return false;
    }

    if (tryDigDirt(forward)) {
      return true;
    }

    return tryDepositDirt(Direction.CENTER);
  }
}
