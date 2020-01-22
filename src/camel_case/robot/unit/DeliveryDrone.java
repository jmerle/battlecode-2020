package camel_case.robot.unit;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class DeliveryDrone extends Unit {
  public DeliveryDrone(RobotController rc) {
    super(rc, RobotType.DELIVERY_DRONE);
  }

  @Override
  public void run() throws GameActionException {
    // TODO(jmerle): Implement non-action logic

    if (!rc.isReady()) return;

    // TODO(jmerle): Implement action logic

    if (tryCompleteOrder()) {
      return;
    }

    tryMoveRandom();
  }

  private boolean tryPickUpUnit(int id) throws GameActionException {
    if (rc.canPickUpUnit(id)) {
      rc.pickUpUnit(id);
      return true;
    }

    return false;
  }

  private boolean tryDropUnit(Direction direction) throws GameActionException {
    if (rc.canDropUnit(direction)) {
      rc.dropUnit(direction);
      return true;
    }

    return false;
  }
}
