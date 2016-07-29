
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;
import javax.sound.sampled.*;
import java.net.*;

/**
 * Game where you lick a lot of ice cream as fast as you can
 * 
 * @author Java Party 2014 Team
 * @version 4/24/14
 */
public class IceCream extends JPanel implements MouseListener, ActionListener
{
    //Defines how many seconds the game will be played for
    final int GAME_DURATION = 30;
    //Defines how many "licks" the user has to do before one scoop goes away
    final int LICKS_PER_SCOOP = 15;

    BufferedImage background = null;
    BufferedImage cone = null;
    BufferedImage cream = null;
    BufferedImage licker = null;
    BufferedImage licker1 = null;
    BufferedImage licker2 = null;
    ObjectInputStream input = null;
    ObjectOutputStream output = null;
    int dissolveAmnt = 0;
    int numClicks = 0;
    int seconds = 0;
    boolean gameOver = false;
    Clip lick1;
    Font titleFont = new Font("SansSerif", Font.BOLD, 50);
    javax.swing.Timer timer;

    /**
     * Constructor for Ice cream, takes in an object input and output stream from the client for reading/writing
     * Also creates the interface
     * 
     * @param input the client's objectinputstream
     * @param output the client's objectoutputstream
     */
    public IceCream(ObjectInputStream in, ObjectOutputStream out)
    {
        input = in;
        output = out;

        //Show instructions for this game before it starts
        JOptionPane.showMessageDialog(this,"This game is a race against the clock to\nsee who can lick the most ice cream in\n" + GAME_DURATION + " seconds. The game will begin right\nwhen you click ok.Click the left mouse \nbutton as fastas you can to lick.","Ice Cream Rules", JOptionPane.INFORMATION_MESSAGE);

        try {
            URL soundLoc = getClass().getResource("IceCream/lick1.wav");
            AudioInputStream stream;
            AudioFormat format;
            DataLine.Info info;

            stream = AudioSystem.getAudioInputStream(soundLoc);
            format = stream.getFormat();
            info = new DataLine.Info(Clip.class, format);
            lick1 = (Clip) AudioSystem.getLine(info);
            lick1.open(stream);

        }
        catch (Exception e) {
            System.out.println("Couldn't make sound");
        }

        try {
            background = ImageIO.read(getClass().getResource("IceCream/icecreambackground.jpg"));
            cone = ImageIO.read(getClass().getResource("IceCream/cone.png"));
            cream = ImageIO.read(getClass().getResource("IceCream/icecreamtop.png"));
            licker = ImageIO.read(getClass().getResource("IceCream/licker1.png"));
            licker1 = ImageIO.read(getClass().getResource("IceCream/licker1.png"));
            licker2 = ImageIO.read(getClass().getResource("IceCream/licker2.png"));
        } catch (IOException e) {
            System.out.println("Could not load an image");
        }
        timer = new javax.swing.Timer(1000, this);
        timer.start(); 
        addMouseListener(this);
        setSize(700,500);
        setVisible(true);
        repaint();
    }

    /**
     * Draws and makes the ice cream move based on the licksper scoop
     */
    public void paintComponent(Graphics page)
    {
        super.paintComponent(page);
        page.drawImage(background, 0, 0, null);
        page.drawImage(cone, 295, 350, null);
        page.drawImage(cream, 285, (260 + (((170/LICKS_PER_SCOOP)*dissolveAmnt)/2)), 170, 170-((170/LICKS_PER_SCOOP)*dissolveAmnt), null);
        for (int i=1; i<6; i++)
        {
            page.drawImage(cream, 285, (260 + ((-100*i)+((100/LICKS_PER_SCOOP)*dissolveAmnt))), null);
        }
        page.drawImage(licker, 415, 130, null);
        page.setFont(titleFont);
        page.setColor(Color.WHITE);
        page.drawString(""+numClicks,90,170);
        page.drawString(""+(GAME_DURATION-seconds),105,320);
        if (gameOver)
        {
            page.drawString("Please wait for players", 20,200);
            page.drawString("to finish their game", 20,250);
        }
    }

    /**
     * When the mouse button is pressed, change how much ice cream is licked and play a sound, change the licker's image
     */
    public void mousePressed(MouseEvent e) {
        if(!gameOver)
        {
            licker = licker2;
            lick1.start();
            dissolveAmnt = numClicks%LICKS_PER_SCOOP;
            repaint();
        }
    }

    /**
     * When the mouse button is released, reset the sound, add 1 to the score, and change the licker's image back to default
     */
    public void mouseReleased(MouseEvent e) {
        if(!gameOver)
        {
            numClicks++;
            licker = licker1;
            lick1.stop();
            lick1.setMicrosecondPosition(0);
            repaint();
        }
    }

    public void mouseEntered(MouseEvent e) {
        //Do nothing
    }

    public void mouseExited(MouseEvent e) {
        //Do nothing
    }

    public void mouseClicked(MouseEvent e) {
        //Do nothing
    }

    /**
     * ActionPerformed for the timer, imcrements the time and once the time is up stops the timer and submits the new scores to the server
     */
    public void actionPerformed(ActionEvent e) {
        if (seconds>=GAME_DURATION)
        {
            try
            {
                if (!gameOver)
                {

                    output.writeObject(new String("**addScore" + numClicks));
                    output.flush();
                    output.reset();
                    output.writeObject(new String("**startGames"));
                    output.flush();
                    output.reset();
                    gameOver = true;

                }
            }
            catch (IOException ioe)
            {
                System.out.println("Couldn't end this game");
                ioe.printStackTrace();
            }
        }
        else
        {
            seconds++;
        }
        repaint();
    }
}
