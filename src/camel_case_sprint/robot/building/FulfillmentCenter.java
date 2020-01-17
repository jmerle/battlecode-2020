package camel_case_sprint.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class FulfillmentCenter extends Building {
  public FulfillmentCenter(RobotController rc) {
    super(rc, RobotType.FULFILLMENT_CENTER);
  }

  @Override
  public void run() throws GameActionException {
    // TODO(jmerle): Implement non-action logic

    if (!rc.isReady()) return;

    // TODO(jmerle): Implement action logic
  }
}
