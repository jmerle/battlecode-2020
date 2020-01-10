package camel_case.robot.unit;

import battlecode.common.*;
import camel_case.message.impl.SoupFoundMessage;
import camel_case.message.impl.SoupGoneMessage;
import camel_case.util.BetterRandom;

import java.util.HashSet;
import java.util.Set;

public class Miner extends Unit {
  private MapLocation hq;

  private int soupFoundMessageCooldown = 0;
  private Set<MapLocation> soupLocations = new HashSet<>(16);

  private MapLocation[] wanderTargets;
  private int currentWanderTarget = 0;
  private int wanderTargetCooldown = 0;

  public Miner(RobotController rc) {
    super(rc, RobotType.MINER);

    int mapWidth = rc.getMapWidth();
    int mapHeight = rc.getMapHeight();

    wanderTargets =
        new MapLocation[] {
          new MapLocation(0, 0),
          new MapLocation(0, mapHeight - 1),
          new MapLocation(mapWidth - 10, 0),
          new MapLocation(mapWidth - 10, mapHeight - 1),
          new MapLocation(mapWidth / 2, mapHeight / 2)
        };
  }

  @Override
  public void run() throws GameActionException {
    if (hq == null) {
      senseHQ();
    }

    if (soupFoundMessageCooldown > 0) {
      soupFoundMessageCooldown--;
    }

    senseSoupLocations();

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

  private void senseSoupLocations() throws GameActionException {
    int sensorRadius = (int) Math.sqrt(me.sensorRadiusSquared + 1);
    MapLocation myLocation = rc.getLocation();
    boolean soupFoundDispatched = false;

    for (int y = -sensorRadius; y < sensorRadius; y++) {
      for (int x = -sensorRadius; x < sensorRadius; x++) {
        MapLocation location = new MapLocation(myLocation.x + x, myLocation.y + y);

        if (rc.canSenseLocation(location) && rc.senseSoup(location) > 0) {
          if (soupLocations.add(location) && soupFoundMessageCooldown == 0) {
            dispatchMessage(new SoupFoundMessage(location));
            soupFoundDispatched = true;
          }
        }
      }
    }

    if (soupFoundDispatched) {
      soupFoundMessageCooldown = 25;
    }
  }

  private void deliverSoup() throws GameActionException {
    if (hq == null) {
      tryMoveRandom();
      return;
    }

    if (tryRefine(directionTowards(hq))) {
      return;
    }

    tryMoveTo(hq);
  }

  private void findAndMineSoup() throws GameActionException {
    if (tryMineSoup()) return;
    if (tryMoveToInterestingLocation()) return;
    if (tryWander()) return;

    tryMoveRandom();
  }

  private boolean tryMineSoup() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (tryMine(direction)) {
        MapLocation soupLocation = rc.adjacentLocation(direction);

        if (rc.senseSoup(soupLocation) == 0) {
          soupLocations.remove(soupLocation);
          dispatchMessage(new SoupGoneMessage(soupLocation));
        } else if (soupLocations.add(soupLocation) && soupFoundMessageCooldown == 0) {
          dispatchMessage(new SoupFoundMessage(soupLocation));
          soupFoundMessageCooldown = 50;
        }

        return true;
      }
    }

    return false;
  }

  private boolean tryMoveToInterestingLocation() throws GameActionException {
    MapLocation nearestLocation = null;
    int nearestDistance = Integer.MAX_VALUE;

    for (MapLocation interestingLocation : soupLocations) {
      int distance = rc.getLocation().distanceSquaredTo(interestingLocation);
      if (distance < nearestDistance) {
        nearestLocation = interestingLocation;
        nearestDistance = distance;
      }
    }

    return nearestLocation != null && tryMoveTo(nearestLocation);
  }

  private boolean tryWander() throws GameActionException {
    if (wanderTargetCooldown > 0) {
      wanderTargetCooldown--;
    }

    if (wanderTargetCooldown == 0) {
      int oldTarget = currentWanderTarget;
      int oldTargetPositive = oldTarget < 0 ? -oldTarget - 1 : oldTarget;

      while (currentWanderTarget == oldTarget || currentWanderTarget == oldTargetPositive) {
        currentWanderTarget = BetterRandom.nextInt(wanderTargets.length);
      }

      wanderTargetCooldown = 50;
    }

    if (currentWanderTarget >= 0) {
      MapLocation wanderTarget = wanderTargets[currentWanderTarget];

      if (rc.getLocation().equals(wanderTarget)) {
        currentWanderTarget = -currentWanderTarget - 1;
        return false;
      }

      return tryMoveTo(wanderTargets[currentWanderTarget]);
    }

    return false;
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
