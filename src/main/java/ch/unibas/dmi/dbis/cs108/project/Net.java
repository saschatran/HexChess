package ch.unibas.dmi.dbis.cs108.project;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
NETWORK-PROTOCOL
Chats:
	1: Global		 // can be written on Chat-GUI or commandLine
	2: Lobby		 //(chat only receives messages from server, no client to client chat within Lobby)
	3: Direct		//Whisper Chat , start with @name

Player is uniquely identified (internally) by the upmost 64bit of the SHA(IP:Systime)
All networking commands start with a three letter long signature. Then an eight digit long hexadecimal
number follows which encodes the length of the whole command.
Finally a message or some parameters follow. A standard networking command looks like this: Txt cx 00000014hello

//no = number, b = bit
MSG-types:
	Bel no 64b  								//"Bell": PingPong
	Ckm no 64b  								// command outside of main game, catches messages about who has won
	Fld no 64b								// is used to send the chessboard information from the server to all clients.
	//is used to request information. A client can request a player list, a lobby list or a highscore list. (status on commandLine)
	Get no 64b
    Lob no 64b								// broadcast messages to lobbyplayers
    Mov no 64b 								//"Move": is used to send chess moves from the clients to the server.
    Nam no 64b  								//"Name": is used to change the player name.
    QUI no 64b								//someone quits over commandLine or closes main GUI-Panel, user logout
    Snd no 64b								// is used to send information form the server to all clients.
    //is used to send text messages. These messages will all be displaced in the chat window.
    Txt no 64b

