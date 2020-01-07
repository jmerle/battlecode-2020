package camel_case;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import camel_case.robot.Robot;
import camel_case.robot.building.*;
import camel_case.robot.unit.DeliveryDrone;
import camel_case.robot.unit.Landscaper;
import camel_case.robot.unit.Miner;

@SuppressWarnings("unused")
public class RobotPlayer {
  public static void run(RobotController rc) {
    Robot robot = createRobot(rc);

    if (robot == null) {
      return;
    }

    //noinspection InfiniteLoopStatement
    while (true) {
      try {
        robot.run();
        logBytecodeUsage(rc);
        Clock.yield();
      } catch (Exception e) {
        System.out.println("Exception in robot #" + rc.getID() + " (" + rc.getType() + ")");
        e.printStackTrace();
      }
    }
  }

  private static Robot createRobot(RobotController rc) {
    switch (rc.getType()) {
      case MINER:
        return new Miner(rc);
      case LANDSCAPER:
        return new Landscaper(rc);
      case DELIVERY_DRONE:
        return new DeliveryDrone(rc);
      case HQ:
        return new HQ(rc);
      case REFINERY:
        return new Refinery(rc);
      case VAPORATOR:
        return new Vaporator(rc);
      case DESIGN_SCHOOL:
        return new DesignSchool(rc);
      case FULFILLMENT_CENTER:
        return new FulfillmentCenter(rc);
      case NET_GUN:
        return new NetGun(rc);
      default:
        System.out.println("Unknown robot type '" + rc.getType() + "'");
        return null;
    }
  }

  private static void logBytecodeUsage(RobotController rc) {
    int used = Clock.getBytecodeNum();
    int total = rc.getType().bytecodeLimit;
    int percentage = (int) Math.round((double) used / (double) total * 100.0);
    System.out.println(used + "/" + total + " (" + percentage + "%)");
  }
}
