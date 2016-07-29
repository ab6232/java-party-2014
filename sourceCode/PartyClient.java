
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Client for Java Party 2014. Holds the game that is being played and the chat
 * 
 * @author Tyler Swett, Mitch Steenburgh, Michael Lynch, Andrew Behrens
 * @version 4/15/2014
 */
public class PartyClient extends JFrame implements ActionListener, WindowListener, KeyListener 
{
    final int PORT_NUMBER = 14623;

    //Defines how many milleseconds to wait between checking for new player joins 
    final int UPDATE_INTERVAL = 500;

    ArrayList<Player> players = new ArrayList<Player>();
    Socket socket = null;
    JTextField input;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    String host;
    String name;
    Lobby gameLobby;
    JTextArea output;
    JPanel gamePanel;
    int currentGame = 0;

    /**
     * Main method starts program by running constructor for PartyClient
     */
    public static void main(String[] args)
    {
        new PartyClient();
    }

    /**
     * PartyClient constructor creates the layout and creates new connnection
     */
    public PartyClient()
    {
        setTitle("Java Party 2014");
        setLayout(new BorderLayout());
        JLabel chatTitle = new JLabel("In Game Chat", JLabel.CENTER);
        chatTitle.setForeground(Color.BLUE);
        chatTitle.setFont(new Font("Arial", Font.BOLD, 18));
        JPanel chatPanel = new JPanel(new BorderLayout());
        input = new JTextField();
        input.setBorder(new EtchedBorder());
        output = new JTextArea(30,20);
        output.setBorder(new EtchedBorder());
        JScrollPane outputPane = new JScrollPane(output);
        JPanel sendPanel = new JPanel(new BorderLayout());
        JButton send = new JButton("Send"); 
        sendPanel.add(input, BorderLayout.CENTER); 
        sendPanel.add(send, BorderLayout.EAST);
        output.setEditable(false);
        chatPanel.add(chatTitle, BorderLayout.NORTH);
        chatPanel.add(outputPane, BorderLayout.CENTER);
        chatPanel.add(sendPanel, BorderLayout.SOUTH);

        //Ask for host and name
        host = JOptionPane.showInputDialog("Enter an IP address or host name");
        name = JOptionPane.showInputDialog("Enter your name");
        //Create the new server sockets and in and out
        try {
            //ObjectWriter/Reader for the game communication
            socket = new Socket(host,PORT_NUMBER);
            OutputStream outputStream = socket.getOutputStream();
            out = new ObjectOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            in = new ObjectInputStream(inputStream);
        }

        catch(UnknownHostException uhe) {
            output.setText("Could not connect to host!");
        }
        catch(IOException ie) {
            output.setText("Error Communicating");
        }

        //Set initial game panel to the lobby
        gameLobby = new Lobby(in,out);
        //Set this variable to the panel you want to add then revalidate
        gamePanel = gameLobby;

        add(gamePanel, BorderLayout.CENTER);
        add(chatPanel, BorderLayout.EAST);

        setResizable(false);
        setSize(950,520);
        setLocationRelativeTo(null);
        send.addActionListener(this);
        input.addKeyListener(this);
        addWindowListener(this);
        setVisible(true);

        //Start Network Reading/Writing
        Reader th = new Reader();
        th.start();
    }

    /**
     * Tells the server that the client is leaving when the "X" button is hit
     */
    public void windowClosing(WindowEvent e) { 
        try
        {
            out.writeObject(new String(name + " has disconnected"));
            out.writeObject(new String("EXIT_SERVER")); 
            out.flush();
            out.reset();
            out.close();
        }
        catch(Exception exc)
        {
            System.out.println("ERROR: Close message not sent to server");
        }
        finally
        {
            System.exit(0);
        }
    } 

    public void windowOpened(WindowEvent e) { } 

    public void windowClosed(WindowEvent e) { } 

    public void windowActivated(WindowEvent e) { } 

    public void windowDeactivated(WindowEvent e) { } 

    public void windowIconified(WindowEvent e) { } 

    public void windowDeiconified(WindowEvent e) { } 

