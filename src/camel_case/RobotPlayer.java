package camel_case;

import battlecode.common.Clock;
import battlecode.common.RobotController;

@SuppressWarnings("unused")
public class RobotPlayer {
  @SuppressWarnings("unused")
  public static void run(RobotController rc) {
    while (true) {
      try {
        Clock.yield();
      } catch (Exception e) {
        System.out.println("Exception in robot #" + rc.getID() + " (" + rc.getType() + ")");
        e.printStackTrace();
      }
    }
  }
}
