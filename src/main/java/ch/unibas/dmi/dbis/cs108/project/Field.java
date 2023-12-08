package ch.unibas.dmi.dbis.cs108.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Field GUI for playing chess
 */
public class Field implements ActionListener {

    static final double fieldHeight = 30;
    static final double fieldWidth = (fieldHeight / (Math.tan(Math.PI / 3)) * 2);
    static final int with = 800;
    static final int height = 800;
    private static final Logger LOGGER = LogManager.getLogger(Field.class);
    OutputStream out;
    LobbyGUI lobby;
    Lounge lounge;
    ClientThread clientThread;

    JFrame gameFrame = new JFrame();
    Point start;
    Point end;

    //position of chess pieces
    String[][] figures = null;

    //invisible buttons for playing the game
    JButton[][] buttons = null;

    JButton manual = new JButton("Manual");
    JButton leaveGame = new JButton("LeaveGame");

    //displays whose turn it is
    JTextArea yourTurn = new JTextArea("It is black's turn");
    JScrollPane turnPanel = new JScrollPane(yourTurn);

    //displays feedback of correct or wrong move
    JTextArea feedbackMove = new JTextArea();
    JScrollPane movePanel = new JScrollPane(feedbackMove);

    //JTextArea for tagging chess color
    JTextArea yourColorB = new JTextArea("manual"); //displays who black is
    JTextArea yourColorW = new JTextArea("manual"); //displays who white is
    JTextArea yourColorR = new JTextArea("manual"); //displays who red is
    //JScrollPane for JTextArea above
    JScrollPane colorPanelB = new JScrollPane(yourColorB);
    JScrollPane colorPanelW = new JScrollPane(yourColorW);
    JScrollPane colorPanelR = new JScrollPane(yourColorR);


    /**
     * constructor; creating GUI with chess figures at right place
     *
     * @param out OutputStream of Client
     * @param lobby lobby which will be played
     * @param clientThread Thread of the client
     * @param lounge lounge of client
     */
    public Field(OutputStream out, LobbyGUI lobby, ClientThread clientThread, Lounge lounge) {
        this.out = out;
        this.lobby = lobby;
        this.clientThread = clientThread;
        this.lounge = lounge;

        figures = new String[17][];

        for (int i = 0; i < 9; i++) {
            figures[i] = new String[9 + i];
        }
        for (int i = 0; i < 8; i++) {
            figures[i + 9] = new String[16 - i];
        }
        buttons = new JButton[17][];

        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton[9 + i];
        }
        for (int i = 0; i < 8; i++) {
            buttons[i + 9] = new JButton[16 - i];
        }

