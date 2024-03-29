package camel_case_sprint;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import camel_case_sprint.robot.Robot;
import camel_case_sprint.robot.building.*;
import camel_case_sprint.robot.unit.DeliveryDrone;
import camel_case_sprint.robot.unit.Landscaper;
import camel_case_sprint.robot.unit.Miner;

@SuppressWarnings("unused")
public class RobotPlayer {
  public static void run(RobotController rc) {
    Robot robot = createRobot(rc);

    if (robot == null) {
      return;
    }

    int startTurn = rc.getRoundNum();

    //noinspection InfiniteLoopStatement
    while (true) {
      try {
        robot.getMessageDispatcher().handleIncomingMessages();
        robot.run();
        robot.getMessageDispatcher().sendBatch();
      } catch (Exception e) {
        System.out.println("Exception in robot #" + rc.getID() + " (" + rc.getType() + ")");
        e.printStackTrace();
      }

      if (rc.getRoundNum() > startTurn) {
        int limit = rc.getType().bytecodeLimit;
        System.out.println("Used too much bytecode, the limit is " + limit + "!");
      } else {
        notifyHighBytecodeUsage(rc);
      }

      Clock.yield();
      startTurn = rc.getRoundNum();
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

  private static void notifyHighBytecodeUsage(RobotController rc) {
    int used = Clock.getBytecodeNum();
    int total = rc.getType().bytecodeLimit;
    double percentage = (double) used / (double) total * 100.0;

    if (percentage >= 90) {
      String format = "High bytecode usage!\n%s/%s (%s%%)";
      System.out.println(String.format(format, used, total, (int) Math.round(percentage)));
    }
  }
}
