package camel_case_sprint.robot.unit;

import battlecode.common.*;
import camel_case_sprint.message.impl.*;
import camel_case_sprint.util.BetterRandom;

import java.util.*;

public class Miner extends Unit {
  private MapLocation dropOff;

  private int soupFoundMessageCooldown = 0;
  private Set<MapLocation> soupLocations = new HashSet<>(16);

  private MapLocation[] wanderTargets;
  private int currentWanderTarget = 0;
  private int wanderTargetCooldown = 0;

  private List<MapLocation> possibleEnemyLocations = new ArrayList<>(3);
  private boolean dispatchedEnemyLocation = false;

  private Queue<OrderMessage> buildOrders =
      new PriorityQueue<>(Comparator.comparingInt(OrderMessage::getId));

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
    if (dropOff == null) {
      dropOff = senseHQ();
    }

    if (soupFoundMessageCooldown > 0) {
      soupFoundMessageCooldown--;
    }

    senseSoupLocations();

    if (!rc.isReady()) return;

    if (!possibleEnemyLocations.isEmpty()) {
      rush();
      return;
    }

    if (!buildOrders.isEmpty()) {
      OrderMessage order = buildOrders.peek();
      if (rc.getTeamSoup() >= order.getRobotType().cost) {
        completeOrder(order);
        return;
      }
    }

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

  @Override
  public void onMessage(OrderMessage message) {
    if (message.getRobotType().isBuilding()) {
      buildOrders.add(message);
    }
  }

  @Override
  public void onMessage(OrderCompletedMessage message) {
    removeOrder(message.getId());
  }

  @Override
  public void onMessage(StartRushMessage message) {
    if (message.getRobotId() == rc.getID()) {
      fillPossibleEnemyLocations();
    }
  }

  private MapLocation senseHQ() {
    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, myTeam);

    for (RobotInfo robotInfo : nearbyRobots) {
      if (robotInfo.getType() == RobotType.HQ) {
        return robotInfo.getLocation();
      }
    }

    return null;
  }

  private void senseSoupLocations() throws GameActionException {
    int sensorRadius = (int) Math.sqrt(me.sensorRadiusSquared + 1);
    boolean soupFoundDispatched = false;

    for (int y = -sensorRadius; y < sensorRadius; y++) {
      for (int x = -sensorRadius; x < sensorRadius; x++) {
        MapLocation location = rc.getLocation().translate(x, y);

        if (rc.canSenseLocation(location)
            && rc.senseSoup(location) > 0
            && !rc.senseFlooding(location)) {
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

  private void fillPossibleEnemyLocations() {
    int x = dropOff.x;
    int y = dropOff.y;

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
      dispatchMessage(new EnemyNotFoundMessage());
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

      if (!dispatchedEnemyLocation) {
        dispatchMessage(new EnemyFoundMessage(targetLocation));
        dispatchedEnemyLocation = false;
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

      for (Direction direction : adjacentDirections) {
        MapLocation location = targetLocation.add(direction);

        if (location.equals(rc.getLocation()) || rc.senseRobotAtLocation(location) == null) {
          dispatchMessage(new OrderMessage(0, RobotType.DESIGN_SCHOOL, location));
          break;
        }
      }

      possibleEnemyLocations.clear();
    }

    tryMoveTo(targetLocation);
  }

  private void completeOrder(OrderMessage order) throws GameActionException {
    Direction direction = directionTowards(order.getLocation());

    if (rc.canSenseLocation(order.getLocation())) {
      RobotInfo robot = rc.senseRobotAtLocation(order.getLocation());
      if (robot != null && robot.getTeam() == enemyTeam) {
        boolean enemyHqNearby = false;

        for (RobotInfo nearbyRobot : rc.senseNearbyRobots(-1, enemyTeam)) {
          if (nearbyRobot.getType() == RobotType.HQ) {
            enemyHqNearby = true;
            break;
          }
        }

        if (enemyHqNearby) {
          removeOrder(order.getId());
          dispatchMessage(new OrderCompletedMessage(order.getId()));
          return;
        }
      }
    }

    if (rc.getLocation().add(direction).equals(order.getLocation())) {
      if (tryBuildRobot(order.getRobotType(), direction)) {
        removeOrder(order.getId());
        dispatchMessage(new OrderCompletedMessage(order.getId()));
        return;
      }

      RobotInfo blockingRobot = rc.senseRobotAtLocation(order.getLocation());
      if (blockingRobot != null && blockingRobot.getType().isBuilding()) {
        removeOrder(order.getId());
        return;
      }
    }

    if (rc.getLocation().equals(order.getLocation())) {
      tryMoveRandom();
    } else {
      tryMoveTo(order.getLocation());
    }
  }

  private void removeOrder(int id) {
    Iterator<OrderMessage> it = buildOrders.iterator();

    while (it.hasNext()) {
      OrderMessage order = it.next();

      if (order.getId() == id) {
        if (order.getRobotType() == RobotType.REFINERY) {
          dropOff = order.getLocation();
        }

        it.remove();
        break;
      }
    }
  }

  private void deliverSoup() throws GameActionException {
    if (tryRefine(directionTowards(dropOff))) {
      return;
    }

    tryMoveTo(dropOff);
  }

  private void findAndMineSoup() throws GameActionException {
    if (tryMineSoup()) return;
    if (tryMoveToInterestingLocation()) return;
    if (tryWander()) return;

    tryMoveRandom();
  }

  private boolean tryMineSoup() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      MapLocation soupLocation = rc.adjacentLocation(direction);

      if (tryMine(direction)) {
        if (rc.senseSoup(soupLocation) == 0) {
          soupLocations.remove(soupLocation);
          dispatchMessage(new SoupGoneMessage(soupLocation));
        } else if (soupLocations.add(soupLocation) && soupFoundMessageCooldown == 0) {
          dispatchMessage(new SoupFoundMessage(soupLocation));
          soupFoundMessageCooldown = 50;
        }

        return true;
      } else if (soupLocations.contains(soupLocation)) {
        soupLocations.remove(soupLocation);
        dispatchMessage(new SoupGoneMessage(soupLocation));
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
