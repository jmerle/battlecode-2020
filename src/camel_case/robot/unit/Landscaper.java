package camel_case.robot.unit;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Landscaper extends Unit {
  public Landscaper(RobotController rc) {
    super(rc, RobotType.LANDSCAPER);
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

  private boolean tryDigDirt(Direction direction) throws GameActionException {
    if (rc.canDigDirt(direction)) {
      rc.digDirt(direction);
      return true;
    }

    return false;
  }

  private boolean tryDepositDirt(Direction direction) throws GameActionException {
    if (rc.canDepositDirt(direction)) {
      rc.depositDirt(direction);
      return true;
    }

    return false;
  }
}
