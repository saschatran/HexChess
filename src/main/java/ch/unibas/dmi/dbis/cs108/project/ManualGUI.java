package ch.unibas.dmi.dbis.cs108.project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * There is a JButton called "Manual" in the Lobby, Lounge and Field GUI. If this button is clicked on,
 * ManualGUI should be opened. It displays, how to enter a lobby and how the game is played with all the
 * game rules.
 */
public class ManualGUI extends JFrame implements ActionListener {

    //initialize frame size
    int width = 1000;
    int height = 650;

    //String array for all pages of Manual
    String[] pages = {"/ManualStart.png", "/Manual2.png", "/Manual3.png","/Manual4.png"};

    //get lastPage's number
    int lastPage = pages.length - 1;

    JPanel image;   //current image of GUI of manual
    JButton next = new JButton("NEXT");
    JButton previous = new JButton("PREVIOUS");

    public static int currentPage = 0; //states current currentPage

    /**
     * constructor
     * creates JFrame for displaying the Manual
     */
    public ManualGUI() {
        //adds Button ActionListener
        next.addActionListener(this);
        previous.addActionListener(this);

        //updates Page
        currentManual(currentPage);

        //design GUI
        setContentPane(image);
        add(previous);
        add(next);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * updates page of Manual
     * @param page page on which the player wants to be
     */
    private void currentManual(int page) {
        //image of Manual
        image = ToolsGUI.createBackgroundPanel(pages[page], width, height);

        //design JFrame GUI
        setContentPane(image);
        add(previous);
        add(next);

        //button next and previous should be visible, as long as player isn't on the first or last page
        if (page != 0 && page != lastPage) {    //page in between
            previous.setVisible(true);
            next.setVisible(true);
        } else if (page == 0)  {    //first page
            previous.setVisible(false);
        } else if (page == lastPage) {  //last page
            next.setVisible(false);
        }

        pack();
        setVisible(true);
    }

    /**
     * looks up which page should be displayed
     * @param e event listening to a button click (previous or next)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == next) {    //updates page number, going forward
            currentPage++;
            currentManual(currentPage);
        } else if (e.getSource() == previous) { //updates page number, going back
            currentPage--;
            currentManual(currentPage);
        }
    }
}
