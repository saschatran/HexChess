package ch.unibas.dmi.dbis.cs108.project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * generates, updates HighScoreList-file
 */
public class HighScore {

  private static final Logger LOGGER = LogManager.getLogger(HighScore.class);

  //saves score of player name and number of wins in format: "1;name"
  public static List<String> savedScores = new LinkedList<>();

  /**
   * constructor; fills savedScores with content of HighScoreList
   *
   * @throws IOException if filed can't be read or found
   */
  public HighScore() throws IOException {

    try {
      BufferedReader bufferedReader =
          new BufferedReader(new FileReader("src/main/resources/HighScoreList"));
      String line = "";
      while (line != null) {  //save all lines of HighScoreList to savedScores
        line = bufferedReader.readLine();
        savedScores.add(line);
      }
      savedScores.remove(savedScores.size() - 1);
      sortList();
      LOGGER.info(savedScores);
    } catch (FileNotFoundException e) {
      LOGGER.error("File not found");
    }
  }

  /**
   * adds either new player to HighScoreList or increases number of wins of player in HighScoreList
   *
   * @param name player who has won a game
   */
  public static void saveHighScore(String name) {
    int i;  //counter to check whether player is already in HighScoreList or not.

    for (i = 0; i < savedScores.size(); i++) {
      String[] savedPlayer = savedScores.get(i)
          .split(";");   //savedPlayer[0] = number of Wins, savedPlayer[1] = which player
      if (savedPlayer.length == 2) {
        if (name
            .equals(savedPlayer[1])) {  //player already exists in savedScores resp. HighScoreList
          savedPlayer[0] = String
              .valueOf(Integer.parseInt(savedPlayer[0]) + 1);  //increase number of wins by one
          savedScores.remove(i);
          savedScores
              .add(savedPlayer[0] + ";" + name);   //replace number of wins of specific player
          break;
        }
      } else {
        i = savedScores.size();
        break;
      }
    }

    if (i == savedScores.size()) {  //no player in savedScores found, hence new player
      savedScores.add("1;" + name);   //adds player to list
    }

    sortList();
    LOGGER.info(savedScores);

    saveToHighScoreList();
  }

  /**
   * saves savedScores on HighScoreList file
   *
   */
  public static void saveToHighScoreList() {
    try {
      PrintWriter writer = new PrintWriter(
          "src/main/resources/HighScoreList");
      for (int i = 0; i < savedScores.size(); i++) {  //write on HighScoreList file
        writer.println(savedScores.get(i));
      }
      writer.close();
    } catch (FileNotFoundException e) {
      LOGGER.error("File not found");
    }
  }

  /**
   * reorder savedScores in descending order of number of wins
   */
  public static void sortList() {
    savedScores.sort(Comparator.naturalOrder());  //scores in ascending order
    Collections.reverse(savedScores);   //scores in descending order
  }

  /**
   * prints List to chat window and to HighScore frame
   *
   * @param name the player how wants to get the list
   */
  public static void getList(String name) {
    //go through all Strings in savedScores
    for (int i = 0; i < savedScores.size(); i++) {
      String[] save = savedScores.get(i)
          .split(";");  //split number of wins and the player into separate Strings
      if (save.length == 2) { //savedScores is not empty
        String msg = "#" + (i + 1) + "  -----  " + save[1] + ":  " + save[0];
        Net.sendMsgBack(name, msg); //sends message in order to display highscore list
      }
    }
  }
}


