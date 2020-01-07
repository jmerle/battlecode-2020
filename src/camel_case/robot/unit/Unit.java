package camel_case.robot.unit;

import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case.robot.Robot;

public abstract class Unit extends Robot {
  public Unit(RobotController rc, RobotType type) {
    super(rc, type);
  }
}
