package camel_case.robot.unit;

import battlecode.common.*;
import camel_case.message.impl.OrderMessage;
import camel_case.robot.Robot;
import camel_case.util.BetterRandom;
import camel_case.util.Color;

public abstract class Unit extends Robot {
  protected MapLocation currentTarget;

  private boolean isBugMoving;
  private int distanceBeforeBugMoving;
  private boolean huggingLeftWall;
  private MapLocation lastHuggedWall;

  private int distanceToTarget;
  private int turnsSpentMovingTowardsTarget;

  public Unit(RobotController rc, RobotType type) {
    super(rc, type);
  }

  @Override
  protected boolean tryCompleteOrder() throws GameActionException {
    if (super.tryCompleteOrder()) {
      return true;
    }

    OrderMessage order = orders.peek();
    return order != null && tryMoveTo(order.getLocation());
  }

  protected boolean isStuck() {
    return turnsSpentMovingTowardsTarget > distanceToTarget * 1.5;
  }

  protected boolean tryMove(Direction direction) throws GameActionException {
    if (rc.canMove(direction)) {
      if (me.canFly() || !rc.senseFlooding(rc.adjacentLocation(direction))) {
        rc.move(direction);

        if (currentTarget != null) {
          drawLine(currentTarget, Color.YELLOW);
        }

        if (lastHuggedWall != null) {
          drawDot(lastHuggedWall, Color.RED);
        }

        return true;
      }
    }

    return false;
  }

  protected boolean tryMoveRandom() throws GameActionException {
    currentTarget = null;
    return tryMove(adjacentDirections[BetterRandom.nextInt(adjacentDirections.length)]);
  }

  protected boolean tryMoveTo(MapLocation target) throws GameActionException {
    if (!target.equals(currentTarget)) {
      currentTarget = target;

      isBugMoving = false;
      lastHuggedWall = null;

      distanceToTarget = (int) Math.ceil(Math.sqrt(rc.getLocation().distanceSquaredTo(target)));
      turnsSpentMovingTowardsTarget = 1;
    } else {
      turnsSpentMovingTowardsTarget++;
    }

    if (isBugMoving) {
      if (rc.getLocation().distanceSquaredTo(currentTarget) < distanceBeforeBugMoving) {
        if (tryMove(directionTowards(currentTarget))) {
          isBugMoving = false;
          lastHuggedWall = null;
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
    Direction forward = directionTowards(currentTarget);

    Direction left = forward.rotateLeft();
    int leftDistance = Integer.MAX_VALUE;

    Direction right = forward.rotateRight();
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
