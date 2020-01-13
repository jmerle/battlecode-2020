package camel_case.robot.unit;

import battlecode.common.*;
import camel_case.robot.Robot;

public abstract class Unit extends Robot {
  private MapLocation currentTarget;
  private boolean isBugMoving;
  private int distanceBeforeBugMoving;
  private boolean huggingLeftWall;
  private MapLocation lastHuggedWall;

  public Unit(RobotController rc, RobotType type) {
    super(rc, type);
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

  protected boolean tryMoveRandom() throws GameActionException {
    return tryMove(randomAdjacentDirection());
  }

  protected boolean tryMoveTo(MapLocation target) throws GameActionException {
    if (!target.equals(currentTarget)) {
      currentTarget = target;
      isBugMoving = false;
    }

    if (isBugMoving) {
      if (rc.getLocation().distanceSquaredTo(currentTarget) < distanceBeforeBugMoving) {
        if (tryMove(directionTowards(currentTarget))) {
          isBugMoving = false;
          return true;
        }
      }
    } else {
      if (tryMove(directionTowards(currentTarget))) {
        return true;
      } else {
        isBugMoving = true;
        distanceBeforeBugMoving = rc.getLocation().distanceSquaredTo(currentTarget);

        determineBugMoveDirection();
      }
    }

    return makeBugMove(true);
  }

  private void determineBugMoveDirection() {
    Direction left = directionTowards(currentTarget).rotateLeft();
    int leftDistance = Integer.MAX_VALUE;

    Direction right = directionTowards(currentTarget).rotateRight();
    int rightDistance = Integer.MAX_VALUE;

    for (int i = 0; i < 8; i++) {
      if (rc.canMove(left)) {
        leftDistance = rc.getLocation().add(left).distanceSquaredTo(currentTarget);
        break;
      }

      left = left.rotateLeft();
    }

    for (int i = 0; i < 8; i++) {
      if (rc.canMove(right)) {
        rightDistance = rc.getLocation().add(right).distanceSquaredTo(currentTarget);
        break;
      }

      right = right.rotateRight();
    }

    if (leftDistance > rightDistance) {
      huggingLeftWall = true;
      lastHuggedWall = rc.getLocation().add(right.rotateLeft());
    } else {
      huggingLeftWall = false;
      lastHuggedWall = rc.getLocation().add(left.rotateRight());
    }
  }

  private boolean makeBugMove(boolean firstCall) throws GameActionException {
    Direction currentDirection = directionTowards(lastHuggedWall);

    for (int i = 0; i < 8; i++) {
      if (huggingLeftWall) {
        currentDirection = currentDirection.rotateRight();
      } else {
        currentDirection = currentDirection.rotateLeft();
      }

      MapLocation newLocation = rc.getLocation().add(currentDirection);

      if (firstCall && !isOnTheMap(newLocation)) {
        huggingLeftWall = !huggingLeftWall;
        return makeBugMove(false);
      }

      if (tryMove(currentDirection)) {
        return true;
      } else {
        lastHuggedWall = rc.getLocation().add(currentDirection);
      }
    }

    return false;
  }
}
