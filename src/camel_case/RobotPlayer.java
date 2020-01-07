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
  @SuppressWarnings("unused")
  public static void run(RobotController rc) {
    Robot robot;

    switch (rc.getType()) {
      case MINER:
        robot = new Miner(rc);
        break;
      case LANDSCAPER:
        robot = new Landscaper(rc);
        break;
      case DELIVERY_DRONE:
        robot = new DeliveryDrone(rc);
        break;
      case HQ:
        robot = new HQ(rc);
        break;
      case REFINERY:
        robot = new Refinery(rc);
        break;
      case VAPORATOR:
        robot = new Vaporator(rc);
        break;
      case DESIGN_SCHOOL:
        robot = new DesignSchool(rc);
        break;
      case FULFILLMENT_CENTER:
        robot = new FulfillmentCenter(rc);
        break;
      case NET_GUN:
        robot = new NetGun(rc);
        break;
      default:
        System.out.println("Unknown robot type '" + rc.getType() + "'");
        return;
    }

    //noinspection InfiniteLoopStatement
    while (true) {
      try {
        robot.run();
        Clock.yield();
      } catch (Exception e) {
        System.out.println("Exception in robot #" + rc.getID() + " (" + rc.getType() + ")");
        e.printStackTrace();
      }
    }
  }
}