@ = only SRV as receiver		    //Server
# = only CLT as receiver			//Client

        type Bel: @
            response: Bel no 64b Ans (=echo)
        type Cmk: #
            message: someone has won
            response: chessboard is cleared and all player go back to Lobby
        type Fld: #
            message: any kind of update for chessboard
            response: sends information to chessboard from server to all clients in the specified lobby, updates chessboard
        type Get: @
            input: lobbies, status, players, highscores
            response: displays specified list on Chat and defined GUI
        type Lob: @#
            message: enter lobby or leave lobby
            response: adds/deletes player from list in the specified lobby (@)
            updates chessboard (if game has already started) (#), sends message to all players in the same lobby who has left (received by #)
        type Mov: @
            input: "* [0-17] [0-17] [0-17] [0-17]"
            response: make a move or sends error message
        type Nam: @#
            message: change my name
            response: changes name if it is unique (@), sends confirmation or error message (received by #)
        type QUI: @#
            message: player wants to log out
            response: all client-sided threads closed (#), deletes the thread from list (@)
        type Snd: @#
            message: send message to everyone
            response: displays message on chat
        type Txt: @#
            message: any text that hasn’t been mentioned yet. (incl. broadcast)
            input: Txt cx (TEXT-len) 32b TEXT
            response: displays message on chat

*/

/**
 * all methods called by EncodeNet
 */
public class Net {

    private static final Logger LOGGER = LogManager.getLogger(Net.class);
    public static LinkedList<String> usernames = new LinkedList<String>();
    public static LinkedList<Lobby> lobbies = new LinkedList<Lobby>();

    /**
     * creates empty lobbies
     */
    public static void createLobbies() {
        for (int i = 0; i < 10; i++) {
            lobbies.add(new Lobby());
        }
    }

    /**
     * prints list of lobbies, usernames or game status to this specific user
     *
     * @param name specific Username
     * @param msg  check which list should be printed
     */
    public static void getList(String name, String msg) {
        msg = msg.substring(16);     //deletes #
        msg = msg.strip();  //makes sure there are no unnecessary spaces
        if (msg.equalsIgnoreCase("lobbies")) {  //sends list of existing lobbies and who is in there
            for (int i = 0; i < lobbies.size(); i++) {
                sendMsgBack(name, "Lobby " + i + ": " + Arrays.toString(lobbies.get(i).getPlayers()));
            }
        } else if (msg.equalsIgnoreCase("players")) {   //sends list of existing players in total
            sendMsgBack(name, "PLAYERS:" + Arrays.toString(usernames.toArray()));
        } else if (msg.equalsIgnoreCase("status")) {    //sends lists of status; ongoing, ...
            for (int i = 0; i < lobbies.size(); i++) {
                sendMsgBack(name, "Lobby " + i + ": " + lobbies.get(i).getStatus());
            }
        } else if (msg.equalsIgnoreCase("highscores")) {    //sends ranking of players
            HighScore.getList(name);
        } else {    //# should be meant as text and send to all Clients
            msg = "#" + msg;
            sendMsg(name, msg);
        }
    }

    /**
     * adds player to the chosen Lobby if possible
     *
     * @param i    says which Lobby
     * @param name specific User
     * @throws IOException addPlayer has sendMsgBack
     */
    public static void chooseLobby(int i, String name) throws IOException {
        if (i < lobbies.size()) {   //adds player to specified lobby if lobby exists
            lobbies.get(i).addPlayer(name, i);
        } else {
            sendMsgBack(name, "Lobby doesn't exist!");
        }
    }

    /**
     * Ping Pong
     * checks whether client has lost connection or not
     * @param name username of player
     */
    public static void sendBel(String name) {
        for (ServerThread cl : Server.clients) {    //iterates through all existing threads
            if (cl.name.equals(name)) { //found specified client
                cl.timeout = 0;
                break;
            }
        }
    }

    /**
     * send message to all Clients or one specific player
     *
     * @param name specific User
     * @param msg  message of User
     */
    public static void sendMsg(String name, String msg) {
        String input = msg.substring(15);   //start of text
        if (input.startsWith("@")) {  //check possibility of whisperchat
            input = input.substring(1); //deletes @ temporarily
            input = input.strip();
            String[] receiver = input.split("\\s+");    //find name
            if (usernames.contains(receiver[0])) {  //is meant to be a whisper chat to receiver[0]
                input = input.substring(receiver[0].length());  //without receiver name
                input = name + "(psst): " + input;        //add who sends it
                sendMsgBack(receiver[0], input);
                return;
            }
        }

        //not a whisper chat, send to all existing players
        for (ServerThread cl : Server.clients) {
            if (!cl.name.equals(name)) {    //print client's name to all other clients but oneself
                String tmp = "00000000";
                String z = "Txt cx " + tmp + name + ": " + msg.substring(15);
                tmp = tmp + Integer.toHexString(z.length());
                tmp = tmp.substring(tmp.length() - 8);
                z = "Txt cx " + tmp + name + ": " + msg.substring(15);
                OutputStream outCl = null;
                try {
                    outCl = cl.socket.getOutputStream();
                    outCl.write((z).getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
    }

    /**
     * sends message to all lobby players (max 3)
     *
     * @param msg   message to be send.
     * @param lobby lobby in which the message has to be send.
     */
    public static void lobbyBroadcast(String msg, Lobby lobby){
        for (ServerThread cl : Server.clients) {
            //print client's name to all other clients but oneself
            if (lobby.lobbyPlayers.contains(cl.name)) {
                String tmp = "00000000";
                String z = "Txt cx " + tmp + msg;   //sends move with name e.g. manual: 0 1 0 1
                tmp = tmp + Integer.toHexString(z.length());
                tmp = tmp.substring(tmp.length() - 8);
                z = "Txt cx " + tmp + msg;
                OutputStream outCl = null;
                try {
                    outCl = cl.socket.getOutputStream();
                    outCl.write((z).getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LOGGER.error("line 208 " + e);
                }
            }
        }
    }

    /**
     * needed for achievement
     *
     * @param name message to be send.
     * @param msg  expecting message from client
     */
    public static void broadcast(String name, String msg){
        sendMsg(name, msg);
    }

    /**
     * sends message back to this user, as in feedback
     *
     * @param name specific user
     * @param msg  message (usually error messages) to be send to the specific use
     */
    public static void sendMsgBack(String name, String msg) {
        for (ServerThread cl : Server.clients) {
            if (cl.name.equals(name)) { //found the player to be sent
                String tmp = "00000000";
                String z = "Txt cx " + tmp + msg;
                tmp = tmp + Integer.toHexString(z.length());
                tmp = tmp.substring(tmp.length() - 8);
                try {
                    OutputStream outCl = cl.socket.getOutputStream();
                    z = "Txt cx " + tmp + msg;
                    outCl.write((z).getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) { //if ServerThread hasn't been deleted properly yet
                    LOGGER.error("line 242 " + e);
                }
                System.out.println(z);
                LOGGER.info(z);
                break;
            }
        }
    }

    /**
     * sends the current field state to all clients in the lobby
     *
     * @param msg  field state
     * @param name name of player who made the move
     */
    public static void sendField(String msg, String name)  {
        Lobby lob = null;
        for (Lobby lobby : Net.lobbies) {   //find lobby
            if (lobby.lobbyPlayers.contains(name)) {    //found lobby
                lob = lobby;
                break;
            }
        }
        for (ServerThread cl : Server.clients) {
            if (lob.lobbyPlayers.contains(cl.name)) {   //found thread to the player
                String tmp = "00000000";
                String z = "Fld cx " + tmp + msg;
                tmp = tmp + Integer.toHexString(z.length());
                tmp = tmp.substring(tmp.length() - 8);
                z = "Fld cx " + tmp + msg;
                try {
                    OutputStream outCl = cl.socket.getOutputStream();
                    outCl.write((z).getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LOGGER.error("line 276 " + e);
                }
            }
        }
    }

    /**
     * sends a message to all clients in the lobby if one color is checkmate
     *
     * @param msg  checkmate message with color
     * @param name name of the player who checkmated the color
     * @throws IOException thrown when Socket, OutputStream or InputStream closes
     */
    public static void sendCheckmate(String msg, String name) throws IOException {
        Lobby lob = null;
        for (Lobby lobby : Net.lobbies) { //find correct lobby
            if (lobby.lobbyPlayers.contains(name)) {
                lob = lobby;
                break;
            }
        }
        try {
            for (ServerThread cl : Server.clients) {  //send to all players of lobby
                if (lob.lobbyPlayers.contains(cl.name)) {
                    OutputStream outCl = cl.socket.getOutputStream();
                    Tools.netMsg(msg, "Txt", outCl);    //message will be displayed on Chat
                    Tools.netMsg(msg, "Ckm", outCl);
                }
            }
        } catch (NullPointerException e) {
            LOGGER.error(e);
        }
        if (msg.endsWith("won")) {  //saves number of wins of winner to HighScoreList
            HighScore.saveHighScore(name);
        }

    }

    /**
     * changes username if entered username is unique
     *
     * @param name  specific User
     * @param input is the name to be changed in
     * @throws IOException out throws Exception when socket closes
     */
    public static void rename(String name, String input) throws IOException {
        //go through all clients
        for (ServerThread cl : Server.clients) {
            if (cl.name.equals(name)) {         //find client
                OutputStream out = cl.socket.getOutputStream();
                usernames.remove(cl.name);
                LOGGER.info(cl.name + " removed");
                String n = input.substring(15); //new username temporarily
                if (!usernames.contains(n)) { //new username is unique
                    cl.name = n;
                    usernames.add(cl.name);
                    sendMsgBack(cl.name, "Your username is : " + cl.name);  //username saved
                } else {
                    String oldie = n;   //keeps initial entered username
                    while (usernames.contains(n)) { //makes sure that username suggestion is indeed unique
                        n = oldie;
                        n = n + ServerThread.nc;
                        ServerThread.nc++;
                    }
                    Tools.netMsg("Username already exists you might name yourself " + n, "Txt", out);

                }
                break;
            }
        }
    }

    /**
     * deletes Player from specific Lobby
     *
     * @param name        player who has left lobby
     * @throws IOException is thrown when either Socket, InputStream or OutputStream is closed
     */
    public static void leaveLobby(String name) throws IOException {
        for (Lobby lob : Net.lobbies) {
            if (lob.lobbyPlayers.contains(name)) {
                lob.leaveLobby(name);
                break;
            }
        }
    }

    /**
     * catch msg of which color the player is (for GUI  reasons) and other messages to be caught and sent as "Lob"
     *
     * @param name name of player to who it should be sent
     * @param msg   message about which color the player is; e.g. "You are b!"
     * @param lobby source of message
     * @param allMembers whether message should be sent to all members or not
     * @throws IOException  exception of OutputStream outCl
     */
    public static void lobMsg(String name, String msg, Lobby lobby, boolean allMembers) throws IOException {
        for (ServerThread cl : Server.clients) {    //iterate through all clients
            if (allMembers) {   //send to all lobby members
                if (lobby.lobbyPlayers.contains(cl.name)) { //check if cl.name is member of this lobby
                    OutputStream outCl = cl.socket.getOutputStream();
                    Tools.netMsg(msg, "Lob", outCl);
                }
            } else {    //"Lob" message sent back
                if (name.equals(cl.name)) { //found the one
                    OutputStream outCl = cl.socket.getOutputStream();
                    Tools.netMsg(msg, "Lob", outCl);
                    return;
                }
            }
        }
    }

    /**
     * Quit function to delete client from server
     * @param name name of client
     */
    public static void deleteClient(String name) {
        for (ServerThread s: Server.clients) {
            if(s.name.equals(name)) {   //found logged out client
                Server.clients.remove(s);
                break;
            }
        }
        usernames.remove(name); //deletes name of client out of list
    }
}





