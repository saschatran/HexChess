package ch.unibas.dmi.dbis.cs108.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * called by Server
 * read Client send to Server
 * System.out.print is on Server
 */
public class ServerThread implements Runnable {
  private static final Logger LOGGER = LogManager.getLogger(ServerThread.class);
  public static int nc = 0; //add number to username as suggestion
  public int timeout;
  public String name; //client's name
  public Socket socket;

  /**
   * constructor specifying name and gets socket from Server
   *
   * @param name   specific Username
   * @param socket Socket from Server
   */
  public ServerThread(String name, Socket socket) {
    this.name = name;
    this.socket = socket;
  }

  /**
   * reads text from Client cmd and send it to Server
   */
  public void run() {
    String input = "";
    LOGGER.info(name + " connected");

    try {
      InputStream in = socket.getInputStream();
      BufferedReader bin = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      int c = 0;

      //read Client send to Server
      while (c != -1) {
        while (input.length() < 3 && c != -1) { //first 3 letters
          c = bin.read();
          input = input + ((char) c);
        }
        if (input.toCharArray()[0] == 'B') { //Bel
          while (input.length() < 12) {
            c = bin.read();
            input = input + ((char) c);
          }
        } else {
          while (input.length() < 15 && c != -1) {
            c = bin.read();
            input = input + ((char) c);
          }
          long cx = Tools.hexDec(input.substring(7, 15).toCharArray()); //length of text
          while (input.length() < cx && c != -1) {
            c = bin.read();
            input = input + ((char) c);
          }
        }
        LOGGER.info(this.name + ": " + input);
        EncodeNet.encodeNet(name, input); //look up protocol
        input = "";
      }
      LOGGER.info("Terminated ->" + this.name);
      Net.usernames.remove(this.name);

    } catch (StringIndexOutOfBoundsException | SocketException e) {
      LOGGER.trace(this.name + " disconnected");
      Net.usernames.remove(this.name);

    } catch (IOException e) {
      LOGGER.error(e);

    }
  }
}