package ch.unibas.dmi.dbis.cs108.project;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * NETWORK-PROTOCOL
 *
 * find correct method to decode
 *
 * Bel: ping message
 * Ckm: message about who has won
 * Get: gets any kind of lists available
 * Lob: send messages within the Lobby
 * Mov: moves chess piece if possible
 * Nam: changes username if unique and saves it on server
 * QUI: sends message, that player wants to log out
 * Snd: broadcast message to all Clients
 * Txt: sends message, will be displayed on Chat-GUI
 */
public class EncodeNet {

    private static final Logger LOGGER = LogManager.getLogger(EncodeNet.class);

    /**
     * looks for the correct method which will be found in Net
     *
     * @param name  is individual username of Clients
     * @param input any text, msg and so on
     * @throws IOException sendMsg and sendMsgBack throw exception
     */
    public static void encodeNet(String name, String input) throws IOException {
        char[] packet = input.toCharArray();
        switch (packet[0]) {
            case 'B'://Bel: ping message
                Net.sendBel(name);
                break;
            case 'G': //Get: gets different Lists: lobbies, players, status (only on commandline), highscores
                Net.getList(name, input);
                break;
            case 'L': //Lob: broadcast messages to lobbyplayers
                input = input.substring(15);    //input == message without the numbers...
                if (input.charAt(0) == 'L') {   //leaving lobby resp. lounge
                    Net.leaveLobby(name);
                } else {
                    if (StringUtils.isNumeric(input)) { //makes sure this is really a number
                        Net.chooseLobby(Integer.parseInt(input), name); //entering lobby if possible
                    } else {
                        Net.sendMsgBack(name, "Wrong input!");
                    }
                }
                break;
            case 'M'://Mov: is used to send chess moves from the clients to the server.
                try {
                    String z = input.substring(15); //reads move e.g. "* 0 1 0 2"
                    String[] parts = z.split("\\s+");   //saves the four numbers
                    LOGGER.info(parts[1] + parts[2] + parts[3] + parts[4]);
                    for (Lobby lobby : Net.lobbies) {   //find correct lobby
                        if (lobby.lobbyPlayers.contains(name)) {    //found lobby
                            char c = lobby.getColor(name);
                            lobby.game.move(Integer.parseInt(parts[1]), //checks move and moves chess piece if possible
                                    Integer.parseInt(parts[2]),
                                    Integer.parseInt(parts[3]),
                                    Integer.parseInt(parts[4]), c, name);
                            Net.lobbyBroadcast(
                                    name + ": " + parts[1] + " " + parts[2] + " " + parts[3] + " " + parts[4] +
                                            "\n", lobby);
                        }
                    }
                } catch (Exception e) { //any typo that can occur means that syntax for moving a chess piece was wrong
                    e.printStackTrace();
                    Net.sendMsgBack(name, "wrong Syntax");
                    LOGGER.info("wrong Syntax");
                }
                break;
            case 'N'://Nam: resolve username
                Net.rename(name, input);
                break;
            case 'Q'://QUI: leaveGame, logOut user
                Net.deleteClient(name);
                break;
            case 'S'://Snd: broadcast to all Clients
                Net.broadcast(name, input);
                break;
            case 'T': //Txt: send messages (will be displayed on chat)
                Net.sendMsg(name, input);
                break;
        }
    }
}