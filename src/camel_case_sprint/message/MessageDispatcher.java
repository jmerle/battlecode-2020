package camel_case_sprint.message;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Transaction;
import camel_case_sprint.GeneratedData;
import camel_case_sprint.message.impl.*;
import camel_case_sprint.robot.Robot;

import java.util.ArrayDeque;
import java.util.Queue;

public class MessageDispatcher {
  private RobotController rc;
  private Robot robot;

  private Queue<Message> messageQueue = new ArrayDeque<>(16);

  private MessageType[] messageTypes = MessageType.values();

  private int totalCost = 0;
  private int messageCount = 0;

  private int lastHandledRound = 0;

  public MessageDispatcher(RobotController rc, Robot robot) {
    this.rc = rc;
    this.robot = robot;
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
    for (int i = lastHandledRound + 1, iMax = rc.getRoundNum(); i < iMax; i++) {
      handleIncomingMessages(i);
    }

    lastHandledRound = rc.getRoundNum() - 1;
  }

  public void handleIncomingMessages(int round) throws GameActionException {
    if (round <= 0) {
      return;
    }

    Transaction[] transactions = rc.getBlock(round);

    for (Transaction transaction : transactions) {
      totalCost += transaction.getCost();
      messageCount++;

      MessageData data = new MessageData(transaction.getMessage());

      if (data.hasValuesLeft() && isHashValid(data.readInt(), round)) {
        while (data.hasValuesLeft()) {
          int typeIndex = data.readInt();

          if (typeIndex == 0) {
            break;
          }

          switch (messageTypes[typeIndex - 1]) {
            case SOUP_FOUND:
              robot.onMessage(new SoupFoundMessage(data));
              break;
            case SOUP_GONE:
              robot.onMessage(new SoupGoneMessage(data));
              break;
            case ORDER:
              robot.onMessage(new OrderMessage(data));
              break;
            case ORDER_COMPLETED:
              robot.onMessage(new OrderCompletedMessage(data));
              break;
            case START_RUSH:
              robot.onMessage(new StartRushMessage(data));
              break;
            case ENEMY_FOUND:
              robot.onMessage(new EnemyFoundMessage(data));
              break;
            case ENEMY_NOT_FOUND:
              robot.onMessage(new EnemyNotFoundMessage(data));
              break;
          }
        }
      }
    }
  }

  private int createHash(int round) {
    int secret1 = GeneratedData.MESSAGE_HASH_SECRET_1;
    int secret2 = GeneratedData.MESSAGE_HASH_SECRET_2;
    int secret3 = GeneratedData.MESSAGE_HASH_SECRET_3;
    int secret4 = rc.getTeam().ordinal() + 1;
    int secret5 = rc.getMapWidth();

    return ((((round * secret1) - secret2) * secret3) - secret4) * secret5;
  }

  private boolean isHashValid(int hash, int round) {
    for (int i = 0; i < 10; i++) {
      if (checkHash(hash, round - i)) {
        return true;
      }
    }

    return false;
  }

  private boolean checkHash(int hash, int round) {
    double secret1 = GeneratedData.MESSAGE_HASH_SECRET_1;
    double secret2 = GeneratedData.MESSAGE_HASH_SECRET_2;
    double secret3 = GeneratedData.MESSAGE_HASH_SECRET_3;
    double secret4 = rc.getTeam().ordinal() + 1;
    double secret5 = rc.getMapWidth();

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
