package ch.unibas.dmi.dbis.cs108.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * creates chat panel and receives messages
 */
public class Chat extends JPanel {
  JTextArea enteredText = new JTextArea(10, 32); // visible field in which text will be saved
  JTextField typedText = new JTextField(32); // visible field in which text will be written
  OutputStream out;

  /**
   * The chat GUI gets created when a Chat object is created. In simple terms:
   * Chat chat = new Chat(name,out) creates the GUI because all is in the constructor
   *
   * @param out OutputStream of client which calls the GUI
   */
  public Chat(OutputStream out) {
    super(new BorderLayout());
    ToolsGUI.lookAndFeel();
    JFrame frame = new JFrame();
    this.out = out;

    // sets the text panel as non editable and lightgray color
    enteredText.setEditable(false);
    enteredText.setBackground(Color.lightGray);

    // tells field typedText to listen to a specific action. In this case when hitting enter.
    typedText.addActionListener(this::actionPerformed);

    // adds the elements
    Container content = frame.getContentPane();
    content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
    content.add(typedText, BorderLayout.SOUTH);


    // display the window, with focus on typing box
    typedText.requestFocusInWindow();
    frame.setTitle("Chat");
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //prevents closing chat-window accidentally
    frame.pack();
    typedText.requestFocusInWindow();
    frame.setVisible(true);

  }

  /**
   * saves text input from typedText when enter pressed
   *
   * @param e action: Enter
   */
  public void actionPerformed(ActionEvent e) {

    // if text doesnt consist of only whitespaces.
    if (typedText.getText().trim().length() > 0) {

      // inserts text (=typedText) in enteredText
      String s = "[You]: " + typedText.getText();
      enteredText.insert(s + "\n", enteredText.getText().length());
      enteredText.setCaretPosition(enteredText.getText().length());
      System.out.println(typedText.getText());

      // puts text into OutputStream
      try {
        String tmp = "00000000"; //length of line in hexdec
        String z = "Txt cx " + tmp + typedText.getText();
        tmp = tmp + Integer.toHexString(z.length());    //////////

        //last eight digit visible
        tmp = tmp.substring(tmp.length() - 8);
        z = "Txt cx " + tmp + typedText.getText();
        out.write(z.getBytes(StandardCharsets.UTF_8));

      } catch (IOException ioException) {
      }
      typedText.setText("");
      typedText.requestFocusInWindow();
    }
  }

  /**
   * inserts text in Chat GUI. Needed for inserting text outside of this file.
   *
   * @param s text to be inserted.
   */
  public void insertText(String s) {
    enteredText.insert(s + "\n", enteredText.getText().length());
    enteredText.setCaretPosition(enteredText.getText().length());

  }
}







