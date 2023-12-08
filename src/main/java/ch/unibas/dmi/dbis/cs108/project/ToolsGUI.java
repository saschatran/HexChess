package ch.unibas.dmi.dbis.cs108.project;

import com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * methods that are used several times in many classes for GUI:
 *
 * - implements adjustable Background picture to JPanel
 * - Background of any JComponent can be set transparent
 * - Border of any JComponent can be set transparent
 * - design for GUI
 */
public class ToolsGUI {

  /**
   * Creates background-panel with background image from path in resources folder
   * background is adjustable to frame
   *
   * @param path   file path
   * @param width  width
   * @param height height
   * @return JPanel panel of background
   */
  public static JPanel createBackgroundPanel(String path, int width, int height) {
    return new JPanel() {
      BufferedImage image = null;

      {
        try {
          //gets picture from resources folder
          image = ImageIO.read(Objects.requireNonNull(getClass().getResource(path)));
        } catch (IOException ex) {
          System.out.println("not found");
        }
      }

      /**
       * Paints image to frame, making image adjustable.
       * @param g draws graphic
       */
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
      }

      /**
       * initializes panel size (hence frame too)
       * @return Dimension initial size of frame
       */
      @Override
      public Dimension getPreferredSize() {

        return new Dimension(width, height);
      }
    };
  }

  /**
   * Background of Panel, JTextField and other JComponent are made transparent
   *
   * @param component JComponent which needs transparent Background
   */
  public static void setBackgroundTransparent(JComponent component) {
    component.setOpaque(false);
    component.setBackground(new Color(0, 0, 0, 0)); //a == 0 leads to complete transparent color
  }

  /**
   * Background of Panel, JTextField and other JComponent are made transparent
   *
   * @param component JComponent which needs transparent Background
   */
  public static void setBorderTransparent(JComponent component) {
    component.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 0)));
  }

  /**
   * GUI skin
   */
  public static void lookAndFeel() {
    try {
      UIManager.setLookAndFeel(new FlatDarkFlatIJTheme());
    } catch (Exception ex) {
      System.err.println("Failed to initialize LaF");
    }
  }

  /**
   * When clicked on cross of closeOperation, confirmation request will be displayed.
   * If the player confirms the action, the player will be logged out.
   *
   * @param frame JFrame on which the clicked on the cross
   * @param out OutputStream of the "place" of JFrame frame
   * @param exit  if command is from Field.java so that chessboard is updated
   */
  public static void frameQuit(JFrame frame, OutputStream out, boolean exit) {
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        if(JOptionPane.showConfirmDialog(frame, "Are you sure ?") == JOptionPane.OK_OPTION){
          frame.setVisible(false);
          if (exit) { //chessboard has to be updated
            Tools.netMsg("L" + 1, "Lob", out);   //send message to Server that client left lobby
          }
          try {
            out.write(("QUI").getBytes(StandardCharsets.UTF_8));
          } catch (IOException ignored) {
          }
          frame.dispose();
          System.exit(0);
        }
      }
    });
  }
}