        //creates GUI of Field
        gameFrame.setContentPane(createPanel());
        // gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setLayout(new BorderLayout());
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[i].length; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].addActionListener(this);
                ToolsGUI.setBackgroundTransparent(buttons[i][j]);
                ToolsGUI.setBorderTransparent(buttons[i][j]);
                buttons[i][j].setVisible(true);
                gameFrame.add(buttons[i][j]);
            }
        }
        //design manual-Button
        manual.setFont(new Font("Arial", Font.BOLD, 15));
        manual.addActionListener(this);

        //design leaveGame-Button
        leaveGame.setFont(new Font("Arial", Font.BOLD, 15));
        leaveGame.addActionListener(this);

        //add manual-Button and various JTextArea for feedback of game
        gameFrame.add(manual);
        gameFrame.add(leaveGame);
        gameFrame.add(turnPanel);
        gameFrame.add(colorPanelB);
        gameFrame.add(colorPanelW);
        gameFrame.add(colorPanelR);
        gameFrame.add(movePanel);
        ToolsGUI.frameQuit(gameFrame,out,true);
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
    }

    /**
     * assigns a coordinate to the selected field
     *
     * @param row    row
     * @param column column
     * @return coordinates of field
     */
    public static Point getCoordinates(int row, int column) {
        double y = row * fieldHeight;
        double x;
        if (row < 9) {
            x = (5 * fieldWidth - (row * fieldHeight) / Math.tan(Math.PI / 3) + column * fieldWidth);
        } else {
            x = (5 * fieldWidth - (8 * fieldHeight) / Math.tan(Math.PI / 3) +
                    ((row - 8) * fieldHeight) / Math.tan(Math.PI / 3) + column * fieldWidth);
        }
        return new Point(x, y);
    }

    //no further use, just for testing
    public static void main(String[] args) {
        //f.buildField("Fld cx 000000000 0 0 0.bRock bPawn empty empty empty empty empty rPawn rRock@ bKnight bPawn bPawn empty empty empty empty rPawn rPawn rKnight@ bBishop bPawn bPawn empty empty empty empty empty rPawn rPawn rBishop@ bQueen bPawn bPawn empty empty empty empty empty empty rPawn rPawn rKing@ bBishop bPawn bPawn empty empty empty empty empty empty empty rPawn rPawn rBishop@ bKing bPawn bPawn empty empty empty empty empty empty empty empty rPawn rPawn rQueen@ bBishop bPawn bPawn empty empty empty empty empty empty empty empty empty rPawn rPawn rBishop@ bKnight bPawn bPawn empty empty empty empty empty empty empty empty empty empty rPawn rPawn rKnight@ bRock bPawn bPawn empty empty empty empty empty empty empty empty empty empty empty rPawn rPawn rRock@ bPawn bPawn empty empty empty empty empty empty empty empty empty empty empty empty bPawn rPawn@ empty empty empty empty empty empty empty empty empty empty empty empty empty empty empty@ empty empty empty empty empty empty empty empty empty empty empty empty empty empty@ empty empty empty empty empty empty empty empty empty empty empty empty empty@ empty empty empty empty empty empty empty empty empty empty empty empty@ empty wPawn wPawn wPawn wPawn wPawn wPawn wPawn wPawn wPawn empty@ wPawn wPawn wPawn wPawn wPawn wPawn wPawn wPawn wPawn wPawn@ wRock wKnight wBishop wQueen wBishop wKing wBishop wKnight wRock@$");
    }

    /**
     * updates Field after every move
     *
     * @param s move to be done
     */
    public void buildField(String s) {
        s = s.substring(15);
        String[] z = s.split("\\.");
        s = z[1];
        String[] rows = s.split("@");
        for (int i = 0; i < 17; i++) {
            figures[i] = rows[i].strip().split("\\s");
        }
        gameFrame.repaint();
    }

    /**
     * create a Panel for all chess figures
     *
     * @return returns the created Panel
     */
    private JPanel createPanel() {
        JPanel j = new JPanel() {
            BufferedImage imageFrame = null;

            BufferedImage imageGField = null;
            BufferedImage imageWField = null;
            BufferedImage imageBField = null;

            BufferedImage imageBKing = null;
            BufferedImage imageBQueen = null;
            BufferedImage imageBRook = null;
            BufferedImage imageBKnight = null;
            BufferedImage imageBPawn = null;
            BufferedImage imageBBishop = null;

            BufferedImage imageWKing = null;
            BufferedImage imageWQueen = null;
            BufferedImage imageWRook = null;
            BufferedImage imageWKnight = null;
            BufferedImage imageWPawn = null;
            BufferedImage imageWBishop = null;

            BufferedImage imageRKing = null;
            BufferedImage imageRQueen = null;
            BufferedImage imageRRook = null;
            BufferedImage imageRKnight = null;
            BufferedImage imageRPawn = null;
            BufferedImage imageRBishop = null;

            {
                try {
                    imageFrame = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Frame.png")));

                    imageGField = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Field3.png")));
                    imageWField = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Field2.png")));
                    imageBField = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Field1.png")));

                    imageBKing = ImageIO.read(Objects.requireNonNull(getClass().getResource("/bKing.png")));
                    imageBQueen = ImageIO.read(Objects.requireNonNull(getClass().getResource("/bQueen.png")));
                    imageBRook = ImageIO.read(Objects.requireNonNull(getClass().getResource("/bRook.png")));
                    imageBKnight =
                            ImageIO.read(Objects.requireNonNull(getClass().getResource("/bKnight.png")));
                    imageBPawn = ImageIO.read(Objects.requireNonNull(getClass().getResource("/bPawn.png")));
                    imageBBishop =
                            ImageIO.read(Objects.requireNonNull(getClass().getResource("/bBishop.png")));

                    imageWKing = ImageIO.read(Objects.requireNonNull(getClass().getResource("/wKing.png")));
                    imageWQueen = ImageIO.read(Objects.requireNonNull(getClass().getResource("/wQueen.png")));
                    imageWRook = ImageIO.read(Objects.requireNonNull(getClass().getResource("/wRook.png")));
                    imageWKnight =
                            ImageIO.read(Objects.requireNonNull(getClass().getResource("/wKnight.png")));
                    imageWPawn = ImageIO.read(Objects.requireNonNull(getClass().getResource("/wPawn.png")));
                    imageWBishop =
                            ImageIO.read(Objects.requireNonNull(getClass().getResource("/wBishop.png")));

                    imageRKing = ImageIO.read(Objects.requireNonNull(getClass().getResource("/rKing.png")));
                    imageRQueen = ImageIO.read(Objects.requireNonNull(getClass().getResource("/rQueen.png")));
                    imageRRook = ImageIO.read(Objects.requireNonNull(getClass().getResource("/rRook.png")));
                    imageRKnight =
                            ImageIO.read(Objects.requireNonNull(getClass().getResource("/rKnight.png")));
                    imageRPawn = ImageIO.read(Objects.requireNonNull(getClass().getResource("/rPawn.png")));
                    imageRBishop =
                            ImageIO.read(Objects.requireNonNull(getClass().getResource("/rBishop.png")));

                } catch (IOException ex) {
                    LOGGER.error("not found");
                }
            }

            /**
             * Paints the images at the wright position on the screen
             * @param g draws graphic
             */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                //creates button for calling manual
                manual.setLocation(500, 600);
                manual.setSize(130, 50);

                //creates button for quitting
                leaveGame.setLocation(500, 700);
                leaveGame.setSize(130, 50);

                //designs textArea and panel for displaying whose turn it is
                yourTurn.setFont(new Font("Arial", Font.BOLD, 20));
                yourTurn.setEditable(false);
                turnPanel.setLocation(600, 400);
                turnPanel.setSize(190, 40);

                //designs textArea and panel for displaying wrong or right move
                feedbackMove.setFont(new Font("Arial", Font.BOLD, 20));
                feedbackMove.setEditable(false);
                feedbackMove.setForeground(Color.RED);
                movePanel.setLocation(600, 445);
                movePanel.setSize(190, 40);

                //designs textArea and panel for displaying who is which color
                //tagging black and designing it's panel incl. area
                yourColorB.setFont(new Font("Arial", Font.BOLD, 15));
                yourColorB.setEditable(false);
                colorPanelB.setLocation(20, 80);
                colorPanelB.setSize(100, 30);
                //tagging white and designing it's panel incl. area
                yourColorW.setFont(new Font("Arial", Font.BOLD, 15));
                yourColorW.setEditable(false);
                colorPanelW.setLocation(275, 550);
                colorPanelW.setSize(100, 30);
                //tagging red and designing it's panel incl. area
                yourColorR.setFont(new Font("Arial", Font.BOLD, 15));
                yourColorR.setEditable(false);
                colorPanelR.setLocation(550, 80);
                colorPanelR.setSize(100, 30);

                try {
                    if (figures == null) {
                        figures = new String[17][];
                        for (int i = 0; i < 9; i++) {
                            figures[i] = new String[9 + i];
                        }
                        for (int i = 0; i < 8; i++) {
                            figures[i + 9] = new String[16 - i];
                        }
                    }
                    for (int i = 0; i < figures.length; i++) {
                        for (int j = 0; j < figures[i].length; j++) {
                            buttons[i][j].setLocation((int) getCoordinates(i, j).x,
                                    (int) (getCoordinates(i, j).y + fieldHeight / 6));
                            buttons[i][j].setSize((int) fieldWidth, (int) fieldHeight);
                            //draws fields
                            if (i < 9 && (i + j == 2 || i + j == 5 || i + j == 8 || i + j == 11 || i + j == 14 ||
                                    i + j == 17 || i + j == 20 || i + j == 23)) {
                                g.drawImage(imageWField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if ((i == 9 || i == 12 || i == 15) &&
                                    (j == 1 || j == 4 || j == 7 || j == 10 || j == 13)) {
                                g.drawImage(imageWField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if ((i == 10 || i == 13 || i == 16) &&
                                    (j == 2 || j == 5 || j == 8 || j == 11 || j == 14)) {
                                g.drawImage(imageWField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if ((i == 11 || i == 14) && (j == 0 || j == 3 || j == 6 || j == 9 || j == 12)) {
                                g.drawImage(imageWField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if (i < 9 && (i + j == 0 || i + j == 3 || i + j == 6 || i + j == 9 || i + j == 12 ||
                                    i + j == 15 || i + j == 18 || i + j == 21 || i + j == 24)) {
                                g.drawImage(imageGField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if ((i == 9 || i == 12 || i == 15) &&
                                    (j == 2 || j == 5 || j == 8 || j == 11 || j == 14)) {
                                g.drawImage(imageGField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if ((i == 10 || i == 13 || i == 16) &&
                                    (j == 0 || j == 3 || j == 6 || j == 9 || j == 12)) {
                                g.drawImage(imageGField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if ((i == 11 || i == 14) && (j == 1 || j == 4 || j == 7 || j == 10 || j == 13)) {
                                g.drawImage(imageGField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if (i < 9 && (i + j == 1 || i + j == 4 || i + j == 7 || i + j == 10 || i + j == 13 ||
                                    i + j == 16 || i + j == 19 || i + j == 22)) {
                                g.drawImage(imageBField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if ((i == 9 || i == 12 || i == 15) &&
                                    (j == 0 || j == 3 || j == 6 || j == 9 || j == 12 || j == 15)) {
                                g.drawImage(imageBField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if ((i == 10 || i == 13 || i == 16) &&
                                    (j == 1 || j == 4 || j == 7 || j == 10 || j == 13)) {
                                g.drawImage(imageBField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            if ((i == 11 || i == 14) && (j == 2 || j == 5 || j == 8 || j == 11)) {
                                g.drawImage(imageBField, (int) getCoordinates(i, j).x, (int) getCoordinates(i, j).y,
                                        (int) fieldWidth, (int) (fieldHeight + fieldHeight / 3), this);
                            }
                            //draws figures
                            if (!(figures[i][j].equals("empty"))) {
                                if ((figures[i][j].equals("bKing"))) {
                                    g.drawImage(imageBKing, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("bQueen"))) {
                                    g.drawImage(imageBQueen, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("bRock"))) {
                                    g.drawImage(imageBRook, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("bKnight"))) {
                                    g.drawImage(imageBKnight, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("bBishop"))) {
                                    g.drawImage(imageBBishop, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("bPawn"))) {
                                    g.drawImage(imageBPawn, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }

                                if ((figures[i][j].equals("wKing"))) {
                                    g.drawImage(imageWKing, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("wQueen"))) {
                                    g.drawImage(imageWQueen, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("wRock"))) {
                                    g.drawImage(imageWRook, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("wKnight"))) {
                                    g.drawImage(imageWKnight, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("wBishop"))) {
                                    g.drawImage(imageWBishop, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("wPawn"))) {
                                    g.drawImage(imageWPawn, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }

                                if ((figures[i][j].equals("rKing"))) {
                                    g.drawImage(imageRKing, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("rQueen"))) {
                                    g.drawImage(imageRQueen, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("rRock"))) {
                                    g.drawImage(imageRRook, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("rKnight"))) {
                                    g.drawImage(imageRKnight, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("rBishop"))) {
                                    g.drawImage(imageRBishop, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                                if ((figures[i][j].equals("rPawn"))) {
                                    g.drawImage(imageRPawn, (int) getCoordinates(i, j).x,
                                            (int) getCoordinates(i, j).y, (int) fieldHeight, (int) fieldHeight, this);
                                }
                            }
                        }
                        if (!(start == null)) {//frame that shows selected figure
                            g.drawImage(imageFrame, (int) (getCoordinates((int) start.x, (int) start.y)).x,
                                    (int) getCoordinates((int) start.x, (int) start.y).y, (int) fieldWidth,
                                    (int) (fieldHeight + fieldHeight / 3), this);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }

            /**
             * initializes panel size (hence frame too)
             *
             * @return Dimension initial size of frame
             */
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(Field.with, Field.height);
            }
        };

        //read JPanel coordinates

        return j;
    }

    /**
     * sends the move to the server
     *
     * @param s start Position
     * @param e end Position
     */
    public void sendMove(Point s, Point e) {
        int rs = (int) s.x;
        int cs = (int) s.y;
        int re = (int) e.x;
        int ce = (int) e.y;
        String tmp = "00000000";
        String z = "Mov cx " + tmp + "* " + rs + " " + cs + " " + re + " " + ce;
        tmp = tmp + Integer.toHexString(z.length());
        tmp = tmp.substring(tmp.length() - 8);
        z = "Mov cx " + tmp + "* " + rs + " " + cs + " " + re + " " + ce;
        System.out.println(z);
        try {
            out.write((z).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        this.start = null;
        this.end = null;
    }

    /**
     * every field has its own button when it gets pressed the field is selected
     *
     * @param e Button
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == manual) {  //new instance of ManualGUI, player can see how to play the game
            javax.swing.SwingUtilities.invokeLater(() -> {
                new ManualGUI();
            });
        } else if (e.getSource() == leaveGame) {
            gameFrame.setVisible(false);
            Tools.netMsg("L" + 1, "Lob", out);   //send message to Server that client left lobby
            lobby.setVisible(true); //back to LobbyGUI
            clientThread.setFieldNull();
        } else {  //move of a chess piece
            for (int i = 0; i < buttons.length; i++) {
                for (int j = 0; j < buttons[i].length; j++) {
                    if (e.getSource() == buttons[i][j]) {
                        if (start == null) {
                            start = new Point(i, j);
                            gameFrame.repaint();
                        } else if (!(start.x == new Point(i, j).x && start.y == new Point(i, j).y)) {
                            end = new Point(i, j);
                            sendMove(start, end);
                        }
                        break;
                    }
                }
            }
        }
    }
}
