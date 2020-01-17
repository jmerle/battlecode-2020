package camel_case_sprint.robot.building;

import battlecode.common.*;
import camel_case_sprint.message.impl.EnemyFoundMessage;
import camel_case_sprint.message.impl.EnemyNotFoundMessage;
import camel_case_sprint.message.impl.OrderMessage;
import camel_case_sprint.message.impl.StartRushMessage;

import java.util.ArrayDeque;
import java.util.Queue;

public class HQ extends Building {
  private enum RushState {
    NOT_STARTED,
    STARTED,
    DONE
  }

  private RushState rushState = RushState.NOT_STARTED;
  private int minersSpawned = 0;
  private boolean dispatchedBuildOrders = false;

  private MapLocation enemyHq;

  public HQ(RobotController rc) {
    super(rc, RobotType.HQ);
  }

  @Override
  public void run() throws GameActionException {
    if (rushState == RushState.NOT_STARTED && minersSpawned == 1) {
      startRush();
    }

    if (!rc.isReady()) return;

    if (tryShootEnemyDrone()) {
      return;
    }

    if (minersSpawned < 3) {
      trySpawnMiner();
      return;
    }

    if (rushState != RushState.DONE) {
      return;
    }

    if (minersSpawned < 5) {
      trySpawnMiner();
      return;
    }

    if (!dispatchedBuildOrders) {
      dispatchBuildOrders();
      dispatchedBuildOrders = true;
    }
  }

  @Override
  public void onMessage(OrderMessage message) {
    if (message.getRobotType() == RobotType.DESIGN_SCHOOL && rushState == RushState.STARTED) {
      rushState = RushState.DONE;
    }
  }

  @Override
  public void onMessage(EnemyFoundMessage message) {
    enemyHq = message.getLocation();
  }

  @Override
  public void onMessage(EnemyNotFoundMessage message) {
    rushState = RushState.DONE;
  }

  private void startRush() {
    rushState = RushState.STARTED;

    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, myTeam);
    for (RobotInfo robot : nearbyRobots) {
      if (robot.getType() == RobotType.MINER) {
        dispatchMessage(new StartRushMessage(robot.getID()));
        return;
      }
    }
  }

  private Queue<MapLocation> findBuildLocations() throws GameActionException {
    Queue<MapLocation> locations = new ArrayDeque<>(8);

    int myElevation = rc.senseElevation(rc.getLocation());

    int[][] checks = {{-2, -2}, {0, -2}, {2, -2}, {-2, 0}, {2, 0}, {-2, 2}, {0, 2}, {2, 2}};
    for (int[] check : checks) {
      MapLocation location = rc.getLocation().translate(check[0], check[1]);

      if (rc.onTheMap(location)
          && !rc.senseFlooding(location)
          && Math.abs(myElevation - rc.senseElevation(location)) <= 3) {
        RobotInfo robotInfo = rc.senseRobotAtLocation(location);
        if (robotInfo == null || !robotInfo.getType().isBuilding()) {
          locations.add(location);
        }
      }
    }

    return locations;
  }

  private void trySpawnMiner() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (tryBuildRobot(RobotType.MINER, direction)) {
        minersSpawned++;
        return;
      }
    }
  }

  private void dispatchBuildOrders() throws GameActionException {
    int designSchoolRange = RobotType.DESIGN_SCHOOL.sensorRadiusSquared;
    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(designSchoolRange, myTeam);
    boolean designSchoolInRange = false;

    for (RobotInfo robotInfo : nearbyRobots) {
      if (robotInfo.getType() == RobotType.DESIGN_SCHOOL) {
        designSchoolInRange = true;
      }
    }

    Queue<MapLocation> buildLocations = findBuildLocations();

    if (buildLocations.isEmpty()) {
      return;
    }

    if (buildLocations.size() == 1 && !designSchoolInRange) {
      dispatchMessage(new OrderMessage(1, RobotType.DESIGN_SCHOOL, buildLocations.poll()));
      return;
    }

    dispatchMessage(new OrderMessage(1, RobotType.REFINERY, buildLocations.poll()));

    if (!designSchoolInRange && !buildLocations.isEmpty()) {
      dispatchMessage(new OrderMessage(2, RobotType.DESIGN_SCHOOL, buildLocations.poll()));
    }
  }
}
