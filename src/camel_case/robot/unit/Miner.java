package camel_case.robot.unit;

import battlecode.common.*;
import camel_case.message.impl.SoupNearbyMessage;
import camel_case.util.BetterRandom;
import camel_case.util.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Miner extends Unit {
  private List<MapLocation> dropOffLocations = new ArrayList<>();

  private Set<MapLocation> soupLocations = new HashSet<>();
  private Set<MapLocation> invalidSoupLocations = new HashSet<>();

  private Set<MapLocation> interestingLocations = new HashSet<>();
  private Set<MapLocation> invalidInterestingLocations = new HashSet<>();
  private int soupNearbyMessageCooldown = 0;

  private MapLocation[] wanderTargets;
  private int currentWanderTarget;

  public Miner(RobotController rc) {
    super(rc, RobotType.MINER);

    int mapWidth = rc.getMapWidth();
    int mapHeight = rc.getMapHeight();
    int wanderMargin = 3;

    wanderTargets =
        new MapLocation[] {
          new MapLocation(wanderMargin, wanderMargin),
          new MapLocation(mapWidth - wanderMargin, wanderMargin),
          new MapLocation(wanderMargin, mapHeight - wanderMargin),
          new MapLocation(mapWidth - wanderMargin, mapHeight - wanderMargin),
          new MapLocation(mapWidth / 2, mapHeight / 2)
        };

    currentWanderTarget = BetterRandom.nextInt(wanderTargets.length);
  }

  @Override
  public void run() throws GameActionException {
    // TODO(jmerle): Building refineries when necessary, replacing the HQ drop off location

    if (dropOffLocations.isEmpty()) {
      dropOffLocations.add(senseHQ());
    }

    senseSoup();

    if (!rc.isReady()) return;

    if (tryCompleteOrder()) {
      return;
    }

    if (rc.getSoupCarrying() == me.soupLimit) {
      if (!tryDepositSoup()) {
        tryMoveToDropOff();
      }

      return;
    }

    if (tryMineSoup()) {
      return;
    }

    if (tryMoveTowardsSoup()) {
      return;
    }

    if (tryMoveTowardsInterestingLocation()) {
      return;
    }

    tryWander();
  }

  @Override
  public void onMessage(SoupNearbyMessage message) {
    MapLocation location = message.getLocation();

    if (!invalidInterestingLocations.contains(location)) {
      interestingLocations.add(location);
    }
  }

  private MapLocation senseHQ() {
    for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
      if (robot.getType() == RobotType.HQ) {
        return robot.getLocation();
      }
    }

    return null;
  }

  private void senseSoup() {
    if (soupNearbyMessageCooldown > 0) {
      soupNearbyMessageCooldown--;
    }

    for (MapLocation soupLocation : rc.senseNearbySoup()) {
      if (invalidSoupLocations.contains(soupLocation)) {
        continue;
      }

      if (soupLocations.add(soupLocation) && soupNearbyMessageCooldown == 0) {
        dispatchMessage(new SoupNearbyMessage(rc.getLocation()));
        invalidInterestingLocations.add(rc.getLocation());
        soupNearbyMessageCooldown = 100;
      }
    }
  }

  private boolean tryDepositSoup() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (rc.canDepositSoup(direction)) {
        drawLine(rc.adjacentLocation(direction), Color.BLUE);
        rc.depositSoup(direction, rc.getSoupCarrying());
        return true;
      }
    }

    return false;
  }

  private void tryMoveToDropOff() throws GameActionException {
    MapLocation bestDropOff = getClosestLocation(dropOffLocations);

    if (bestDropOff != null) {
      tryMoveTo(bestDropOff);
    }
  }

  private boolean tryMineSoup() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (rc.canMineSoup(direction)) {
        drawLine(rc.adjacentLocation(direction), Color.GREEN);
        rc.mineSoup(direction);
        return true;
      } else {
        soupLocations.remove(rc.adjacentLocation(direction));
      }
    }

    return false;
  }

  private boolean tryMoveTowardsSoup() throws GameActionException {
    MapLocation closestSoup = getClosestLocation(soupLocations);

    if (closestSoup == null) {
      return false;
    }

    if (closestSoup.equals(currentTarget) && isStuck()) {
      soupLocations.remove(closestSoup);
      invalidSoupLocations.add(closestSoup);
      return tryMoveTowardsSoup();
    }

    return tryMoveTo(closestSoup);
  }

  private boolean tryMoveTowardsInterestingLocation() throws GameActionException {
    MapLocation closestLocation = getClosestLocation(interestingLocations);

    if (closestLocation == null) {
      return false;
    }

    if (rc.getLocation().equals(closestLocation)) {
      interestingLocations.remove(closestLocation);
      invalidInterestingLocations.add(closestLocation);
      return tryMoveTowardsInterestingLocation();
    }

    if (currentTarget.equals(closestLocation) && isStuck()) {
      interestingLocations.remove(closestLocation);
      invalidInterestingLocations.add(closestLocation);
      return tryMoveTowardsInterestingLocation();
    }

    return tryMoveTo(closestLocation);
  }

  private void tryWander() throws GameActionException {
    if (isStuck() || rc.getLocation().equals(wanderTargets[currentWanderTarget])) {
      currentWanderTarget = (currentWanderTarget + 1) % wanderTargets.length;
    }

    tryMoveTo(wanderTargets[currentWanderTarget]);
  }
}
