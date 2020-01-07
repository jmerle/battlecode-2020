package camel_case.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Vaporator extends Building {
  public Vaporator(RobotController rc) {
    super(rc, RobotType.VAPORATOR);
  }

  @Override
  public void run() throws GameActionException {
    // TODO: Implement non-action logic

    if (!rc.isReady()) return;

    // TODO: Implement action logic
  }
}
