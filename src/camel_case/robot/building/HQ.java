package camel_case.robot.building;

import battlecode.common.*;
import camel_case.GeneratedData;
import camel_case.message.impl.OrderCanceledMessage;
import camel_case.message.impl.OrderMessage;
import camel_case.message.impl.RushMessage;
import camel_case.util.Color;

public class HQ extends Building {
  private int minersSpawned = 0;

  private boolean shouldBuildDesignSchool = false;
  private boolean hasStartedRush = false;

  public HQ(RobotController rc) {
    super(rc, RobotType.HQ);
  }

  @Override
  public void run() throws GameActionException {
    for (OrderMessage order : orders) {
      drawDot(order.getLocation(), Color.GREEN);
    }

    if (shouldBuildDesignSchool) {
      shouldBuildDesignSchool = !dispatchDesignSchoolOrder();
    }

    if (!hasStartedRush && minersSpawned > 0) {
      hasStartedRush = startRush();
    }

    if (!rc.isReady()) return;

    if (tryShootEnemyDrone()) {
      return;
    }

    if (minersSpawned >= 5) {
      return;
    }

    if (trySpawnMiner()) {
      minersSpawned++;

      if (minersSpawned == 5) {
        shouldBuildDesignSchool = !dispatchDesignSchoolOrder();
      }
    }
  }

  @Override
  public void onMessage(OrderCanceledMessage message) {
    OrderMessage order = getOrder(message.getId());

    if (order != null && order.getRobotType() == RobotType.DESIGN_SCHOOL) {
      shouldBuildDesignSchool = true;
    }

    super.onMessage(message);
  }

  private boolean dispatchDesignSchoolOrder() throws GameActionException {
    MapLocation hq = rc.getLocation();

    for (int[] offset : GeneratedData.RING_2_OFFSETS) {
      MapLocation location = hq.translate(offset[0], offset[1]);

      if (canDispatchOrderAt(location, hq, 6)) {
        dispatchMessage(new OrderMessage(0, RobotType.DESIGN_SCHOOL, location));
        return true;
      }
    }

    return false;
  }

  private boolean startRush() {
    for (RobotInfo robot : rc.senseNearbyRobots(-1, myTeam)) {
      if (robot.getType() == RobotType.MINER) {
        dispatchMessage(new RushMessage(robot.getID()));
        return true;
      }
    }

    return false;
  }

  private boolean trySpawnMiner() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (tryBuildRobot(RobotType.MINER, direction)) {
        return true;
      }
    }

    return false;
  }
}