    /**
     * Looks for when the send button is pressed and sends the server the message in the chat box
     */
    public void actionPerformed(ActionEvent ae)
    {
        try 
        {
            String outLine = null;
            if (!((outLine = input.getText().trim()).equals(" ")))
            {
                out.writeObject(new String(name + ": " + outLine));
            }
            out.flush();
            out.reset();
            input.setText("");
        }
        catch (Exception exc)
        {
            System.out.println("Caught exception from sending message.");
            exc.printStackTrace();
        }
    }

    /**
     * Sends the message in the chat box to the server when the enter key is pressed
     */
    public void keyPressed(KeyEvent ke)
    {
        int enter = ke.getKeyCode();
        try
        {
            if(enter==KeyEvent.VK_ENTER)
            {
                String outLine = null;
                if (!((outLine = input.getText().trim()).equals(" ")))
                {
                    out.writeObject(new String(name + ": " + outLine));
                }
                out.flush();
                out.reset();
                input.setText("");
            }
        }
        catch (Exception exc)
        {
            System.out.println("Caught exception from sending message.");
            exc.printStackTrace();
        }
    }

    public void keyReleased(KeyEvent ke){}

    public void keyTyped(KeyEvent ke){}

    /**
     * Thread to read data coming in from the server
     */
    class Reader extends Thread
    {
        public Reader()
        {

        }

        public void run()
        {
            try
            {
                out.writeObject(new String("" + name + " has connected."));
                out.flush();
                out.reset();

                //Read messages from server, outputs to screen
                Object currentObject = null;
                while(true)
                {
                    currentObject = in.readObject();
                    //Check if the object is a string (either a message to display or a command)
                    if (currentObject instanceof String)
                    {
                        String message = (String)currentObject;

                        //If the server asks to update, request the players arraylist to be sent to client
                        if (message.equals("**updatePlease"))
                        {
                            //Command to send the party client the updated arraylist and in turn pass it
                            out.writeObject(new String("**sendPlayers"));
                            out.flush();
                            out.reset();
                            repaint();
                        }
                        //If any client exits display a warning
                        else if (message.equals("**clientExited"))
                        {
                            JOptionPane.showMessageDialog(null,"A client exited. Your game may be \nat jeopardy. Please restart client and server","Client Exited", JOptionPane.ERROR_MESSAGE);
                        }
                        //Swap in the next game in order
                        else if (message.equals("**playNext"))
                        {
                            remove(gamePanel);
                            gamePanel=null;
                            
                            //Add any new games here with a gameLobby between them. Be sure to reset the lobby and pass it the players
                            if (currentGame==0)
                            {
                                gamePanel = new IceCream(in,out);
                            }
                            else if (currentGame==1)
                            {
                                out.writeObject(new String("**sendPlayers"));
                                out.flush();
                                out.reset();
                                gameLobby.resetLobby();
                                gameLobby.updatePlayers(players);
                                gamePanel = gameLobby;
                            }
                            else if (currentGame==2)
                            {
                                gamePanel = new TargetShoot(in,out);
                            }
                            else if (currentGame==3)
                            {
                                out.writeObject(new String("**sendPlayers"));
                                out.flush();
                                out.reset();
                                gameLobby.resetLobby();
                                gameLobby.updatePlayers(players);
                                //Last game, so run show winners in the gamelobby
                                gameLobby.showWinners();
                                gamePanel = gameLobby;
                            }

                            add(gamePanel, BorderLayout.CENTER);
                            currentGame++;
                            invalidate();
                            validate();
                            repaint();
                        }
                        else
                        {
                            output.append((message) + "\n");
                        }
                    }

                    //Simply write the arraylist of players over the network to force an update in the lobby
                    else if (currentObject instanceof ArrayList<?>)
                    {
                        players = (ArrayList<Player>)currentObject;
                        gameLobby.updatePlayers(players);
                    }
                }
            }
            catch (ClassNotFoundException cnfe)
            {
                System.out.println("Couldn't find the class");
                cnfe.printStackTrace();
            }
            catch (EOFException eofe)
            {
                //Ignore when there's no more data!
            }
            catch (IOException ioe)
            {
                System.out.println("Couldn't read messages from server");
                ioe.printStackTrace();
            }
        }
    }

}
