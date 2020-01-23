package camel_case.message;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.Transaction;
import camel_case.GeneratedData;
import camel_case.message.impl.*;
import camel_case.robot.Robot;

import java.util.ArrayDeque;
import java.util.Queue;

public class MessageDispatcher {
  private static final MessageType[] messageTypes = MessageType.values();

  private RobotController rc;
  private Robot robot;

  private Queue<Message> messageQueue = new ArrayDeque<>(16);

  private int possibleDelay = 0;

  private int totalCost = 0;
  private int messageCount = 0;

  private int lastHandledRound;

  private double secret1 = GeneratedData.MESSAGE_HASH_SECRET_1;
  private double secret2 = GeneratedData.MESSAGE_HASH_SECRET_2;
  private double secret3 = GeneratedData.MESSAGE_HASH_SECRET_3;
  private double secret4;
  private double secret5;

  public MessageDispatcher(RobotController rc, Robot robot) {
    this.rc = rc;
    this.robot = robot;

    lastHandledRound = rc.getRoundNum() - 1;

    secret4 = rc.getTeam().ordinal() + 1;
    secret5 = rc.getMapWidth();
  }

  public void addToBatch(Message message) {
    messageQueue.add(message);
  }

  public void sendBatch() throws GameActionException {
    if (messageQueue.isEmpty()) {
      return;
    }

    int price = getPrice();

    if (rc.getTeamSoup() < price) {
      return;
    }

    int hash = createHash(rc.getRoundNum());
    MessageData currentData = new MessageData(hash);

    while (!messageQueue.isEmpty()) {
      Message message = messageQueue.peek();
      int messageSize = message.getSize() + 1;

      if (!currentData.hasSpace(messageSize)) {
        rc.submitTransaction(currentData.getData(), price);

        if (rc.getTeamSoup() < price || messageQueue.isEmpty()) {
          currentData = null;
          break;
        }

        currentData = new MessageData(hash);
      }

      currentData.writeInt(message.getType().ordinal() + 1);
      message.write(currentData);
      messageQueue.poll();
    }

    if (currentData != null) {
      rc.submitTransaction(currentData.getData(), price);
    }
  }

  public void handleIncomingMessages() throws GameActionException {
    int currentRound = rc.getRoundNum();

    for (int i = lastHandledRound + 1; i < currentRound; i++) {
      handleIncomingMessages(i);
    }

    lastHandledRound = currentRound - 1;
  }

  private void handleIncomingMessages(int round) throws GameActionException {
    if (round <= 0) {
      return;
    }

    Transaction[] transactions = rc.getBlock(round);

    for (Transaction transaction : transactions) {
      totalCost += transaction.getCost();
      messageCount++;

      MessageData data = new MessageData(transaction.getMessage());

      if (!isHashValid(data.readInt(), round)) {
        continue;
      }

      while (data.hasValuesLeft()) {
        int typeIndex = data.readInt();

        if (typeIndex == 0) {
          break;
        }

        switch (messageTypes[typeIndex - 1]) {
          case SOUP_NEARBY:
            robot.onMessage(new SoupNearbyMessage(data));
            break;
          case ORDER:
            robot.onMessage(new OrderMessage(data));
            break;
          case ORDER_COMPLETED:
            robot.onMessage(new OrderCompletedMessage(data));
            break;
          case ORDER_CANCELED:
            robot.onMessage(new OrderCanceledMessage(data));
            break;
          case RUSH:
            robot.onMessage(new RushMessage(data));
            break;
        }
      }
    }

    if (transactions.length == GameConstants.NUMBER_OF_TRANSACTIONS_PER_BLOCK) {
      possibleDelay++;
    } else if (possibleDelay > 0) {
      possibleDelay--;
    }
  }

  private int createHash(int round) {
    return (int) (((((round * secret1) - secret2) * secret3) - secret4) * secret5);
  }

  private boolean isHashValid(int hash, int round) {
    for (int i = 0; i <= possibleDelay; i++) {
      if (checkHash(hash, round - i)) {
        return true;
      }
    }

    return false;
  }

  private boolean checkHash(int hash, int round) {
    double hashRound = (((((double) hash / secret5) + secret4) / secret3) + secret2) / secret1;
    return hashRound == round;
  }

  private int getPrice() {
    if (messageCount == 0) {
      return 1;
    }

    return (int) (Math.round((double) totalCost / (double) messageCount) + 1);
  }
}
