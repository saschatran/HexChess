package ch.unibas.dmi.dbis.cs108.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Creates the Lobby GUI
 */
public class LobbyGUI extends JFrame implements ActionListener {

  LinkedList<JButton> buttonList = new LinkedList<>();    //"lobbyButtons"

  JButton status; //lists status of game: e.g ongoing,...
  JButton highScore;  //lists highScores
  JButton players;  //lists all existing players

  //content of lists: status, highscores
  JTextArea listText;
  String listString = "";

  StartMenu start;
  OutputStream out;

  String feedback;    //"welcome" message, or when you can't enter lobby
  JTextField message; //field for String feedback

  int width = 650;
  int height = 425;

  /**
   * constructor
   *
   * @param start StartMenu will
   * @param out   OutputStream of ClientThread
   */
  public LobbyGUI(OutputStream out, StartMenu start) {
    //initializes variables
    this.start = start;
    this.out = out;

    //GUI design
    ToolsGUI.lookAndFeel();
    //put username as titel in frame
    setTitle(start.name);
    //create GUI of Lobby
    setContentPane(ToolsGUI.createBackgroundPanel("/start blue.png", width, height));    //background
    add(lobbyButton());

    ToolsGUI.frameQuit(this,out,false);

    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  /**
   * creates JPanel with all buttons: e.g. "Lobby 5", "changeName"...
   * including Panel with feedback messages
   *
   * @return JPanel main: any JPanels, JButtons or JTextField combined
   */
  public JPanel lobbyButton() {

    //create field for messages such as "Welcome..."
    message = new JTextField();
    message.setPreferredSize(new Dimension(width, height / 5));
    message.setHorizontalAlignment(JTextField.CENTER);
    message.setEditable(false);
    message.setForeground(Color.WHITE);
    message.setFont(new Font("Arial", Font.BOLD, 25));
    ToolsGUI.setBorderTransparent(message);
    ToolsGUI.setBackgroundTransparent(message);

    //JPanel for JTextField "message"
    JPanel feedback = new JPanel();
    feedback.add(message);
    ToolsGUI.setBackgroundTransparent(feedback);

    //initialize buttons for entering a lobby
    JButton button1 = new JButton();
    JButton button2 = new JButton();
    JButton button3 = new JButton();
    JButton button4 = new JButton();
    JButton button5 = new JButton();
    JButton button6 = new JButton();
    JButton button7 = new JButton();
    JButton button8 = new JButton();
    JButton button9 = new JButton();
    JButton button10 = new JButton();

    //add buttons to LinkedList "buttonList"
    buttonList.add(button1);
    buttonList.add(button2);
    buttonList.add(button3);
    buttonList.add(button4);
    buttonList.add(button5);
    buttonList.add(button6);
    buttonList.add(button7);
    buttonList.add(button8);
    buttonList.add(button9);
    buttonList.add(button10);

    String buttonName = "Lobby ";


    //Panel for all JButtons for entering lobby
    JPanel buttons = new JPanel(new GridLayout(0, 2, 20, 10));
    for (int i = 0; i < buttonList.size(); i++) {
      buttonList.get(i).setText(buttonName + i);    //label Buttons
      buttonList.get(i).setFont(new Font("Arial", Font.BOLD, 20));
      buttons.add(buttonList.get(i));     //add buttons to JPanel "buttons"
      buttonList.get(i).addActionListener(this); //adds to ActionListener
    }

    //creates Button to go back to StartMenu
    JButton changeName = new JButton("Change Username");
    changeName.setFont(new Font("Arial", Font.BOLD, 20));
    changeName.addActionListener(this);
    buttons.add(changeName);

    //creates and designs button for calling status of Lobbies
    status = new JButton("Lobby Status");
    status.setFont(new Font("Arial", Font.BOLD, 20));
    status.addActionListener(this);
    buttons.add(status);

    //creates and designs button for calling highScoreList
    highScore = new JButton("High Score");
    highScore.setFont(new Font("Arial", Font.BOLD, 20));
    highScore.addActionListener(this);
    buttons.add(highScore);

    //creates and designs button for calling all existing players
    players = new JButton("Players");
    players.setFont(new Font("Arial", Font.BOLD, 20));
    players.addActionListener(this);
    buttons.add(players);

    ToolsGUI.setBackgroundTransparent(buttons); //background of JPanel "buttons" is transparent


    //combine every Panel to one: feedback, buttons
    JPanel main = new JPanel(new BorderLayout());
    main.add(feedback, BorderLayout.NORTH);
    main.add(buttons, BorderLayout.SOUTH);
    ToolsGUI.setBackgroundTransparent(main);

    return main;
  }

  /**
   * Acts when a button is pressed.
   *
   * @param event ActionEvent
   */
  @Override
  public void actionPerformed(ActionEvent event) {

    //send message to server in order to enter lobby if possible
    for (int i = 0; i < buttonList.size(); i++) {
      if (event.getSource() == buttonList.get(i)) {   //find the pressed lobbyButton
        Tools.netMsg(String.valueOf(i), "Lob", out);
        return;
      }
    }
    if (event.getSource() == status) {  //opens frame with game status
      createStatus("#lobbies");
      return;
    } else if (event.getSource() == highScore) {  //opens frame with highscore
      createStatus("#highscores");
      return;
    }
    if (event.getSource() == players) { //opens frame with players
      createStatus("#players");
      return;
    }
    //if button changeName is pressed
    setVisible(false);
    start.frame.setVisible(true);
  }

  /**
   * updates status after every call
   *
   * @param msg Choice of which list should be displayed
   */
  public void createStatus(String msg) {

    listString = "";

    //displays content of specific list
    listText = new JTextArea();
    listText.setEditable(false);
    listText.setFont(new Font("Arial", Font.BOLD, 20));


    JScrollPane pane = new JScrollPane(listText); //scrollable Panel

    Tools.netMsg(msg, "Get", out);

    //creates frame for displaying lists
    JFrame statusList = new JFrame();
    statusList.setPreferredSize(new Dimension(width, height));
    statusList.pack();
    statusList.setLocationRelativeTo(null);
    statusList.setContentPane(pane);
    statusList.setVisible(true);
  }

}
