package ch.unibas.dmi.dbis.cs108.project;

import static javax.swing.BoxLayout.Y_AXIS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Very first GUI to be seen. Username will be initialized here.
 */
public class StartMenu implements ActionListener {

  OutputStream out;
  String name;
  JFrame frame = new JFrame();
  String feedback;    //feedback on whether username is possible or not
  JTextField enterName = new JTextField();
  JButton enter = new JButton("ENTER");
  JTextArea filler = new JTextArea();

  //initializes frame size
  int width = 1000;
  int height = 650;

  /**
   * constructor
   * opens panel
   *
   * @param out  OutputStream from Client
   * @param name initial username, not saved yet
   */
  public StartMenu(OutputStream out, String name) {
    this.out = out;
    this.name = name;
    ToolsGUI.lookAndFeel();
    frame.setContentPane(ToolsGUI.createBackgroundPanel("/Schach blau.jpg", width, height));
    frame.setLayout(new BorderLayout());
    frame.add(createMainPanel(), BorderLayout.CENTER);
    ToolsGUI.frameQuit(frame,out, false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /**
   * Creates Panels and combine them into one mainPanel
   *
   * @return JPanel returns mainPanel: all Panels combined
   */
  private JPanel createMainPanel() {

    //create JPanel for JTextField "enterName"
    JPanel textField = new JPanel();
    textField.setPreferredSize(
        new Dimension(width, (int) (height / 1.2))); //set Dimension not variable yet
    ToolsGUI.setBackgroundTransparent(textField);

    //design JTextField "enterName"
    enterName.setText(name);
    enterName.setPreferredSize(new Dimension(width / 4, height / 15));
    enterName.setFont(new Font("Arial", Font.PLAIN, 20));
    enterName.setHorizontalAlignment(JTextField.CENTER);    //centered writing

    enter.setFont(new Font("Arial", Font.BOLD, 20));    //button sign size

    //add JTextField "enterName" and JButton "enter" to JPanel "textField"
    textField.add(enterName);
    textField.add(enter);

    //JPanel area for instructions and feedbacks referring to username
    JPanel area = new JPanel();
    area.setLayout(new BorderLayout());
    area.setPreferredSize(new Dimension(width, height / 4));
    ToolsGUI.setBackgroundTransparent(area);

    //create JTextField for instructions - not editable
    JTextField instruction = new JTextField("Enter username:");
    instruction.setForeground(new Color(250, 250, 250));    //set Font-Color white
    instruction.setHorizontalAlignment(JTextField.CENTER);
    instruction.setFont(new Font("Arial", Font.BOLD, 30));
    ToolsGUI.setBorderTransparent(instruction);
    ToolsGUI.setBackgroundTransparent(instruction);

    //design JTextArea "filler"
    ToolsGUI.setBackgroundTransparent(filler);
    instruction.setEditable(false);
    filler.setEditable(false);

    //add JTextField "instruction" and JTextArea "filler" to JPanel "area"
    area.add(filler, BorderLayout.NORTH);
    area.add(instruction, BorderLayout.SOUTH);

    //combines JPanel "textField" and JPanel "area" into one mainPanel
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, Y_AXIS));
    ToolsGUI.setBackgroundTransparent(mainPanel);
    mainPanel.add(area);
    mainPanel.add(textField);

    actionListener(this);   //actionListener for JButton "enter", JTextField "enterName"

    return mainPanel;
  }

  /**
   * collection of components for receiving ActionEvent
   *
   * @param listen ActionListener of StartMenu
   */
  public void actionListener(ActionListener listen) {
    enterName.addActionListener(listen);
    enter.addActionListener(listen);
  }

  /**
   * Here: saves input from enterName when either pressed enter or used the JButton enter
   *
   * @param action action: Enter or JButton enter
   */
  public void actionPerformed(ActionEvent action) {

    feedback = "Please enter username."; //if empty String
    if (enterName.getText().trim().length() > 0) { //no empty String
      Tools.netMsg(enterName.getText(), "Nam", out);
    }
  }
}
