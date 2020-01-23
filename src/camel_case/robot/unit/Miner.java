package camel_case.robot.unit;

import battlecode.common.*;
import camel_case.message.impl.*;
import camel_case.util.BetterRandom;
import camel_case.util.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Miner extends Unit {
  private MapLocation hq;

  private Set<MapLocation> dropOffLocations = new HashSet<>();

  private Set<MapLocation> soupLocations = new HashSet<>();
  private Set<MapLocation> invalidSoupLocations = new HashSet<>();

  private Set<MapLocation> interestingLocations = new HashSet<>();
  private Set<MapLocation> invalidInterestingLocations = new HashSet<>();
  private int soupNearbyMessageCooldown = 0;

  private MapLocation[] wanderTargets;
  private int currentWanderTarget;

  private boolean canBuildRefineries = false;

  private List<MapLocation> possibleEnemyLocations = new ArrayList<>(9);

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
    if (hq == null) {
      hq = senseOwnRobot(RobotType.HQ);
    }

    if (dropOffLocations.isEmpty() && !canBuildRefineries) {
      dropOffLocations.add(senseOwnRobot(RobotType.HQ));
    }

    if (rc.getRoundNum() % 500 == 0) {
      invalidSoupLocations.clear();
      invalidInterestingLocations.clear();
    }

    if (soupLocations.size() < 10) {
      senseSoup();
    }

    if (!rc.isReady()) return;

    if (!possibleEnemyLocations.isEmpty()) {
      rush();
      return;
    }

    if (!dropOffLocations.contains(hq) && isAdjacentTo(hq)) {
      tryMoveRandom();
      return;
    }

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

  @Override
  public void onMessage(OrderCompletedMessage message) {
    OrderMessage order = getOrder(message.getId());

    if (order != null
        && order.getRobotType() == RobotType.DESIGN_SCHOOL
        && stepsTo(order.getLocation(), hq) == 2) {
      canBuildRefineries = true;
    }

    if (order != null && order.getRobotType() == RobotType.REFINERY) {
      if (dropOffLocations.size() == 1 && dropOffLocations.contains(hq)) {
        dropOffLocations.clear();
      }

      dropOffLocations.add(order.getLocation());
    }

    super.onMessage(message);
  }

  @Override
  public void onMessage(RushMessage message) {
    if (rc.getID() == message.getId()) {
      fillPossibleEnemyLocations();
    }
  }

  private void fillPossibleEnemyLocations() {
    int x = hq.x;
    int y = hq.y;

    int horizontalMiddle = rc.getMapWidth() / 2;
    int verticalMiddle = rc.getMapHeight() / 2;

    int horizontalOffset = horizontalMiddle - x;
    int verticalOffset = verticalMiddle - y;

    int xOffset = horizontalMiddle + horizontalOffset;
    int yOffset = verticalMiddle + verticalOffset;

    possibleEnemyLocations.add(new MapLocation(xOffset - 1, y));
    possibleEnemyLocations.add(new MapLocation(xOffset, y));
    possibleEnemyLocations.add(new MapLocation(xOffset + 1, y));

    possibleEnemyLocations.add(new MapLocation(x, yOffset - 1));
    possibleEnemyLocations.add(new MapLocation(x, yOffset));
    possibleEnemyLocations.add(new MapLocation(x, yOffset + 1));

    possibleEnemyLocations.add(new MapLocation(xOffset - 1, yOffset - 1));
    possibleEnemyLocations.add(new MapLocation(xOffset, yOffset));
    possibleEnemyLocations.add(new MapLocation(xOffset + 1, yOffset + 1));
  }

  private void rush() throws GameActionException {
    if (possibleEnemyLocations.isEmpty()) {
      return;
    }

    if (isStuck()) {
      possibleEnemyLocations.remove(0);
      rush();
      return;
    }

    MapLocation targetLocation = possibleEnemyLocations.get(0);

    if (rc.canSenseLocation(targetLocation)) {
      RobotInfo robot = rc.senseRobotAtLocation(targetLocation);
      if (robot == null || robot.getType() != RobotType.HQ || robot.getTeam() != enemyTeam) {
        possibleEnemyLocations.remove(0);
        rush();
        return;
      }

      boolean canSenseAllDirections = true;
      for (Direction direction : adjacentDirections) {
        if (!rc.canSenseLocation(targetLocation.add(direction))) {
          canSenseAllDirections = false;
          break;
        }
      }

      if (!canSenseAllDirections) {
        tryMoveTo(targetLocation);
        return;
      }

      MapLocation designSchoolLocation = rc.getLocation().add(directionTowards(targetLocation));
      if (canDispatchOrderAt(designSchoolLocation, null, 3)) {
        dispatchMessage(new OrderMessage(10, RobotType.DESIGN_SCHOOL, designSchoolLocation));
      }

      possibleEnemyLocations.clear();
    }

    tryMoveTo(targetLocation);
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

      if (soupLocations.size() == 10) {
        break;
      }
    }
  }

  private boolean tryCompleteOrder() throws GameActionException {
    OrderMessage order = orders.peek();

    if (order == null) {
      return false;
    }

    if (order.getRobotType().cost > rc.getTeamSoup()) {
      return false;
    }

    MapLocation orderLocation = order.getLocation();

    if (rc.getLocation().equals(orderLocation)) {
      return tryMoveRandom();
    }

    if (rc.canSenseLocation(orderLocation)) {
      RobotInfo robot = rc.senseRobotAtLocation(orderLocation);

      if (robot != null) {
        if (robot.getTeam() == enemyTeam
            || (robot.getType().isBuilding() && robot.getType() != order.getRobotType())) {
          dispatchMessage(new OrderCanceledMessage(order.getId()));
        }

        return false;
      }
    }

    if (!isAdjacentTo(orderLocation)) {
      return tryMoveTo(orderLocation);
    }

    if (tryBuildRobot(order.getRobotType(), directionTowards(orderLocation))) {
      dispatchMessage(new OrderCompletedMessage(order.getId()));
      return true;
    }

    return false;
  }

  private boolean tryDepositSoup() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (rc.canDepositSoup(direction)) {
        drawLine(rc.adjacentLocation(direction), Color.BLUE);
        rc.depositSoup(direction, rc.getSoupCarrying());
        return true;
      } else {
        dropOffLocations.remove(rc.adjacentLocation(direction));
      }
    }

    return false;
  }

  private void tryMoveToDropOff() throws GameActionException {
    MapLocation bestDropOff = getClosestLocation(dropOffLocations);

    if (canBuildRefineries && dropOffLocations.size() == 1 && dropOffLocations.contains(hq)) {
      tryBuildRefinery();
    } else if (stepsTo(bestDropOff) > 5) {
      tryBuildRefinery();
      return;
    }

    tryMoveTo(bestDropOff);
  }

  private void tryBuildRefinery() throws GameActionException {
    for (OrderMessage order : orders) {
      if (order.getRobotType() == RobotType.REFINERY) {
        return;
      }
    }

    MapLocation hq = senseOwnRobot(RobotType.HQ);

    for (Direction direction : adjacentDirections) {
      MapLocation location = rc.adjacentLocation(direction);

      if (canDispatchOrderAt(location, hq, 3)) {
        dispatchMessage(new OrderMessage(1, RobotType.REFINERY, location));
        return;
      }
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

    if (closestLocation.equals(currentTarget) && isStuck()) {
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
