
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
 * Game where you moving targets
 * 
 * @author Java Party 2014 Team
 * @version 4/29/14
 */
public class TargetShoot extends JPanel implements MouseListener, ActionListener, MouseMotionListener
{
    //Defines how long the game will be played for in seconds
    final int GAME_DURATION = 30; 
    
    Clip gunshot;
    BufferedImage background = null;
    BufferedImage target = null;
    BufferedImage crosshair = null;
    Font titleFont = new Font("SansSerif", Font.BOLD, 50);
    ObjectInputStream input = null;
    ObjectOutputStream output = null;
    int seconds = 0;
    int numHit = 0;
    int xCoordinate;
    int yCoordinate;
    int xMouse;
    int yMouse;
    int xClicked;
    int yClicked;
    boolean hitTarget = true;
    boolean gameOver = false;
    javax.swing.Timer timer;

    /**
     * Constructor for targetshoot, takes in an object input and output stream from the client for reading/writing
     * Also creates the interface
     * 
     * @param input the client's objectinputstream
     * @param output the client's objectoutputstream
     */
    public TargetShoot(ObjectInputStream in, ObjectOutputStream out)
    {
        input = in;
        output = out;

        //Show instructions for this game before it starts
        JOptionPane.showMessageDialog(this,"In this game, you target floating targets.\nEach target you click is worth 10 points\nYou have " + GAME_DURATION + " seconds. To click as\nmany targets as you can! Beware, a miss\nlooses you 5 points!","Target Shoot Rules", JOptionPane.INFORMATION_MESSAGE);

        try 
        {
            background = ImageIO.read(getClass().getResource("TargetShoot/targetshootbackground.png"));
            target = ImageIO.read(getClass().getResource("TargetShoot/target.png"));
            crosshair = ImageIO.read(getClass().getResource("TargetShoot/crosshair.png"));
        } 
        catch (IOException e) 
        {
            System.out.println("Could not load an image");
        }
        timer = new javax.swing.Timer(1000, this);
        timer.start(); 
        addMouseMotionListener(this);
        setCursor(getToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),"null"));
        addMouseListener(this);
        setSize(700,500);
        setVisible(true);
        repaint();
    }
    
    /**
     * Draws the targets randomly
     */
    public void paintComponent(Graphics page)
    {
        super.paintComponent(page);
        page.drawImage(background, 0, 0, null);
        if(hitTarget == true)
        {
            xCoordinate = (int)(Math.random() * 700);
            if(xCoordinate <= 50)
            {
                xCoordinate+=50;
            }
            if(xCoordinate >= 650)
            {
                xCoordinate-=50;
            }
            yCoordinate = (int)(Math.random() * 500);
            if(yCoordinate < 50)
            {
                yCoordinate+=50;
            }
            if(yCoordinate >= 450)
            {
                yCoordinate-=50;
            }
            page.drawImage(target, xCoordinate, yCoordinate, null);
            hitTarget = false;
        }
        page.drawImage(target, xCoordinate, yCoordinate, null);
        page.drawImage(crosshair, xMouse, yMouse, null);
        page.setFont(titleFont);
        page.setColor(Color.WHITE);
        page.drawString(""+(GAME_DURATION-seconds),20,50);
        page.drawString("Score: "+(numHit),420,50);
        if (gameOver)
        {
            page.drawString("Please wait for players", 20,200);
            page.drawString("to finish their game", 20,250);
        }
    }

    /**
     * When the mouse button is pressed, plays a sound. If coordinates match between mouse and target, then adds to the score
     */
    public void mousePressed(MouseEvent e) 
    {
        if(!gameOver)
        {
            try 
            {
                URL soundLoc = getClass().getResource("TargetShoot/gunshot.wav");
                AudioInputStream stream;
                AudioFormat format;
                DataLine.Info info;

                stream = AudioSystem.getAudioInputStream(soundLoc);
                format = stream.getFormat();
                info = new DataLine.Info(Clip.class, format);
                gunshot = (Clip)AudioSystem.getLine(info);
                gunshot.open(stream);
            }
            catch(Exception ee) 
            {
                System.out.println("Couldn't make sound");
            }
            gunshot.start();
            xClicked = e.getX();
            yClicked = e.getY();
            if(((xCoordinate <= xClicked) && (xClicked <= (xCoordinate + 50))) && (((yCoordinate <= yClicked) && (yClicked <= (yCoordinate + 50)))))
            {
                hitTarget = true;
                numHit+=10;
            }
            else{
                numHit-=5;
            }
            repaint();
        }
    }

    public void mouseReleased(MouseEvent e) 
    {
        //Do nothing
    }

    /**
     * Stores and updates mouse's x and y positions when the mouse moves
     */
    public void mouseMoved(MouseEvent e)
    {
        xMouse = e.getX() - 25; //change based in 1/2 size of crosshair
        yMouse = e.getY() - 25;
        repaint();
    }

    public void mouseDragged(MouseEvent e)
    {
        //Do nothing
    }

    public void mouseEntered(MouseEvent e) 
    {
        //Do nothing
    }

    public void mouseExited(MouseEvent e) 
    {
        //Do nothing
    }

    public void mouseClicked(MouseEvent e) 
    {
        //Do nothing
    }

    /**
     * ActionPerformed method for the timer increments the timer, stops the timer after 30 seconds and submits the scores to the server
     */
    public void actionPerformed(ActionEvent e) 
    {
        if (seconds>=GAME_DURATION)
        {
            try
            {
                if (!gameOver)
                {
                    output.writeObject(new String("**addScore" + numHit));
                    output.flush();
                    output.reset();
                    output.writeObject(new String("**startGames"));
                    output.flush();
                    output.reset();
                    gameOver = true;
                    timer.stop();
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