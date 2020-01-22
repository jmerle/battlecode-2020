package camel_case.robot.building;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case.message.impl.AllMinersSpawnedMessage;
import camel_case.message.impl.OrderCanceledMessage;
import camel_case.message.impl.OrderMessage;
import camel_case.util.Color;

public class HQ extends Building {
  private int minersSpawned = 0;

  public HQ(RobotController rc) {
    super(rc, RobotType.HQ);
  }

  @Override
  public void run() throws GameActionException {
    for (OrderMessage order : orders) {
      drawDot(order.getLocation(), Color.GREEN);
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
        dispatchMessage(new AllMinersSpawnedMessage());
        dispatchDesignSchoolOrder();
      }
    }
  }

  @Override
  public void onMessage(OrderCanceledMessage message) {
    OrderMessage order = getOrder(message.getId());

    if (order != null && order.getRobotType() == RobotType.DESIGN_SCHOOL) {
      dispatchDesignSchoolOrder();
    }

    super.onMessage(message);
  }

  private boolean trySpawnMiner() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (tryBuildRobot(RobotType.MINER, direction)) {
        return true;
      }
    }

    return false;
  }

  private void dispatchDesignSchoolOrder() {
    // TODO(jmerle): Find best design school location and dispatch order
  }
}
