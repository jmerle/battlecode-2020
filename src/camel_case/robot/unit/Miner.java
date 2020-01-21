package camel_case.robot.unit;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Miner extends Unit {
  public Miner(RobotController rc) {
    super(rc, RobotType.MINER);
  }

  @Override
  public void run() throws GameActionException {
    // TODO(jmerle): Implement non-action logic

    if (!rc.isReady()) return;

    // TODO(jmerle): Implement action logic

    tryMoveRandom();
  }

  private boolean tryMineSoup(Direction direction) throws GameActionException {
    if (rc.canMineSoup(direction)) {
      rc.mineSoup(direction);
      return true;
    }

    return false;
  }

  private boolean tryDepositSoup(Direction direction) throws GameActionException {
    if (rc.canDepositSoup(direction)) {
      rc.depositSoup(direction, rc.getSoupCarrying());
      return true;
    }

    return false;
  }
}
