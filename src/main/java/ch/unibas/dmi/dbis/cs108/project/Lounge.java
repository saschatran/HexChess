package ch.unibas.dmi.dbis.cs108.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Waiting room. Waiting for other players.
 */
public class Lounge extends JFrame implements ActionListener {

  StartMenu start;
  LobbyGUI lobby;

  String lobNum;
  String playNum = "0"; //current number of players (should be at least)
  String wait;    //messages such as "Welcome...", "number of players"
  JTextField welcome; //welcome message of wait
  JTextField current; //number of players in lobby as message

  JButton backToLobby;

  int width = 500;
  int height = 325;

  /**
   * initializes Lounge GUI
   *
   * @param start  StartMenu neede for username
   * @param lobby  LobbyGUI for going back to lobby
   * @param lobNum number of chosen lobby
   */
  public Lounge(StartMenu start, LobbyGUI lobby, String lobNum) {
    this.start = start;
    this.lobby = lobby;
    this.lobNum = lobNum;

    //design GUI of lounge
    ToolsGUI.lookAndFeel();
    //put username as titel in frame
    setTitle(start.name);
    //create Lounge GUI
    setContentPane(ToolsGUI.createBackgroundPanel("/start blue.png", width, height));
    add(pleaseWait());
    ToolsGUI.frameQuit(this,start.out, false);
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  /**
   * creates Panels for Lounge: button to go back, and "welcome"-message
   *
   * @return JPanel for GUI Lounge
   */
  private JPanel pleaseWait() {

    //design backToLobby button
    backToLobby = new JButton("Back to Lobby");
    backToLobby.setFont(new Font("Arial", Font.BOLD, 20));
    backToLobby.addActionListener(this);

    //put "backToLobby"-Button in a Panel
    JPanel buttons = new JPanel();
    ToolsGUI.setBackgroundTransparent(buttons);
    buttons.add(backToLobby);

    //TextField with welcome message in Lounge
    welcome = createTextField();
    welcome.setEditable(false);

    //TextField with message about number of current players in specific Lobby
    current = createTextField();
    current.setEditable(false);

    //combine all J's
    JPanel message = new JPanel(new BorderLayout());
    message.add(welcome, BorderLayout.NORTH);
    message.add(current, BorderLayout.CENTER);
    message.add(buttons, BorderLayout.SOUTH);
    ToolsGUI.setBackgroundTransparent(message);

    return message;
  }

  /**
   * creates JTextField with transparent Borders and Background
   *
   * @return returns created JTextField
   */
  private JTextField createTextField() {
    JTextField field = new JTextField(50);
    field.setHorizontalAlignment(JTextField.CENTER);
    field.setFont(new Font("Arial", Font.BOLD, 30));
    field.setForeground(Color.white);
    field.setEditable(false);
    ToolsGUI.setBackgroundTransparent(field);
    ToolsGUI.setBorderTransparent(field);

    return field;
  }

  /**
   * goes back to lobby if "backToLobby"-Button is pressed
   *
   * @param e ActionEvent for "backToLobby"-Button
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == backToLobby) {
      setVisible(false);  //lounge GUI "disappears"
      Tools.netMsg("L" + lobNum, "Lob", start.out);   //send message to Server that client left lobby
      lobby.feedback = "Welcome " + start.name + "!";
      lobby.message.setText(lobby.feedback);
      lobby.setVisible(true); //Lobby GUI appears
    }
  }
}
