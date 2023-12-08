package ch.unibas.dmi.dbis.cs108.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Game Logic of Hexchess
 */
public class Data {
  //initializes field
  public static final String EMPTY = "empty";
  static final int fieldHeight = 50;
  static final double fieldWidth = (fieldHeight / (Math.tan(Math.PI / 3)) * 2);
  static final int rowNum = 17;
  static final double arcTol = 0.02;
  static int c = 0;
  public String[][] figures = new String[17][];
  char currentColor = 'b';

  private static final Logger LOGGER = LogManager.getLogger(Data.class);

  Lobby lobby;
  String name;  //name of current Player

  //save checkmates
  boolean checkmateB = false;
  boolean checkmateW = false;
  boolean checkmateR = false;
  boolean hasSentB = false;
  boolean hasSentW = false;
  boolean hasSentR = false;

  /**
   * places the figures on the board
   *
   * @param name  Name of the game
   * @param lobby lobby in which the game will be played
   * @throws IOException ioE
   */
  public Data(String name, Lobby lobby) throws IOException {
    this.lobby = lobby;
    for (int i = 0; i < 9; i++) {
      figures[i] = new String[9 + i];
    }
    for (int i = 0; i < 8; i++) {
      figures[i + 9] = new String[16 - i];
    }
    for (int i = 0; i < figures.length; i++) {
      for (int j = 0; j < figures[i].length; j++) {
        figures[i][j] = EMPTY;
        c++;
      }
    }
    figures[0][0] = "bRock";
    figures[1][0] = "bKnight";
    figures[2][0] = "bBishop";
    figures[3][0] = "bQueen";
    figures[4][0] = "bBishop";
    figures[5][0] = "bKing";
    figures[6][0] = "bBishop";
    figures[7][0] = "bKnight";
    figures[8][0] = "bRock";

    figures[16][0] = "wRock";
    figures[16][1] = "wKnight";
    figures[16][2] = "wBishop";
    figures[16][3] = "wQueen";
    figures[16][4] = "wBishop";
    figures[16][5] = "wKing";
    figures[16][6] = "wBishop";
    figures[16][7] = "wKnight";
    figures[16][8] = "wRock";

    figures[0][8] = "rRock";
    figures[1][9] = "rKnight";
    figures[2][10] = "rBishop";
    figures[5][13] = "rQueen";
    figures[4][12] = "rBishop";
    figures[3][11] = "rKing";
    figures[6][14] = "rBishop";
    figures[7][15] = "rKnight";
    figures[8][16] = "rRock";

    for (int i = 0; i < 10; i++) {
      figures[i][1] = "bPawn";
    }
    for (int i = 1; i < 9; i++) {
      figures[i][2] = "bPawn";
    }
    figures[9][0] = "bPawn";

    for (int i = 1; i < 10; i++) {
      figures[14][i] = "wPawn";
    }
    for (int i = 0; i < 10; i++) {
      figures[15][i] = "wPawn";
    }
    for (int i = 0; i < 9; i++) {
      figures[i][7 + i] = "rPawn";
    }
    for (int i = 1; i < 10; i++) {
      figures[i][6 + i] = "rPawn";
    }
    figures[9][14] = "rPawn";
    sendField(name, 0, 0, 0, 0);
  }

  /**
   * assigns a coordinate to the selected field
   *
   * @param row    row
   * @param column column
   * @return coordinates of field
   */
  public static Point getCoordinates(int row, int column) {
    double y = row * fieldHeight;
    double x;
    if (row < 9) {
      x = (5 * fieldWidth - (row * fieldHeight) / Math.tan(Math.PI / 3) + column * fieldWidth);
    } else {
      x = (5 * fieldWidth - (8 * fieldHeight) / Math.tan(Math.PI / 3) +
          ((row - 8) * fieldHeight) / Math.tan(Math.PI / 3) + column * fieldWidth);
    }
    return new Point(x, y);
  }

