package camel_case.robot;

import battlecode.common.*;

import java.util.Random;

public abstract class Robot {
  protected RobotController rc;

  protected RobotType me;

  protected Team myTeam;
  protected Team enemyTeam;

  protected Direction[] adjacentDirections = {
    Direction.NORTH,
    Direction.NORTHEAST,
    Direction.EAST,
    Direction.SOUTHEAST,
    Direction.SOUTH,
    Direction.SOUTHWEST,
    Direction.WEST,
    Direction.NORTHWEST
  };

  protected Random random = new Random();

  public Robot(RobotController rc, RobotType type) {
    this.rc = rc;

    me = type;

    myTeam = rc.getTeam();
    enemyTeam = myTeam.opponent();
  }

  public abstract void run() throws GameActionException;

  protected Direction randomAdjacentDirection() {
    return adjacentDirections[random.nextInt(adjacentDirections.length)];
  }
}
