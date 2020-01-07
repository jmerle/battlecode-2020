package camel_case.robot.unit;

import battlecode.common.*;
import camel_case.robot.Robot;

public abstract class Unit extends Robot {
  public Unit(RobotController rc, RobotType type) {
    super(rc, type);
  }

  protected boolean tryMoveRandom() throws GameActionException {
    return tryMove(randomAdjacentDirection());
  }

  protected boolean tryMoveTo(MapLocation target) throws GameActionException {
    if (me.canFly()) {
      return tryMove(directionTowards(rc.getLocation(), target));
    }

    Direction forward = directionTowards(rc.getLocation(), target);
    Direction left = forward.rotateLeft();
    Direction right = forward.rotateRight();

    if (tryMove(forward)) return true;
    if (tryMove(left)) return true;
    if (tryMove(left.rotateLeft())) return true;
    if (tryMove(left.rotateLeft().rotateLeft())) return true;
    if (tryMove(right)) return true;
    if (tryMove(right.rotateRight())) return true;
    if (tryMove(right.rotateRight().rotateRight())) return true;
    return tryMove(forward.opposite());
  }

  protected boolean tryMove(Direction direction) throws GameActionException {
    if (rc.canMove(direction)) {
      if (me.canFly() || !rc.senseFlooding(rc.adjacentLocation(direction))) {
        rc.move(direction);
        return true;
      }
    }

    return false;
  }
}
