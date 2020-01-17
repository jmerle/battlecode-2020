package camel_case_sprint.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class NetGun extends Building {
  public NetGun(RobotController rc) {
    super(rc, RobotType.NET_GUN);
  }

  @Override
  public void run() throws GameActionException {
    if (!rc.isReady()) return;

    tryShootEnemyDrone();
  }
}
