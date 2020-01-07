package camel_case.robot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public abstract class Robot {
  protected RobotController rc;
  protected RobotType details;

  public Robot(RobotController rc, RobotType type) {
    this.rc = rc;
    details = type;
  }

  public abstract void run() throws GameActionException;
}
