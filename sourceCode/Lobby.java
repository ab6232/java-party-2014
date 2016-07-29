
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;

/**
 * Shows a list of all of the players that are connected and their scores
 * 
 * @author Java Party 2014 Team
 * @version 4/24/14
 */
public class Lobby extends JPanel
{
    //Defines how many milleseconds to wait between checking for new player joins 
    final int SECONDS_BEFORE_START = 5;
    final int MIN_PLAYERS = 2;

    int currentSeconds = SECONDS_BEFORE_START;
    ArrayList<Player> players = new ArrayList<Player>();
    BufferedImage background = null;
    BufferedImage winner = null;
    Font titleFont = new Font("SansSerif", Font.BOLD, 25);
    Font hugeFont = new Font("SansSerif", Font.BOLD, 50);
    int playerStartPosition = 275;
    ObjectInputStream in=null;
    ObjectOutputStream out=null;
    boolean readyContinue = false;
    boolean showWinner = false;
    JButton continueButton;
    int highestIndex = 0;

    /**
     * Constructor for lobby, takes in an object input and output stream from the client for reading/writing
     * Also creates the interface
     * 
     * @param input the client's objectinputstream
     * @param output the client's objectoutputstream
     */
    public Lobby(ObjectInputStream input, ObjectOutputStream output)
    {
        in = input;
        out = output;

        setLayout(null);
        try 
        {
            background = ImageIO.read(getClass().getResource("Lobby/lobbyBackground.jpg"));
            winner = ImageIO.read(getClass().getResource("Lobby/winner.jpg"));
        } 
        catch (IOException e) 
        {
            System.out.println("Could not load an image");
        }
        setSize(700,500);
        setVisible(true);

        continueButton = new JButton("Continue");
        continueButton.setBounds(550,450,135,32);

        continueButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (!readyContinue)
                    {
                        //Let the games begin!
                        try
                        {
                            out.writeObject(new String("**startGames"));
                            out.flush();
                            out.reset();
                            readyContinue = true;
                            repaint();
                            continueButton.setEnabled(false);
                        }
                        catch (IOException ioe)
                        {
                            System.out.println("Couldn't write in lobby");
                        }
                    }
                }
            });

        add(continueButton);
    }

    /**
     * Makes the lobby change its appearance to show the winner more prominently
     */
    public void showWinners()
    {
        showWinner = true;
        background=winner;
        continueButton.setEnabled(false);
        remove(continueButton);
        int highestScore = 0;
        for(int i = 0; i < players.size(); i++)
        {
            if (players.get(i).getScore()>highestScore)
            {
                highestScore = players.get(i).getScore();
                highestIndex = i;
            }
        }
        repaint();
    }

    /**
     * Resets the parameters of the lobby that make it switch the game
     * This prevents the clients from switching right away after the lobby has already been displayed before
     */
    public void resetLobby()
    {
        readyContinue = false;
        continueButton.setEnabled(true);
        repaint();
    }

    /**
     * Because data cannot be read from within lobby class while chat is using it, pass in data about new players through this method
     */
    public void updatePlayers(ArrayList<Player> thePlayers)
    {
        players = thePlayers;
        repaint();
    }

    /**
     * Draws the strings and graphics for the lobby screen
     */
    public void paintComponent(Graphics page)
    {
        super.paintComponent(page);
        page.drawImage(background, 0, 0, null);
        page.setFont(titleFont);
        page.setColor(Color.WHITE);

        if (!showWinner)
        {
            if (players.size()>0)
            {
                for(int i = 0; i < players.size(); i++)
                {
                    int playerPosition=(25*i)+playerStartPosition;
                    page.drawString(((Player)(players.get(i))).getName() + " - Score:" + ((Player)(players.get(i))).getScore(),20,playerPosition);
                }
            }
            if (readyContinue)
            {
                page.drawString("Please wait for other players!", 50, 50);
            }
        }
        else
        {
            page.setFont(hugeFont);
            page.setColor(Color.BLACK);
            page.drawString("Winner: " + ((Player)(players.get(highestIndex))).getName(),20,75);
            page.setColor(Color.WHITE);
            page.drawString("Thanks for playing!",20,135);
            page.drawString("Restart to play again!",20,195);
            page.setFont(titleFont);
            for(int i = 0; i < players.size(); i++)
            {
                int playerPosition=(25*i)+playerStartPosition;
                page.drawString(((Player)(players.get(i))).getName() + " - Score:" + ((Player)(players.get(i))).getScore(),20,playerPosition);
            }
        }
    }

}
