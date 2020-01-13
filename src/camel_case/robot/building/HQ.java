package camel_case.robot.building;

import battlecode.common.*;

import java.util.ArrayDeque;
import java.util.Queue;

public class HQ extends Building {
  private int minersSpawned = 0;

  private Queue<MapLocation> buildLocations = null;

  private boolean dispatchedBuildOrders = false;

  public HQ(RobotController rc) {
    super(rc, RobotType.HQ);
  }

  @Override
  public void run() throws GameActionException {
    if (buildLocations == null) {
      buildLocations = findBuildLocations();
    }

    if (!rc.isReady()) return;

    if (tryShootEnemyDrone()) {
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

  private Queue<MapLocation> findBuildLocations() throws GameActionException {
    Queue<MapLocation> locations = new ArrayDeque<>(8);

    int[][] checks = {{-2, -2}, {0, -2}, {2, -2}, {-2, 0}, {2, 0}, {-2, 2}, {0, 2}, {2, 2}};
    for (int[] check : checks) {
      MapLocation location = rc.getLocation().translate(check[0], check[1]);

      if (rc.onTheMap(location) && !rc.senseFlooding(location)) {
        locations.add(location);
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

  private void dispatchBuildOrders() {
    int designSchoolRange = RobotType.DESIGN_SCHOOL.sensorRadiusSquared;
    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(designSchoolRange, myTeam);
    boolean designSchoolInRange = false;

    for (RobotInfo robotInfo : nearbyRobots) {
      if (robotInfo.getType() == RobotType.DESIGN_SCHOOL) {
        designSchoolInRange = true;
      }
    }

    if (buildLocations.size() < (designSchoolInRange ? 1 : 2)) {
      return;
    }

    dispatchOrder(RobotType.REFINERY, buildLocations.poll());

    if (!designSchoolInRange) {
      dispatchOrder(RobotType.DESIGN_SCHOOL, buildLocations.poll());
    }
  }
}
