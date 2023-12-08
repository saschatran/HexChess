package ch.unibas.dmi.dbis.cs108.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CLIENT
 *
 * creates Thread for reading from SERVER
 * reads from command line resp. terminal and sends message to SERVER
 */
public class Client {

  private static final Logger LOGGER = LogManager.getLogger(Client.class);
  public OutputStream out;
  Socket sock;
  InputStream in;
  String name;
  BufferedReader conIn =
      new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

  /**
   * starts main, creates Server reading thread
   *
   * @param ip   ip of user in String
   * @param port call port as Integer
   * @param name initial name
   */
  public Client(String ip, int port, String name) {
    LOGGER.info("CLIENT IS CONNECTING...");
    this.name = name;
    try {
      sock = new Socket(ip, port);
      LOGGER.info("Client connected to Server!");
      in = sock.getInputStream();
      out = sock.getOutputStream();
    } catch (IOException e) {
      LOGGER.error("Client did not connect to Server! " + e);
      System.exit(1);
    }
    read();
  }

  /**
   * gets desktop of Client resp. User
   *
   * @return name of desktop
   */
  public static String getUname() {
    //System.getenv(String name)
    //lin/mac: $USER
    //windows: %USERNAME%
    String ls = System.lineSeparator();
    if (ls.equals("\n")) {                      //linux or mac
      return System.getenv("USER");
    }
    return System.getenv("USERNAME");   //windows and other operating systems
  }

  /**
   * reads from Client then sends to Server
   */
  public void read() {

    try {
      LOGGER.info("CONNECTED");
      // Intellij solution for thread safety.
      javax.swing.SwingUtilities.invokeLater(() -> {
        Chat chat = new Chat(out);
        StartMenu start = new StartMenu(out, name);
        ClientThread th = new ClientThread(in, out, chat, start); // create server reading thread
        Thread iT = new Thread(th); // stream head
        iT.start();
      });
      //makes sure message is not a unitTest
      if (!name.equals("unitTest0")) {
        if (!name.equals("unitTest1")) {
          if (!name.equals("unitTest2")) {
            if (!name.equals("unitTest3")) {
              // reading head stream
              sendServer();
              // terminate program
              System.out.println("terminating ...");
              in.close();
              out.close();
              sock.close();
              System.exit(0);
            }
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error(e);
      System.exit(1);
    }
  }

  /**
   * reads text in cmd from Client and sends to Server
   *
   * @throws IOException is thrown if out.close()
   */
  public void sendServer() throws IOException {
    while (true) {
      String line = conIn.readLine();

      //deletes spaces at the beginning and end
      line = line.strip();
      if (line.length() > 0) {    //line is not empty
        String tmp = "00000000"; //length of line in hexdec
        if (line.equalsIgnoreCase("QUIT")) {    //terminate
          out.write(("QUI").getBytes(StandardCharsets.UTF_8));
          break;
        }
        cases(line, tmp); //"#", "!", ... catches messages from cmd
      }
    }
  }

  /**
   * is needed to send protocol to server and only used in unit ch.unibas.dmi.dbis.cs108.tests
   * (not used outside of Tests
   *
   * @param msg terminal like input of protocol (e.g "$ 1", to join lobby 1)
   * @throws IOException is thrown when either Socket, InputStream or OutputStream closes
   */
  public void sendServerTest(String msg) throws IOException {
    //deletes spaces at the beginning and end
    String line = msg.strip();
    if (line.length() > 0) {    //line is not empty
      String tmp = "00000000"; //length of line in hexdec
      cases(line, tmp);
    }
  }

  /**
   * is needed to send protocol to server and only used in unit ch.unibas.dmi.dbis.cs108.tests
   * * (not used outside of Tests
   *
   * @param msg msg to be send to Server)
   * @param net net protocol
   */
  public void sendServerNetTest(String msg, String net) {
    Tools.netMsg(msg, net, out);
  }

  /**
   * to check cases in which input will be determined
   * (was needed for unit test dont change pls)
   *
   * @param line line (exp. "$ 1")
   * @param tmp  the zeros
   * @throws IOException is thrown when either Socket, InputStream or OutputStream closes
   */
  private void cases(String line, String tmp) throws IOException {
    switch (line.toCharArray()[0]) {
      case '/':   //change username

        //delete first '/'
        line = line.substring(2);

        //save length of line
        tmp = tmp + Integer.toHexString(line.length() + 15);

        //last eight digit visible
        tmp = tmp.substring(tmp.length() - 8);
        line = "Nam cx " + tmp + line;
        break;
      case '\\':
        tmp = tmp + Integer.toHexString(line.length() + 15);
        tmp = tmp.substring(tmp.length() - 8);
        line = "Rec cx " + tmp + line.substring(1);
        break;
      case '*':   //make a move
        tmp = tmp + Integer.toHexString(line.length() + 15);
        tmp = tmp.substring(tmp.length() - 8);
        line = "Mov cx " + tmp + line;
        break;
      case '$':   //chose Lobby
        line = line.substring(1);
        line = line.strip();
        tmp = tmp + Integer.toHexString(line.length() + 15);
        tmp = tmp.substring(tmp.length() - 8);
        line = "Lob cx " + tmp + line;
        break;
      case '#':   //display lists, options are: status, lobbies, players
        tmp = tmp + Integer.toHexString(line.length() + 15);
        tmp = tmp.substring(tmp.length() - 8);
        line = "Get cx " + tmp + line;
        break;
      case '!':   //!all else it will be sent as message
        tmp = tmp + Integer.toHexString(line.length() + 15);
        tmp = tmp.substring(tmp.length() - 8);
        line = "Snd cx " + tmp + line;
        break;
      default:    //send as text message resp. chat
        tmp = tmp + Integer.toHexString(line.length() + 15);
        tmp = tmp.substring(tmp.length() - 8);
        line = "Txt cx " + tmp + line;
    }
    out.write(line.getBytes(StandardCharsets.UTF_8)); //write to Server
  }
}
