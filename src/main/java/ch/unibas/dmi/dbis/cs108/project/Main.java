package ch.unibas.dmi.dbis.cs108.project;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * starts server and/or client
 * initializes username
 */
public class Main {

  private static final Logger LOGGER = LogManager.getLogger(Main.class);

  /**
   * .jar file calls Main: calls Server and Client
   *
   * @param args has to be either "server PORT" or "client HOSTADRESS:PORT [USERNAME (optional)]"
   */
  public static void main(String[] args) {

    LOGGER.trace("Entering application");
    //wrong args
    if (args.length < 2) {
      System.out
          .println("Usage: java -jar server PORT or java -jar client HOSTADRESS:PORT [USERNAME]");
      LOGGER.error("Wrong input to start jar!");
      return;
    }
    //start server
    if (args[0].equalsIgnoreCase("SERVER")) {

      int port = Integer.parseInt(args[1]);
      LOGGER.info("Started Server with port: " + port);
      new Server(port);

    } else if (args[0].equalsIgnoreCase("CLIENT")) {  //start client

      String name = "";
      int colon = args[1].indexOf(":");
      if (colon == -1) {  //no colon existent
        LOGGER.error("Usage: java -jar server PORT or java -jar client HOSTADRESS:PORT [USERNAME]");

      } else {
        String ip = args[1].substring(0, colon);
        int port = Integer.parseInt(args[1].substring(colon + 1));
        if (port > (1 << 16)) { //invalid port
          LOGGER.warn("Port must be in [1,65535].");
          return;
        }
        if (args.length == 3 &&
            (!(args[2] = Tools.strip(args[2], true)).equals(""))) {  //args consists of non-empty username
          name = args[2];
        } else {  //initialize username
          name = Client.getUname();
        }
        LOGGER.info("Started Client with port: " + port + " IP: " + ip + " Name: " + name);
        new Client(ip, port, name);
      }
    } else {    //wrong args
      LOGGER.error("Wrong input to start jar");
    }
  }
}