  /**
   * returns the field whose coordinates are closest to the given point
   *
   * @param p inspecting coordinates of specific field
   * @return the point of the field
   */
  public static Point getFields(Point p) {
    double x = p.x;
    double y = p.y;
    int row;
    int column;
    row = (int) Math.round(y / fieldHeight);
    if (row < 9) {
      column = (int) Math.round((3 * x + Math.sqrt(3) * y - 3 * 5 * fieldWidth) / (3 * fieldWidth));
    } else {
      column = (int) Math.round(
          (3 * x - Math.sqrt(3) * y + 16 * fieldHeight * Math.sqrt(3) - 3 * 5 * fieldWidth) /
              (3 * fieldWidth));
    }
    return new Point(row, column);
  }

  /**
   * only for testing
   * @param args testing commands
   */
  public static void main(String[] args) {

    try {
      Data d = new Data("name", null);
      BufferedReader conin = new BufferedReader(new InputStreamReader(System.in));
      String line;
      while (true) {
        line = conin.readLine();
        String[] parts = line.split("\\s+");
        switch (parts[0]) {
          case "m": // command example: m 0 0 8 8 b name
            d.move(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), parts[5].toCharArray()[0],
                parts[4]);
            break;
          case "i": // command example: i 0 5 bRock
            d.figures[Integer.parseInt(parts[1])][Integer.parseInt(parts[2])] = parts[3];
            break;
          default:
            LOGGER.info("bad");
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      LOGGER.error(e);

    } catch (IOException e) {
      LOGGER.error(e);
      System.exit(1);
    }
  }

  /**
   * changes the current color
   * @throws IOException of Net
   */
  public void changeColor() throws IOException {
    if (currentColor == 'b') {
      if (!checkmateW) {
        currentColor = 'w';
      } else if (!checkmateR) {
        currentColor = 'r';
      }
      Net.sendCheckmate(currentColor + " turn", name);
      return;
    }
    if (currentColor == 'w') {
      if (!checkmateR) {
        currentColor = 'r';
      } else if (!checkmateB) {
        currentColor = 'b';
      }
      Net.sendCheckmate(currentColor + " turn", name);
      return;
    }
    if (currentColor == 'r') {
      if (!checkmateB) {
        currentColor = 'b';
      } else if (!checkmateW) {
        currentColor = 'w';
      }
      Net.sendCheckmate(currentColor + " turn", name);
    }
  }

  /**
   * Angle verification
   * @param arc arc as a cos value
   * @return if figure walks in 60, 120... deg direction
   */
  public boolean checkArc60(double arc) {
    return Math.abs(arc - 1) < arcTol || Math.abs(arc + 1) < arcTol ||
        Math.abs(arc - 0.5) < arcTol || Math.abs(arc + 0.5) < arcTol;
  }

  /**
   * Angle verification
   * @param arc arc as a cos value
   * @return if figure walks in 30, 90... deg direction
   */
  public boolean checkArc30(double arc) {
    return Math.abs(arc - (Math.sqrt(3) / 2)) < arcTol ||
        Math.abs(arc + (Math.sqrt(3) / 2)) < arcTol || Math.abs(arc - 0) < arcTol;
  }

  /**
   * gives Feedback (as in error message for any wrong move or confirm correct move)
   *
   * @param arc  move direction
   * @param name player name
   * @param msg  feedback message
   * @throws IOException io
   */
  public void feedback(double arc, String name, String msg) throws IOException {
    Net.lobMsg(name, "*" + msg, null, false);
    Net.sendMsgBack(name, msg);
    LOGGER.info(arc + msg);
  }

  /**
   * sends Field
   *
   * @param name   player name
   * @param start1 row of start field
   * @param start2 column of start field
   * @param end1   row of end field
   * @param end2   column of end field
   */
  public void sendField(String name, int start1, int start2, int end1, int end2) {
    String s = start1 + " " + start2 + " " + end1 + " " + end2 + ".";
    for (int i = 0; i < figures.length; i++) {
      for (int j = 0; j < figures[i].length; j++) {
        s = s + " " + figures[i][j];
        c++;
      }
      s = s + "@";
    }
    s = s + "$";
    Net.sendField(s, name);
  }

  /**
   * checks a move of a queen, rook or bishop
   *
   * @param stepLength distance between two fields
   * @param arc        move direction
   * @param start1     start row
   * @param start2     start column
   * @param end1       end row
   * @param end2       end column
   * @param start      start coordinates
   * @param end        end coordinates
   * @param name       player name
   * @param color      color of current move
   * @throws IOException IO
   */
  public void checkMove(double stepLength, double arc, int start1, int start2, int end1, int end2,
                        Point start, Point end, String name, char color) throws IOException {
    int c = 0;
    String hand = figures[start1][start2];
    figures[start1][start2] = EMPTY;
    for (double t = 0;
         t < fieldHeight * 2 * rowNum; ) { // checks whether there are figures on the path
      t = t + stepLength;

      if (getFields(start.line(end, t)).x == getFields(end).x &&
          getFields(start.line(end, t)).y == getFields(end).y) {
        break;
      }
      if (!((figures[(int) getFields(start.line(end, t)).x][(int) getFields(
          start.line(end, t)).y])
          .equals(EMPTY))) {
        c++;
      }
      if (c > 0) {
        break;
      }
    }
    if (c == 0 && !(hand.toCharArray()[0] == figures[end1][end2]
        .toCharArray()[0])) { //checks whether there is a piece of a different colour on the target field
      figures[end1][end2] = hand;
      checkCheck(name);
      {
        feedback(arc, name, "valid move");
        changeColor();
        sendField(name, start1, start2, end1, end2);
      }
    } else {
      figures[start1][start2] = hand;
      feedback(arc, name, " not a valid move blocked");
    }
  }

  /**
   * checks a move of a King or a Knight
   *
   * @param stepLength distance between two fields
   * @param arc        move direction
   * @param start1     start row
   * @param start2     start column
   * @param end1       end row
   * @param end2       end column
   * @param start      start coordinates
   * @param end        end coordinates
   * @param name       player name
   * @param color      color of current move
   * @throws IOException feedback; sendField;
   */
  public void checkLimitedMove(double stepLength, double arc, int start1, int start2, int end1,
                               int end2, Point start, Point end, String name, char color)
      throws IOException {
    if (getFields(start.line(end, stepLength)).x == getFields(end).x &&
        getFields(start.line(end, stepLength)).y == getFields(end).y) {
      if (!(figures[start1][start2].toCharArray()[0] == figures[end1][end2].toCharArray()[0])) {
        figures[end1][end2] = figures[start1][start2];
        figures[start1][start2] = EMPTY;
        checkCheck(name);
        {
          feedback(arc, name, "valid move");
          changeColor();
          sendField(name, start1, start2, end1, end2);
        }
      } else {
        feedback(arc, name, " not a valid move blocked");
      }
    } else {
      feedback(arc, name, " too far");
    }
  }

  /**
   * checks if pawn moves legally
   *
   * @param arc    move direction
   * @param start1 start row
   * @param start2 start column
   * @param end1   end row
   * @param end2   end column
   * @param name   player name
   * @param color  color of current move
   * @throws IOException feedback; sendField;
   */
  public void checkPawnMove(double arc, int start1, int start2, int end1, int end2, String name,
                            char color) throws IOException {
    if (figures[end1][end2].equals(EMPTY)) {
      figures[end1][end2] = figures[start1][start2];
      figures[start1][start2] = EMPTY;
      checkCheck(name);
      {
        feedback(arc, name, "valid move");
        changeColor();
        sendField(name, start1, start2, end1, end2);
      }
    } else {
      feedback(arc, name, " not a valid move blocked");
    }
  }

  /**
   * checks if pawn can capture a figure
   *
   * @param stepLength distance between two fields
   * @param arc        move direction
   * @param start1     start row
   * @param start2     start column
   * @param end1       end row
   * @param end2       end column
   * @param start      start coordinates
   * @param end        end coordinates
   * @param name       player name
   * @param color      color of current move
   * @throws IOException feedback; sendField;
   */
  public void checkPawnCapturing(double stepLength, double arc, int start1, int start2, int end1,
                                 int end2, Point start, Point end, String name, char color)
      throws IOException {
    if (getFields(start.line(end, stepLength)).x == getFields(end).x &&
        getFields(start.line(end, stepLength)).y == getFields(end).y) {
      if (!(figures[end1][end2].equals(EMPTY))) {
        if (!(figures[start1][start2].toCharArray()[0] == figures[end1][end2].toCharArray()[0])) {
          figures[end1][end2] = figures[start1][start2];
          figures[start1][start2] = EMPTY;
          checkCheck(name);
          {
            feedback(arc, name, "valid move");
            changeColor();
            sendField(name, start1, start2, end1, end2);
          }
        } else {
          feedback(arc, name, " not a valid move blocked");
        }
      } else {
        feedback(arc, name, "nothing to capture");
      }
    } else {
      feedback(arc, name, " too far");
    }
  }

  /**
   * moves a chess piece if the move is legal
   *
   * @param start1 start field row
   * @param start2 start field column
   * @param end1   end field row
   * @param end2   end field column
   * @param color  player color
   * @param name   name of playing player
   * @throws IOException Net.sendMsgBack()
   */
  public void move(int start1, int start2, int end1, int end2, char color, String name)
      throws IOException {
    if (!(currentColor == color)) {
      feedback(0,name, "not your turn");
      return;
    }
    if (!(figures[start1][start2].toCharArray()[0] == color)) {
      feedback(0,name, "not your color");
      return;
    }
    Point start = getCoordinates(start1, start2);
    Point end = getCoordinates(end1, end2);
    double arc = start.arc(end);
    switch (figures[start1][start2]) {
      case "bRock":
      case "wRock":
      case "rRock":
        if (checkArc60(arc)) { // checks the angle in the direction of which is moved
          checkMove(fieldWidth, arc, start1, start2, end1, end2, start, end, name, color);
        } else {
          feedback(arc, name, " not a valid move");
        }
        break;
      case "bBishop":
      case "wBishop":
      case "rBishop":
        if (checkArc30(arc)) {
          checkMove(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name, color);
        } else {
          feedback(arc, name, " not a valid move");
        }
        break;
      case "bQueen":
      case "wQueen":
      case "rQueen":
        if (checkArc60(arc)) {
          checkMove(fieldWidth, arc, start1, start2, end1, end2, start, end, name, color);
        } else {
          if (checkArc30(arc)) {
            checkMove(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name, color);
          } else {
            feedback(arc, name, " not a valid move");
          }
        }
        break;
      case "bKing":
      case "wKing":
      case "rKing":
        if (checkArc60(arc)) {
            checkLimitedMove(fieldWidth, arc, start1, start2, end1, end2, start, end, name, color);
        } else {
          if (checkArc30(arc)) {
              checkLimitedMove(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name,
                  color);
          } else {
            feedback(arc, name, " not a valid move");
          }
        }
        break;
      case "bKnight":
      case "wKnight":
      case "rKnight":
        if (Math.abs(arc - Math.cos(Math.PI / 9)) < arcTol ||
            Math.abs(arc - Math.cos(Math.PI / 9 * 2)) < arcTol ||
            Math.abs(arc - Math.cos(Math.PI / 9 * 4)) < arcTol ||
            Math.abs(arc - Math.cos(Math.PI / 9 * 5)) < arcTol ||
            Math.abs(arc - Math.cos(Math.PI / 9 * 7)) < arcTol ||
            Math.abs(arc - Math.cos(Math.PI / 9 * 8)) < arcTol) {
          checkLimitedMove(Math.round(Math.sqrt(21) * 2 / 3 * fieldHeight), arc, start1, start2,
              end1, end2, start, end, name, color);
        } else {
          feedback(arc, name, " not a valid move");
        }
        break;
      case "bPawn":
        // moving
        if (start1 <= end1 && (Math.abs(arc - 1) < arcTol || Math.abs(arc - 0.5) < arcTol)) {
          if (start2 == 1 || start2 == 2 && start1 > 0 && start1 < 9 || start1 == 9 &&
              start2 == 0) { //tests if pawn is in start position
            // moves the pawn two fields forward
            if (getFields(start.line(end, fieldWidth * 2)).x == getFields(end).x &&
                getFields(start.line(end, fieldWidth * 2)).y == getFields(end).y) {
              checkPawnMove(arc, start1, start2, end1, end2, name, color);
              return;
            }
          }
          // moves the pawn only one field forward
          if (getFields(start.line(end, fieldWidth)).x == getFields(end).x &&
              getFields(start.line(end, fieldWidth)).y == getFields(end).y) {
            checkPawnMove(arc, start1, start2, end1, end2, name, color);
          } else {
            feedback(arc, name, " too far");
          }
        } else {
          // capturing
          if (((Math.abs(getCoordinates(start1, start2).x - getCoordinates(end1, end2).x) < 0.1 &&
              start1 < end1) || (start2 < end2 && start1 > end1)) &&
              (Math.abs(arc - (Math.sqrt(3) / 2)) < arcTol || Math.abs(arc - 0) < arcTol)) {
            checkPawnCapturing(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name,
                color);
          } else {
            feedback(arc, name, " not a valid move");
          }
        }
        break;
      case "wPawn":
        if (start1 > end1 && (Math.abs(arc + 0.5) < arcTol || Math.abs(arc - 0.5) < arcTol)) {
          if (start1 == 15 || start1 == 14 && start2 > 0 &&
              start2 < 10) { // tests if pawn is in start position
            // moves the pawn two fields forward
            if (getFields(start.line(end, fieldWidth * 2)).x == getFields(end).x &&
                getFields(start.line(end, fieldWidth * 2)).y == getFields(end).y) {
              checkPawnMove(arc, start1, start2, end1, end2, name, color);
              return;
            }
          }
          if (getFields(start.line(end, fieldWidth)).x == getFields(end).x &&
              getFields(start.line(end, fieldWidth)).y == getFields(end).y) {
            checkPawnMove(arc, start1, start2, end1, end2, name, color);
          } else {
            feedback(arc, name, " too far");
          }
        } else {
          if ((start1 > end1 && start2 != end2) && (Math.abs(arc - (Math.sqrt(3) / 2)) < arcTol ||
              Math.abs(arc + (Math.sqrt(3) / 2)) < arcTol)) {
            checkPawnCapturing(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name,
                color);
          } else {
            feedback(arc, name, " not a valid move");
          }
        }
        break;
      case "rPawn":
        if (start1 <= end1 && (Math.abs(arc + 1) < arcTol || Math.abs(arc + 0.5) < arcTol)) {
          if (start2 == start1 + 7 && start1 < 9 ||
              start1 > 0 && start1 < 10 && start2 == start1 + 6 || start1 == 9 &&
              start2 == 14) { //tests if pawn is in start position
            // moves the pawn two fields forward
            if (getFields(start.line(end, fieldWidth * 2)).x == getFields(end).x &&
                getFields(start.line(end, fieldWidth * 2)).y == getFields(end).y) {
              checkPawnMove(arc, start1, start2, end1, end2, name, color);
              return;
            }
          }
          if (getFields(start.line(end, fieldWidth)).x == getFields(end).x &&
              getFields(start.line(end, fieldWidth)).y == getFields(end).y) {
            checkPawnMove(arc, start1, start2, end1, end2, name, color);
          } else {
            feedback(arc, name, " too far");
          }
        } else {
          if (((Math.abs(getCoordinates(start1, start2).x - getCoordinates(end1, end2).x) < 0.1 &&
              start1 < end1) || (start2 > end2 && start1 > end1)) &&
              (Math.abs(arc + (Math.sqrt(3) / 2)) < arcTol || Math.abs(arc - 0) < arcTol)) {
            checkPawnCapturing(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name,
                color);
          } else {
            feedback(arc, name, " not a valid move");
          }
        }
        break;
      default:
        Net.sendMsgBack(name, "no figure there");
        LOGGER.info("no figure there");
        break;
    }
  }

  /**
   * moves a chess piece if the move is legal
   * only for unit ch.unibas.dmi.dbis.cs108.tests (pls ignore)
   *
   * @param start1 start field row
   * @param start2 start field column
   * @param end1   end field row
   * @param end2   end field column
   * @param color  player color
   * @param name   name of playing player
   * @throws IOException Net.sendMsgBack()
   */
  public void moveTest(int start1, int start2, int end1, int end2, char color, String name)
      throws IOException {
    if (!(figures[start1][start2].toCharArray()[0] == color)) {
      Net.sendMsgBack(name, "not your color");
      LOGGER.info("not your color");
      return;
    }
    Point start = getCoordinates(start1, start2);
    Point end = getCoordinates(end1, end2);
    double arc = start.arc(end);
    switch (figures[start1][start2]) {
      case "bRock":
      case "wRock":
      case "rRock":
        if (checkArc60(arc)) { // checks the angle in the direction of which is moved
          checkMove(fieldWidth, arc, start1, start2, end1, end2, start, end, name, color);
        } else {
          feedback(arc, name, " not a valid move");
        }
        break;
      case "bBishop":
      case "wBishop":
      case "rBishop":
        if (checkArc30(arc)) {
          checkMove(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name, color);
        } else {
          feedback(arc, name, " not a valid move");
        }
        break;
      case "bQueen":
      case "wQueen":
      case "rQueen":
        if (checkArc60(arc)) {
          checkMove(fieldWidth, arc, start1, start2, end1, end2, start, end, name, color);
        } else {
          if (checkArc30(arc)) {
            checkMove(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name, color);
          } else {
            feedback(arc, name, " not a valid move");
          }
        }
        break;
      case "bKing":
      case "wKing":
      case "rKing":
        if (checkArc60(arc)) {
            checkLimitedMove(fieldWidth, arc, start1, start2, end1, end2, start, end, name, color);
        } else {
          if (checkArc30(arc)) {
              checkLimitedMove(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name,
                  color);
          } else {
            feedback(arc, name, " not a valid move");
          }
        }
        break;
      case "bKnight":
      case "wKnight":
      case "rKnight":
        if (Math.abs(arc - Math.cos(Math.PI / 9)) < arcTol ||
            Math.abs(arc - Math.cos(Math.PI / 9 * 2)) < arcTol ||
            Math.abs(arc - Math.cos(Math.PI / 9 * 4)) < arcTol ||
            Math.abs(arc - Math.cos(Math.PI / 9 * 5)) < arcTol ||
            Math.abs(arc - Math.cos(Math.PI / 9 * 7)) < arcTol ||
            Math.abs(arc - Math.cos(Math.PI / 9 * 8)) < arcTol) {
          checkLimitedMove(Math.round(Math.sqrt(21) * 2 / 3 * fieldHeight), arc, start1, start2,
              end1, end2, start, end, name, color);
        } else {
          feedback(arc, name, " not a valid move");
        }
        break;
      case "bPawn":
        // moving
        if (start1 <= end1 && (Math.abs(arc - 1) < arcTol || Math.abs(arc - 0.5) < arcTol)) {
          if (start2 == 1 || start2 == 2 && start1 > 0 && start1 < 9 || start1 == 9 &&
              start2 == 0) { //tests if pawn is in start position
            // moves the pawn two fields forward
            if (getFields(start.line(end, fieldWidth * 2)).x == getFields(end).x &&
                getFields(start.line(end, fieldWidth * 2)).y == getFields(end).y) {
              checkPawnMove(arc, start1, start2, end1, end2, name, color);
            }
          }
          // moves the pawn only one field forward
          if (getFields(start.line(end, fieldWidth)).x == getFields(end).x &&
              getFields(start.line(end, fieldWidth)).y == getFields(end).y) {
            checkPawnMove(arc, start1, start2, end1, end2, name, color);
          } else {
            feedback(arc, name, " too far");
          }
        } else {
          // capturing
          if (((start2 == end2 && start1 < end1) || (start2 < end2 && start1 > end1)) &&
              (Math.abs(arc - (Math.sqrt(3) / 2)) < arcTol || Math.abs(arc - 0) < arcTol)) {
            checkPawnCapturing(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name,
                color);
          } else {
            feedback(arc, name, " not a valid move");
          }
        }
        break;
      case "wPawn":
        if (start1 > end1 && (Math.abs(arc + 0.5) < arcTol || Math.abs(arc - 0.5) < arcTol)) {
          if (start1 == 15 || start1 == 14 && start2 > 0 &&
              start2 < 10) { // tests if pawn is in start position
            // moves the pawn two fields forward
            if (getFields(start.line(end, fieldWidth * 2)).x == getFields(end).x &&
                getFields(start.line(end, fieldWidth * 2)).y == getFields(end).y) {
              checkPawnMove(arc, start1, start2, end1, end2, name, color);
            }
          }
          if (getFields(start.line(end, fieldWidth)).x == getFields(end).x &&
              getFields(start.line(end, fieldWidth)).y == getFields(end).y) {
            checkPawnMove(arc, start1, start2, end1, end2, name, color);
          } else {
            feedback(arc, name, " too far");
          }
        } else {
          if ((start1 > end1 && start2 != end2) && (Math.abs(arc - (Math.sqrt(3) / 2)) < arcTol ||
              Math.abs(arc + (Math.sqrt(3) / 2)) < arcTol)) {
            checkPawnCapturing(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name,
                color);
          } else {
            feedback(arc, name, " not a valid move");
          }
        }
        break;
      case "rPawn":
        if (start1 <= end1 && (Math.abs(arc + 1) < arcTol || Math.abs(arc + 0.5) < arcTol)) {
          if (start2 == start1 + 7 && start1 < 9 ||
              start1 > 0 && start1 < 10 && start2 == start1 + 6 || start1 == 9 &&
              start2 == 14) { // tests if pawn is in start position
            // moves the pawn two fields forward
            if (getFields(start.line(end, fieldWidth * 2)).x == getFields(end).x &&
                getFields(start.line(end, fieldWidth * 2)).y == getFields(end).y) {
              checkPawnMove(arc, start1, start2, end1, end2, name, color);
            }
          }
          if (getFields(start.line(end, fieldWidth)).x == getFields(end).x &&
              getFields(start.line(end, fieldWidth)).y == getFields(end).y) {
            checkPawnMove(arc, start1, start2, end1, end2, name, color);
          } else {
            feedback(arc, name, " too far");
          }
        } else {
          if (((start2 == end2 && start1 < end1) || (start2 > end2 && start1 > end1)) &&
              (Math.abs(arc + (Math.sqrt(3) / 2)) < arcTol || Math.abs(arc - 0) < arcTol)) {
            checkPawnCapturing(fieldHeight * 2, arc, start1, start2, end1, end2, start, end, name,
                color);
          } else {
            feedback(arc, name, " not a valid move");
          }
        }
        break;
      default:
        feedback(0,name, "no figure there");
        break;
    }
  }

  /**
   * checks whether one king has been captured
   *
   * @param name  name of player who checks
   * @throws IOException clearBoard;
   */
  public void checkCheck(String name) throws IOException {
    this.name = name;
    int kings = 0;
    int rowB = -1, rowW = -1, rowR = -1;
    for (int i = 0; i < figures.length; i++) {
      for (int j = 0; j < figures[i].length; j++) {
        if (figures[i][j].equals("bKing")) {
          rowB = i;
          kings += 1;
        }
        if (figures[i][j].equals("wKing")) {
          rowW = i;
          kings += 2;
        }
        if (figures[i][j].equals("rKing")) {
          rowR = i;
          kings += 4;
        }
      }
    }
    if (rowB == -1 || checkmateB) {
      checkmateB = true;
      clearBoard('b');
      if (!hasSentB) {
        Net.sendCheckmate("Black has lost", name);
        hasSentB = true;
      }
    }
    if (rowW == -1 || checkmateW) {
      checkmateW = true;
      clearBoard('w');
      if (!hasSentW) {
        Net.sendCheckmate("White has lost", name);
        hasSentW = true;
      }
    }
    if (rowR == -1 || checkmateR) {
      checkmateR = true;
      clearBoard('r');
      if (!hasSentR) {
        Net.sendCheckmate("Red has lost", name);
        hasSentR = true;
      }
    }
    if ((kings & 1) == kings) {
      Net.sendCheckmate("Black has won", name);
      lobby.quitGame();
    }
    if ((kings & 2) == kings) {
      Net.sendCheckmate("White has won", name);
      lobby.quitGame();
    }
    if ((kings & 4) == kings) {
      Net.sendCheckmate("Red has won", name);
      lobby.quitGame();
    }
  }

  /**
   * clears all figures of one color
   *
   * @param color color
   */
  public void clearBoard(char color) {
    for (int i = 0; i < figures.length; i++) {
      for (int j = 0; j < figures[i].length; j++) {
        if (figures[i][j].charAt(0) == color) {
          figures[i][j] = EMPTY;
        }
      }
    }
  }
}

