import java.util.ArrayList;
import javafx.scene.paint.*;
import javafx.application.Platform;
import javax.swing.JOptionPane;

// The model represents all the actual content and functionality of the app
// For Breakout, it manages all the game objects that the View needs
// (the bat, ball, bricks, and the score), provides methods to allow the Controller
// to move the bat (and a couple of other fucntions - change the speed or stop 
// the game), and runs a background process (a 'thread') that moves the ball 
// every 20 milliseconds and checks for collisions 
public class Model 
{
    // First,a collection of useful values for calculating sizes and layouts etc.

    public int B              = 6;      // Border round the edge of the panel
    public int M              = 40;     // Height of menu bar space at the top

    public int BALL_SIZE      = 16;     // Ball side
    public int BRICK_WIDTH    = 40;     // Brick size
    public int BRICK_HEIGHT   = 20;

    public int BAT_MOVE       = 15;      // Distance to move bat on each keypress
    public int BALL_MOVE      = 4;      // Units to move the ball on each step

    public int HIT_BRICK      = 100;     // Score for hitting a brick
    public int HIT_BOTTOM     = -100;    // Score (penalty) for hitting the bottom of the screen
    public int HIT_BOTTOM2     = -1;     // Life (penalty) for hitting the bottom of the screen
    public int LIVES          = 5;      // The number of lives the player has

    // The other parts of the model-view-controller setup
    View view;
    Controller controller;

    // The game 'model' - these represent the state of the game
    // and are used by the View to display it
    public GameObj ball;                // The ball
    public ArrayList<GameObj> bricks;   // The bricks
    public GameObj bat;                 // The bat
    public int score = 0;               // The score
    public int highscore = 0;           // The highscore
    public int lives = 5;               // Number of lives
    

    // variables that control the game 
    public boolean gameRunning = true;  // Set false to stop the game
    public boolean fast = false;        // Set true to make the ball go faster

    // initialisation parameters for the model
    public int width;                   // Width of game
    public int height;                  // Height of game

    // CONSTRUCTOR - needs to know how big the window will be
    public Model( int w, int h )
    {
        Debug.trace("Model::<constructor>");  
        width = w; 
        height = h;

        initialiseGame();
    }

    // Initialise the game - reset the score and create the game objects 
    public void initialiseGame()
    {       
        score = 0;
        highscore= 0;
        LIVES = 3;
        ball   = new GameObj(width/2, height/2, BALL_SIZE, BALL_SIZE, Color.RED );
        bat    = new GameObj(width/2, height - BRICK_HEIGHT*3/2, BRICK_WIDTH*3, 
            BRICK_HEIGHT/4, Color.GREY);
        bricks = new ArrayList<>();
        // *[1]******************************************************[1]*
        // * Fill in code to add the bricks to the arrayList            *
        // **************************************************************
        int WALL_TOP = 100;                     // how far down the screen the wall starts
        int NUM_BRICKS = width/BRICK_WIDTH;  
        // how many bricks fit on screen
        for (int i=0; i < NUM_BRICKS; i++) {
            GameObj brick = new GameObj(BRICK_WIDTH*i, WALL_TOP, BRICK_WIDTH, BRICK_HEIGHT, Color.BLUE);
            bricks.add(brick);      // add this brick to the list of bricks
        }
        int WALL_TOP1 = 125;                     // how far down the screen the second wall starts
        int NUM_BRICKS1 = width/BRICK_WIDTH;     // how many bricks fit on screen
        for (int i=0; i < NUM_BRICKS; i++) {
            GameObj brick = new GameObj(BRICK_WIDTH*i, WALL_TOP1, BRICK_WIDTH, BRICK_HEIGHT, Color.GREEN);
            bricks.add(brick);      // add this brick to the list of bricks
        }
        
        int WALL_TOP2 = 150;                     // how far down the screen the third wall starts
        int NUM_BRICKS2 = width/BRICK_WIDTH;     // how many bricks fit on screen
        for (int i=0; i < NUM_BRICKS; i++) {
            GameObj brick = new GameObj(BRICK_WIDTH*i, WALL_TOP2, BRICK_WIDTH, BRICK_HEIGHT, Color.YELLOW);
            bricks.add(brick);      // add this brick to the list of bricks
        }
        int WALL_TOP3 = 175;                     // how far down the screen the fourth wall starts
        int NUM_BRICKS3 = width/BRICK_WIDTH;     // how many bricks fit on screen
        for (int i=0; i < NUM_BRICKS; i++) {
            GameObj brick = new GameObj(BRICK_WIDTH*i, WALL_TOP3, BRICK_WIDTH, BRICK_HEIGHT, Color.ORANGE);
            bricks.add(brick);      // add this brick to the list of bricks
        }

        /*int WALL_TOP4 = 570;                     // start at bottom to act as 'lives' before losing points
        int NUM_BRICKS4 = width/BRICK_WIDTH;     // how many bricks fit on screen
        for (int i=0; i < NUM_BRICKS1; i++) {
        GameObj brick = new GameObj(BRICK_WIDTH*i, WALL_TOP4, BRICK_WIDTH, BRICK_HEIGHT, Color.RED);
        bricks.add(brick);      // add this brick to the list of bricks
        }*/
    }

    // Animating the game
    // The game is animated by using a 'thread'. Threads allow the program to do 
    // two (or more) things at the same time. In this case the main program is
    // doing the usual thing (View waits for input, sends it to Controller,
    // Controller sends to Model, Model updates), but a second thread runs in 
    // a loop, updating the position of the ball, checking if it hits anything
    // (and changing direction if it does) and then telling the View the Model 
    // changed.

