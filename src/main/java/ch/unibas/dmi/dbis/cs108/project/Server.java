package ch.unibas.dmi.dbis.cs108.project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SERVER
 *
 * creates Thread for reading CLIENT
 * initializes HighScoreList
 * creates empty Lobbies, if needed
 *
 */
public class Server {
  private static final Logger LOGGER = LogManager.getLogger(Server.class);
  public static Vector<ServerThread> clients = new Vector<>();
  public static int cnt = 0;

  /**
   * starts Server and creates Client reading thread
   * IOException is caught.
   *
   * @param port is Integer and gets it from Call
   */
  public Server(int port) {

    try {
      LOGGER.info("Waiting for port " + port + "...");
      Net.usernames.clear();
      new HighScore();  //highScore initialized
      ServerSocket server = new ServerSocket(port);
      Net.createLobbies();
      while (true) {
        Socket client = server.accept();
        ServerThread eC = new ServerThread(Integer.toString(++cnt), client);
        Thread eCT = new Thread(eC);
        clients.add(eC);
        eCT.start();
        // creates new lobbies if needed for space reasons.
        if (Net.lobbies.size() * 3 < cnt - 3) {
          Net.createLobbies();
        }
      }
    } catch (IOException e) {
      LOGGER.error(e);
      System.exit(0);
    }
  }

}

