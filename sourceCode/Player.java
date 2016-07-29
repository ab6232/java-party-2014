
import java.io.*;
/**
 * Defines how a player is set up in the games
 * 
 * @author Java Party 2014 team
 * @version 4/15/2014
 */
public class Player implements Serializable
{
    String name = null;
    Integer score = null;

    /**
     * Constructor for player
     * 
     * @param String playerName defines the name of the player
     */
    public Player(String playerName)
    {
        name = playerName;
        score = new Integer(0);
    }

    /**
     * Gets the name of the selected player
     * 
     * @return String the player's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the score of the selected player
     * 
     * @return int the player's score
     */
    public int getScore()
    {
        return score;
    }

    /**
     * Adds to the user's score by the submitted amount
     * 
     * @param int scoreval the amount you want to incremement the player's score by
     */
    public void addScore(int scoreVal)
    {
        score+=scoreVal;
    }

    /**
     * Subtracts from the user's score by the submitted amount
     * 
     * @param int scoreval the amount you want to decrement the player's score by
     */
    public void subtractScore(int scoreVal)
    {
        score-=scoreVal;
    }

    /**
     * Sets the user's score to a desired amount
     * 
     * @param int scoreval the amount you want the player's score to be set to
     */
    public void setScore(int scoreVal)
    {
        score = scoreVal;
    }
}

