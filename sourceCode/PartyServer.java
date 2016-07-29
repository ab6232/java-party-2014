
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A multithreaded server that handles chat from multiple clients
 * 
 * @author Tyler S, Mitch S, Dave S, Mike L, Andrew B
 * @version 4/2/2014
 */
public class PartyServer extends JFrame
{
    ArrayList<Player> players = new ArrayList<Player>();
    ArrayList<Thread> chatters = new ArrayList<Thread>();
    int numPlayers = 0;
    int numReady = 0;
    /**
     * Main method creates a new object of PartyServer, which "starts" the program
     */
    public static void main(String [] args)
    {
        new PartyServer();
    }

    /**
     * Waits for new clients to join and runs the ChatHandler method for each socket
     */
    public PartyServer() 
    {
        final int PORT_NUMBER = 14623;
        int currentPlayer = numPlayers;

        
        
        
        try
        {
            System.out.println("Server IP: "+InetAddress.getLocalHost() + "\nPort: " + PORT_NUMBER);
            ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);

            while(true)
            {
                Socket clientSocket = serverSocket.accept();
                chatters.add(new ChatHandler(clientSocket));
            } 
        }
        catch(UnknownHostException uhe)
        {
            System.out.println("Could not find server.");
        }
        catch(IOException ioe)
        {
            System.out.println("Error on server");
            ioe.printStackTrace();
        }  
    }

    /**
     * Reads in each line and prints every line to each client using the print writers in the arraylist ObjectOutputStream
     */
    class ChatHandler extends Thread 
    { 
        private Socket clientSocket; //Current client's socket
        ObjectInputStream gameIn = null;
        ObjectOutputStream gameOut = null;

        /**
         * Constructor for ChatHandler class, starts the run method for each thread
         * 
         * @param   socket  the current client's socket
         */
        public ChatHandler(Socket socket)
        {
            clientSocket = socket;
            start();
        }

        /**
         * Creates a new BufferedReader and PrintWriter for each client, accepts client messages, then sends them to all other clients
         */
        public void run()
        {

            boolean keepOpen = true;
            try 
            {
                OutputStream outputStream = clientSocket.getOutputStream();
                gameOut = new ObjectOutputStream(outputStream);
                InputStream inputStream = clientSocket.getInputStream();
                gameIn = new ObjectInputStream(inputStream);

                //Represents the current player's information
                Player currentPlayer = null;

                Object objectIn = null;
                //Keep reading until exit message sent from client
                while(keepOpen)
                {
                    objectIn = gameIn.readObject();
                    System.out.println(objectIn);
                   
                    //If server receives a string message
                    if (objectIn instanceof String)
                    {
                        String message = (String)objectIn;
                        //If the message ends with has connected, do things to add the new player's name to players
                        if(message.length()>15 && message.substring(message.length()-14,message.length()).equals("has connected."))
                        {
                            String name = message.substring(0,message.length()-14);
                            currentPlayer = new Player(name);
                            players.add(currentPlayer);
                            numPlayers++;
                            for(int i=0; i<chatters.size(); i++)
                            {
                                ((ChatHandler)chatters.get(i)).gameOut.writeObject(name + " has connected");
                                ((ChatHandler)chatters.get(i)).gameOut.flush();
                                ((ChatHandler)chatters.get(i)).gameOut.reset();
                            }
                            //Tell all clients to update when a new player is added
                            for(int i=0; i<chatters.size(); i++)
                            {
                                ((ChatHandler)chatters.get(i)).gameOut.writeObject(players);
                                ((ChatHandler)chatters.get(i)).gameOut.flush();
                                ((ChatHandler)chatters.get(i)).gameOut.reset();
                            }
                        }
                        //If the client asks to send players, write the entire arraylist to the network
                        else if(message.equals("**sendPlayers"))
                        {
                            System.out.println("Sending players from server");
                            gameOut.writeObject(players);
                            gameOut.flush();
                            gameOut.reset();
                        }
                        //If the client wants to start games, wait for all players to say start games, then send the next game command
                        else if(message.equals("**startGames"))
                        {
                            numReady++;
                            System.out.println(numReady + " of " + numPlayers + " are ready for the next game");
                            if (numPlayers==numReady)
                            {
                                System.out.println("Telling clients to play next game");
                                for(int i=0; i<chatters.size(); i++)
                                {
                                    ((ChatHandler)chatters.get(i)).gameOut.writeObject(new String("**playNext"));
                                    ((ChatHandler)chatters.get(i)).gameOut.flush();
                                    ((ChatHandler)chatters.get(i)).gameOut.reset();
                                }
                                numReady = 0;
                            }
                            else
                            {  
                                System.out.println("Waiting for other clients to finish"); 
                            }
                        }
                        //If the client wants to add to its score simply run the add score method on the current player
                        else if(message.length()>9 && message.substring(0,10).equals("**addScore"))
                        {

                            currentPlayer.addScore(Integer.parseInt(message.substring(10,message.length())));

                        }
                        //If the client exited, tell all other clients that it exited
                        else if(message.equals("EXIT_SERVER"))
                        {
                            gameOut.writeObject(new String("**clientExited"));
                            gameOut.flush();
                            gameOut.reset();
                            keepOpen = false;
                            break;
                        }
                        //If its not a special situation, handle it as a chat and send the message to everyone and print it to the console
                        else {
                            for(int i=0; i<chatters.size(); i++)
                            {
                                ((ChatHandler)chatters.get(i)).gameOut.writeObject(new String(message));
                                ((ChatHandler)chatters.get(i)).gameOut.flush();
                                ((ChatHandler)chatters.get(i)).gameOut.reset();
                            }
                        }
                    }

                }
                
                //When the client is exited, remove them from both arraylists
                players.remove(currentPlayer);
                chatters.remove(currentPlayer);
            }
            catch (EOFException eofe)
            {
                //Do nothing when no more data in
            }
            catch (ClassNotFoundException cnfe)
            {
                System.out.println("Could not find class");
            }
            catch(UnknownHostException uhe)
            {
                System.out.println("Could not find server");
            }
            catch(IOException ioe)
            {
                System.out.println("Could write to server, a client exited!");
            }
            finally
            {
                try
                {
                    gameOut.close();
                    gameIn.close();
                    clientSocket.close();
                }
                catch(IOException ioe)
                {
                    System.out.println("Could not close");
                }
            }
        }
    }
}