    // When we use more than one thread, we have to take care that they don't
    // interfere with each other (for example, one thread changing the value of 
    // a variable at the same time the other is reading it). We do this by 
    // SYNCHRONIZING methods. For any object, only one synchronized method can
    // be running at a time - if another thread tries to run the same or another
    // synchronized method on the same object, it will stop and wait for the
    // first one to finish.

    // Start the animation thread
    public void startGame()
    {

        Thread t = new Thread( this::runGame );     // create a thread runnng the runGame method
        t.setDaemon(true);                          // Tell system this thread can die when it finishes
        t.start();                                  // Start the thread running
    }   

    // The main animation loop

    public void runGame()
    {
        try
        {
            // set gameRunning true - game will stop if it is set false (eg from main thread)
            setGameRunning(true);
            while (getGameRunning())
            {
                updateGame();                        // update the game state
                modelChanged();                      // Model changed - refresh screen
                Thread.sleep( getFast() ? 10 : 20 ); // wait a few milliseconds
            }
        } catch (Exception e) 
        { 
            Debug.error("Model::runAsSeparateThread error: " + e.getMessage() );
        }
    }

     // updating the game - this happens about 50 times a second to give the impression of movement
    public synchronized void updateGame()
    {
        // move the ball one step (the ball knows which direction it is moving in)
        ball.moveX(BALL_MOVE);                      
        ball.moveY(BALL_MOVE);
        // get the current ball possition (top left corner)
        int x = ball.topX;  
        int y = ball.topY;

        
        // Deal with possible edge of board hit
        if (x >= width - B - BALL_SIZE)  ball.changeDirectionX();
        if (x <= 0 + B)  ball.changeDirectionX();
        
        int k = bat.topX;  

        // Deal with possible edge of board hit
        if (k >= width - B - BAT_MOVE)  bat.stopY();
        if (k <= 0 + B)  bat.stopY();

        if (y >= height - B - BALL_SIZE)  // Bottom
        { 
            ball.changeDirectionY(); 
            addToScore( HIT_BOTTOM );// score penalty for hitting the bottom of the screen
            takeLife( HIT_BOTTOM2 );               // life penalty for doing the same
            //hasLost();              // all 3 lives lost
        }
        if (y <= 0 + M)  ball.changeDirectionY();

        // check whether ball has hit a (visible) brick
        boolean hit = false;

        // *[3]******************************************************[3]*
        // * Fill in code to check if a visible brick has been hit      *
        // * The ball has no effect on an invisible brick               *
        // * If a brick has been hit, change its 'visible' setting to   *
        // * false so that it will 'disappear'                          * 
        // **************************************************************
        for (GameObj brick: bricks) {
            if (brick.visible && brick.hitBy(ball)) {
                hit = true;
                brick.visible = false;      // set the brick invisible
                addToScore( HIT_BRICK );    // add to score for hitting a brick
                addToHighscore ( HIT_BRICK ); //add to highscore for hitting a brick
                ////Win();
            }
        }    

        if (hit)
            ball.changeDirectionY();

        // check whether ball has hit the bat
        if ( ball.hitBy(bat) )
            ball.changeDirectionY();

        if (score >= 4000)
            fast = true;
         
        if (highscore >= 5900)
           fast = false;
           
        if (highscore >= 6000)
           JOptionPane.showMessageDialog(null, "All Bricks Destroyed. You Win!");
       
        if (lives == 0)
           JOptionPane.showMessageDialog(null, "You Lost All Of Your Lives. Game Over!");


           
    }

    // This is how the Model talks to the View
    // Whenever the Model changes, this method calls the update method in
    // the View. It needs to run in the JavaFX event thread, and Platform.runLater 
    // is a utility that makes sure this happens even if called from the
    // runGame thread
    public synchronized void modelChanged()
    {
        Platform.runLater(view::update);
    }

        
    // update the lives of the player
    public synchronized void takeLife()
    {
        LIVES--;

    }

  

    // Methods for accessing and updating values
    // these are all synchronized so that the can be called by the main thread 
    // or the animation thread safely

    // Change game running state - set to false to stop the game
    public synchronized void setGameRunning(Boolean value)
    {  
        gameRunning = value;
    }

    // Return game running state
    public synchronized Boolean getGameRunning()
    {  
        return gameRunning;
    }

    // Change game speed - false is normal speed, true is fast
    public synchronized void setFast(Boolean value)
    {  
        fast = value;
    }

    //check if the the player lost


    // Return game speed - false is normal speed, true is fast
    public synchronized Boolean getFast()
    {  
        return(fast);
    }

    // Return bat object
    public synchronized GameObj getBat()
    {
        return(bat);
    }

    // return ball object
    public synchronized GameObj getBall()
    {
        return(ball);
    }

    // return bricks
    public synchronized ArrayList<GameObj> getBricks()
    {
        return(bricks);
    }

    // return score
    public synchronized int getScore()
    {
        return(score);
    }

    // update the score
    public synchronized void addToScore(int n)    
    {
        score += n;        
    }

    public synchronized void addToHighscore(int n)    
    {
        highscore += n;        
    } 

    public synchronized int getLives()
    {
        return(lives);
    }

    public synchronized int getHighscore()
    {
        return(highscore);
    } 
    // update the score
    public synchronized void takeLife(int n)    
    {
        lives += n;        
    }
    // move the bat one step - -1 is left, +1 is right
    public synchronized void moveBat( int direction )
    {        
        int dist = direction * BAT_MOVE;    // Actual distance to move
        Debug.trace( "Model::moveBat: Move bat = " + dist );
        bat.moveX(dist);
    }
}   