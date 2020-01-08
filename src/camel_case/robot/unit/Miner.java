package camel_case.robot.unit;

import battlecode.common.*;
import camel_case.message.impl.SoupFoundMessage;
import camel_case.message.impl.SoupGoneMessage;

import java.util.HashSet;
import java.util.Set;

public class Miner extends Unit {
  private MapLocation hq;

  private int soupFoundMessageCooldown = 0;
  private Set<MapLocation> soupLocations = new HashSet<>(16);

  public Miner(RobotController rc) {
    super(rc, RobotType.MINER);
  }

  @Override
  public void run() throws GameActionException {
    if (hq == null) {
      senseHQ();
    }

    soupLocations.remove(rc.getLocation());

    if (soupFoundMessageCooldown > 0) {
      soupFoundMessageCooldown--;
    }

    int sensorRadius = (int) Math.sqrt(me.sensorRadiusSquared + 1);
    MapLocation myLocation = rc.getLocation();

    for (int y = -sensorRadius; y < sensorRadius; y++) {
      for (int x = -sensorRadius; x < sensorRadius; x++) {
        MapLocation location = new MapLocation(myLocation.x + x, myLocation.y + y);

        if (rc.canSenseLocation(location) && rc.senseSoup(location) > 0) {
          if (soupLocations.add(location) && soupFoundMessageCooldown == 0) {
            dispatchMessage(new SoupFoundMessage(location));
            soupFoundMessageCooldown = 100;
          }
        }
      }
    }

    if (!rc.isReady()) return;

    if (rc.getSoupCarrying() == me.soupLimit) {
      deliverSoup();
    } else {
      findAndMineSoup();
    }
  }

  @Override
  public void onMessage(SoupFoundMessage message) {
    soupLocations.add(message.getLocation());
  }

  @Override
  public void onMessage(SoupGoneMessage message) {
    soupLocations.remove(message.getLocation());
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

  private void deliverSoup() throws GameActionException {
    if (hq == null) {
      tryMoveRandom();
      return;
    }

    if (tryRefine(directionTowards(rc.getLocation(), hq))) {
      return;
    }

    tryMoveTo(hq);
  }

  private void findAndMineSoup() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (tryMine(direction)) {
        MapLocation soupLocation = rc.adjacentLocation(direction);

        if (rc.senseSoup(soupLocation) == 0) {
          dispatchMessage(new SoupGoneMessage(soupLocation));
        } else if (soupLocations.add(soupLocation) && soupFoundMessageCooldown == 0) {
          dispatchMessage(new SoupFoundMessage(soupLocation));
          soupFoundMessageCooldown = 100;
        }

        return;
      }
    }

    MapLocation nearestLocation = null;
    int nearestDistance = Integer.MAX_VALUE;

    for (MapLocation interestingLocation : soupLocations) {
      int distance = rc.getLocation().distanceSquaredTo(interestingLocation);
      if (distance < nearestDistance) {
        nearestLocation = interestingLocation;
        nearestDistance = distance;
      }
    }

    if (nearestLocation != null && tryMoveTo(nearestLocation)) {
      return;
    }

    tryMoveRandom();
  }

  private boolean tryMine(Direction direction) throws GameActionException {
    if (rc.canMineSoup(direction)) {
      rc.mineSoup(direction);
      return true;
    }

    return false;
  }

  private boolean tryRefine(Direction direction) throws GameActionException {
    if (rc.canDepositSoup(direction)) {
      rc.depositSoup(direction, rc.getSoupCarrying());
      return true;
    }

    return false;
  }
}
