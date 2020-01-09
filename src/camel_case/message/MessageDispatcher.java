package camel_case.message;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.Transaction;
import camel_case.GeneratedData;
import camel_case.message.impl.SoupFoundMessage;
import camel_case.message.impl.SoupGoneMessage;
import camel_case.robot.Robot;

import java.util.ArrayDeque;
import java.util.Queue;

public class MessageDispatcher {
  private RobotController rc;
  private Robot robot;

  private Queue<Message> messageQueue = new ArrayDeque<>(16);

  private MessageType[] messageTypes = MessageType.values();

  private int totalCost = 0;
  private int messageCount = 0;

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

    int[] data = new int[GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH];
    data[0] = hash;
    int currentIndex = 1;

    while (!messageQueue.isEmpty()) {
      Message message = messageQueue.poll();
      int messageSize = message.getSize() + 1;

      if (currentIndex + messageSize >= data.length) {
        rc.submitTransaction(data, price);

        if (rc.getTeamSoup() < price || messageQueue.isEmpty()) {
          currentIndex = -1;
          break;
        }

        data = new int[GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH];
        data[0] = hash;
        currentIndex = 1;
      }

      data[currentIndex] = message.getType().ordinal() + 1;
      message.write(data, currentIndex + 1);
      currentIndex += messageSize;
    }

    if (currentIndex != -1) {
      rc.submitTransaction(data, price);
    }
  }

  public void handleIncomingMessages() throws GameActionException {
    int round = rc.getRoundNum() - 1;
    Transaction[] transactions = rc.getBlock(round);

    for (Transaction transaction : transactions) {
      totalCost += transaction.getCost();
      messageCount++;

      int[] data = transaction.getMessage();
      int dataLength = data.length;

      if (dataLength > 0 && checkHash(data[0], round)) {
        for (int dataIndex = 1; dataIndex < dataLength && data[dataIndex] != 0; dataIndex++) {
          switch (messageTypes[data[dataIndex] - 1]) {
            case SOUP_FOUND:
              SoupFoundMessage soupFoundMessage = new SoupFoundMessage(data, dataIndex + 1);
              dataIndex += soupFoundMessage.getSize();
              robot.onMessage(soupFoundMessage);
              break;
            case SOUP_GONE:
              SoupGoneMessage soupGoneMessage = new SoupGoneMessage(data, dataIndex + 1);
              dataIndex += soupGoneMessage.getSize();
              robot.onMessage(soupGoneMessage);
              break;
          }
        }
      }
    }
  }

  private int createHash(int round) {
    int team = rc.getTeam().ordinal() + 1;
    int key1 = GeneratedData.MESSAGE_HASH_KEY_1;
    int key2 = GeneratedData.MESSAGE_HASH_KEY_2;
    int key3 = GeneratedData.MESSAGE_HASH_KEY_3;

    return (((round * team) * key1) - key2) * key3;
  }

  private boolean checkHash(int hash, int round) {
    int team = rc.getTeam().ordinal() + 1;
    double key1 = GeneratedData.MESSAGE_HASH_KEY_1;
    double key2 = GeneratedData.MESSAGE_HASH_KEY_2;
    double key3 = GeneratedData.MESSAGE_HASH_KEY_3;

    double hashRound = ((((double) hash / key3) + key2) / key1) / team;
    return hashRound == round;
  }

  private int getPrice() {
    if (messageCount == 0) {
      return 1;
    }

    return (int) (Math.round((double) totalCost / (double) messageCount) + 1);
  }
}
