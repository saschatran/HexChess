package ch.unibas.dmi.dbis.cs108.project;

import java.io.IOException;
import java.util.LinkedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LOBBY
 *
 * initializes empty Lobbies
 * adds player to Lobby if possible
 * after end of game, Lobby is emptied
 */
public class Lobby {

  private static final Logger LOGGER = LogManager.getLogger(Lobby.class);

  // saves all players in Client array.
  public LinkedList<String> lobbyPlayers = new LinkedList<>();
  public char[] colors = {'b', 'w', 'r'}; // "Black, White, Red".
  public Data game;
  boolean status; //if lobby is free to join.
  String gameState;
  int countPlayer;

  /**
   * constructor creates empty Lobby
   */
  public Lobby() {
    status = false;
    gameState = "Haven't started yet...";
    countPlayer = 0;
  }


  /**
   * gets color of player
   * @param name player's name
   * @return  player's color
   */
  public char getColor(String name) {
    return colors[lobbyPlayers.indexOf(name) % 3];
  }

  /**
   * creates lobby, check status and save player to lobbies
   *
   * @param name   player joins chosen lobby
   * @param lobNum number of lobby
   * @throws IOException Net.sendMsgBack;
   */
  public void addPlayer(String name, int lobNum) throws IOException {
    if (!status) {
      lobbyPlayers.add(name);
      countPlayer++;
      Net.sendMsgBack(name, "You have joined lobby number: " + lobNum); //shows which lobby the player is in
      Net.lobbyBroadcast("Current number of players in this lobby: " + countPlayer, this);  //shows how many players already are in the lobby
      Net.sendMsgBack(name, "You are " + colors[countPlayer - 1] + "!");  //shows which color the player is
      LOGGER.info("Player: " + colors[countPlayer - 1] + " joined lobby number: " + lobNum);
      if (countPlayer == 3) { //3 players are in the lobby
        for (int i = 0; i < lobbyPlayers.size(); i++) { //send message to all players in lobby that game starts
          Net.lobMsg("", "#" + lobbyPlayers.get(0) + " " + lobbyPlayers.get(1) + " " + lobbyPlayers.get(2), this, true);  //send all players in color order b, w, r
          Net.sendMsgBack(lobbyPlayers.get(i), "Let's start the game!");
        }
        status = true;
        game = new Data(name, this);
        gameState = "Ongoing...";
      }
    } else {  //game is ongoing, no further person can enter this lobby
      Net.sendMsgBack(name, "Lobby's full. Please choose another one!");
      LOGGER.warn("Tried to join full Lobby number: " + lobNum);
    }
  }

  /**
   * gets all players in the specific lobby
   * @return list of players from a lobby
   */
  public String[] getPlayers() {
    return lobbyPlayers.toArray(new String[countPlayer]);
  }

  /**
   * function to leave lobby.
   * (temporary solution).
   *
   * @param name name of player who is leaving
   * @throws IOException is thrown when either Socket, InputStream or OutputStream is closed
   */
  public void leaveLobby(String name) throws IOException {
    if (countPlayer == 3) { //game has already started
      int index = lobbyPlayers.indexOf(name);
      //whoever has left the game wil be set checkmate
      if (colors[index] =='b') {
        game.checkmateB = true;
      } else if (colors[index] =='w') {
        game.checkmateW = true;
      } else if (colors[index] =='r') {
        game.checkmateR = true;
      }
      lobbyPlayers.set(index, "LEFT");  //replace the left player's name with "LEFT" (prevents NullPointerException)
      game.checkCheck(name);  //updates chessboard
      if (game.currentColor == colors[index]) { //color has to be changed
        game.changeColor();
      }
      Net.lobbyBroadcast(name + " left the game! Next's turn!", this);
      Net.sendMsgBack(name, "You have left the game!");
      return;
    }
    //else player is in the lounge
    Net.sendMsgBack(name, "You left the lobby!");
    lobbyPlayers.remove(name);
    countPlayer--;
    //sends message to the one person left in the lobby
    Net.lobbyBroadcast(name + " left the Lobby!", this);
    if (countPlayer >= 1) { //prevents arrayOutOfBoundsException
      Net.sendMsgBack(name, "You are " + colors[countPlayer - 1] + "!");
    }
    Net.lobbyBroadcast("Current number of players in this lobby: " + countPlayer, this);
  }

  /**
   * gets status of game
   * @return list of status of game as String
   */
  public String getStatus() {
    return gameState;
  }

  /**
   * Resets Lobby after finished Game with initial presets
   */
  public void quitGame() {
    status = false;
    gameState = "Haven't started yet...";
    countPlayer = 0;
    lobbyPlayers.clear();
  }
}
