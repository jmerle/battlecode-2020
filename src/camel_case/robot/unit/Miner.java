package camel_case.robot.unit;

import battlecode.common.*;

public class Miner extends Unit {
  private MapLocation hq;

  public Miner(RobotController rc) {
    super(rc, RobotType.MINER);
  }

  @Override
  public void run() throws GameActionException {
    if (hq == null) {
      senseHQ();
    }

    // TODO: Sense soup around unit and send message to other miners to help if there's a lot

    if (!rc.isReady()) return;

    if (rc.getSoupCarrying() == me.soupLimit) {
      deliverSoup();
    } else {
      findAndMineSoup();
    }
  }

  private void senseHQ() {
    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, myTeam);

    for (RobotInfo robotInfo : nearbyRobots) {
      if (robotInfo.getType() == RobotType.HQ) {
        hq = robotInfo.getLocation();
        return;
      }
    }
  }

  private void deliverSoup() throws GameActionException {
    if (hq == null) {
      tryMoveRandom();
      return;
    }

    if (tryRefine(rc.getLocation().directionTo(hq))) {
      return;
    }

    tryMoveTo(hq);
  }

  private void findAndMineSoup() throws GameActionException {
    for (Direction direction : adjacentDirections) {
      if (tryMine(direction)) {
        return;
      }
    }

    tryMoveRandom();
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
