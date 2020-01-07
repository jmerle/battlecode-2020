package camel_case.robot.building;

import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case.robot.Robot;

public abstract class Building extends Robot {
  public Building(RobotController rc, RobotType type) {
    super(rc, type);
  }
}
