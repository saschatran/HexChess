package ch.unibas.dmi.dbis.cs108.project;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * called by Client.
 * read Server send to Client
 * System.out.print is on Client
 */
public class ClientThread implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(ClientThread.class);
    InputStream in;
    OutputStream out;

    Chat chat;
    StartMenu start;
    LobbyGUI lobby;
    Lounge lounge;
    Field field = null;

    String colorNameB;
    String colorNameW;
    String colorNameR;

    /**
     * ClientThread constructor
     *
     * @param in    InputStream of Client
     * @param out   OutputStream of Client
     * @param chat  opens Chat-window
     * @param start starts Startmenu
     */
    public ClientThread(InputStream in, OutputStream out, Chat chat, StartMenu start) {
        this.out = out;
        this.in = in;
        this.chat = chat;
        this.start = start;
    }

    /**
     * read Server send to Client/s on Chat-window
     */
    public void run() {
        chat.insertText("Welcome to Hexchess!!!\n");
        chat.insertText("Use @name if you want to send via Whisper-Chat.");
        chat.insertText("If you play on command line,");
        chat.insertText("please change your name with /name.\n");
        String head = "";
        String text = "";
        try {
            BufferedReader bin = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            int c = 0;

            //read Server send to all Clients
            while (c != -1) {
                while (head.length() < 3 && c != -1) {  //read first 3 letters
                    c = bin.read();
                    head = head + ((char) c);
                }
                if (head.toCharArray()[0] == 'B') { //head == Bel
                    while (head.length() < 12 && c != -1) {
                        c = bin.read();
                        head = head + ((char) c);
                    }
                    out.write(("Bel pong ans").getBytes(StandardCharsets.UTF_8));
                } else if (head.toCharArray()[0] == 'F') { //field
                    while (!(Character.toString(c).equals("$")) && c != -1) {
                        c = bin.read();
                        head = head + ((char) c);
                    }
                    if (field == null) {
                        field = new Field(out, lobby, this, lounge);
                        field.gameFrame.setTitle(start.name);
                        field.yourColorB.setText(colorNameB);
                        field.yourColorW.setText(colorNameW);
                        field.yourColorR.setText(colorNameR);
                    }
                    field.buildField(head); //head in 'F'

                } else if (head.toCharArray()[0] == 'L') { //catch lobby message; basically which color the player is
                    while (head.length() < 15 && c != -1) { //head reads protocol -> "Txt cx 00000008"
                        c = bin.read();
                        head = head + ((char) c);
                    }
                    long cx = Tools.hexDec(head.substring(7, 15).toCharArray()); //length of text
                    while (text.length() < cx - 15 && c != -1) { //read text
                        c = bin.read();
                        text = text + ((char) c);
                    }
                    msgFromLobby(text); //catches players' Color and feedbackMsg of move

                } else if (head.toCharArray()[0] == 'C') { //checkmate message
                    while (head.length() < 15 && c != -1) { //head reads protocol -> "Txt cx 00000008"
                        c = bin.read();
                        head = head + ((char) c);
                    }
                    long cx = Tools.hexDec(head.substring(7, 15).toCharArray()); //length of text
                    while (text.length() < cx - 15 && c != -1) { //read text
                        c = bin.read();
                        text = text + ((char) c);
                    }
                    if (text.endsWith("won")) { //checkmate, game is finished, field panel should be resetted
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            field.gameFrame.setVisible(false);
                            lobby.setVisible(true);
                        });
                    } else if (text.endsWith(" turn")) {
                        char color = text.charAt(0);
                        if (color == 'b') {
                            field.yourTurn.setText("It is black's turn.");
                        } else if (color == 'w') {
                            field.yourTurn.setText("It is white's turn.");
                        } else if (color == 'r') {
                            field.yourTurn.setText("It is red's turn.");
                        }
                    }
                } else {
                    while (head.length() < 15 && c != -1) { //head reads protocol -> "Txt cx 00000008"
                        c = bin.read();
                        head = head + ((char) c);
                    }
                    long cx = Tools.hexDec(head.substring(7, 15).toCharArray()); //length of text
                    while (text.length() < cx - 15 && c != -1) { //read text
                        c = bin.read();
                        text = text + ((char) c);
                    }

                    feedbackUsername(text); //give feedback of username to StartMenu
                    //opens Lounge, updates current num of players in specific lobby, or feedback "lobby's full"
                    feedbackLobby(text);
                    lists(text);   //shows status on a JFrame in LobbyGUI


                    chat.insertText((text + "\n")); //Txt
                    System.out.println(text + "\n");
                }
                text = "";
                head = "";
            }
        } catch (Exception e) {     //used to be IOException which is true but Nullpointer happens too
            LOGGER.error("Disconnected " + e);
        }
    }

    /**
     * If username is unique, frame of StartMenu should become invisible.
     *
     * @param text any text from Server
     */
    public void feedbackUsername(String text) {
        String check = "Your username is : ";
        String opposite = "Username already exists you might name yourself ";
        //feedback whether username is unique or not
        if (text.startsWith(check)) {   //username is unique, lobby can be opened
            start.name = text.substring(check.length());   //saves unique username in StartMenu
            start.frame.setVisible(false);      //makes startMenu invisible
            //open lobby
            javax.swing.SwingUtilities.invokeLater(() -> {
                lobby = new LobbyGUI(out, start);
                lobby.feedback = "Welcome " + start.name + "!";
                lobby.message.setText(lobby.feedback);
            });
        } else if (text.startsWith(opposite)) {   //username is not unique
            start.name = text.substring(opposite.length());   //username suggestion saved in StartMenu
            start.enterName.setText(start.name);    //suggestion on TextField enteredText visible
            //show feedback on StartMenu GUI
            start.feedback = text;
            start.filler.setText(start.feedback);
            start.filler.setFont(new Font("Arial", Font.BOLD, 20));
            start.filler.setForeground(Color.RED);
        }
    }

    /**
     * gives feedback to either lounge or lobby if entering lobby is possible resp. not
     *
     * @param text any text from Server
     */
    public void feedbackLobby(String text) {
        //checks message from server
        String check = "You have joined lobby number: ";
        String full = "Lobby's full. Please choose another one!";
        String updatePlayNum = "Current number of players in this lobby: ";

        if (text.startsWith(check) || text.startsWith(updatePlayNum)) {   //lobby is not full
            //gets lobby number
            String lobNum = text.substring(check.length());
            lobby.setVisible(false);
            //opens lounge
            javax.swing.SwingUtilities.invokeLater(() -> {

                //entered lobby successfully, opens lounge
                if (text.startsWith(check)) {
                    lounge = new Lounge(start, lobby, lobNum);
                    lounge.wait = "Welcome to Lobby " + lounge.lobNum + "!";
                    lounge.welcome.setText(lounge.wait);
                } else {  //text starts with updatePlayNum
                    //get number of players in specific lobby
                    lounge.playNum = text.substring(updatePlayNum.length());

                    if (lounge.playNum.equals("3")) { //lounge to be set invisible, field will be invoked
                        //if client is last player needed, lounge doesn't need to be visible
                        lounge.setVisible(false);
                        return;
                    }

          /*
          message displayed in JTextField
          updates number of players if a second player enters the same lobby
          */
                    if (lounge.playNum.equals("1")) {   //check grammatical correctness
                        lounge.wait = "There is currently " + lounge.playNum + " player!";
                    } else {
                        lounge.wait = "There are currently " + lounge.playNum + " players!";
                    }
                    lounge.current.setText(lounge.wait);
                }
            });
        } else if (text.startsWith(full)) { //feedback in Lobby, that particular Lobby is full
            lobby.feedback = "Lobby's full. Please choose another one!";
            lobby.message.setText(lobby.feedback);
        } else if (text.startsWith(updatePlayNum)) {  //someone has left a lobby, playernumber has to be updated

            //get number of players in specific lobby
            lounge.playNum = text.substring(updatePlayNum.length());

            //message displayed in JTextField
            if (lounge.playNum.equals("1")) {   //check grammatical correctness
                lounge.wait = "There is currently " + lounge.playNum + " player!";
            } else {
                lounge.wait = "There are currently " + lounge.playNum + " players!";
            }
            lounge.current.setText(lounge.wait);
        }
    }

    /**
     * lists status, highScore on a GUI
     *
     * @param text check which should be displayed
     */
    public void lists(String text) {
        if (text.startsWith("Lobby ") || text.startsWith("#") ||
                text.startsWith("PLAYERS:")) {  //Lobby: lists status, #: lists HighScore, PLAYERS: lists list players
            lobby.listString += text + "\n";
            lobby.listText.setText(lobby.listString);
        }
    }

    /**
     * catches messages from lobby so that name can be tagged on Field GUI and your turn
     *
     * @param text caught message
     */
    public void msgFromLobby(String text) {
        if (text.startsWith("#")) { //catches message of which player is which color in the following order b, w, r
            text = text.substring(1); //deletes "#"
            String[] colorPlayers = text.split("\\s+"); //separates the three different players
            colorNameB = colorPlayers[0]; //save player with the black chess pieces
            colorNameW = colorPlayers[1]; //save player with the white chess pieces
            colorNameR = colorPlayers[2]; //save player with the red chess pieces
        } else if (text.startsWith("*")) {  //catch error message of wrong move resp. confirmed correct move
            if (field != null) {
                field.feedbackMove.setText(text.substring(1));
                return;
            }
            LOGGER.info("didn't work");
        }
    }

  /**
   * sets ones field to null
   */
  public void setFieldNull() {
        field = null;
    }
}

