
import java.util.ArrayList;
import javafx.event.EventHandler;
import javafx.scene.input.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

// The View class creates and manages the GUI for the application.
// It doesn't know anything about the game itself, it just displays
// the current state of the Model, and handles user input
public class View implements EventHandler<KeyEvent>
{ 
    // variables for components of the user interface
    public int width;       // width of window
    public int height;      // height of window

    // usr interface objects
    public Pane pane;       // basic layout pane
    public Canvas canvas;   // canvas to draw game on
    public Label infoText;  // info at top of screen
    public Label infoText2;
    public Label infoText3;
    public Label infoText4;
    public Label infoText5;

    // The other parts of the model-view-controller setup
    public Controller controller;
    public Model model;

    public GameObj   bat;            // The bat
    public GameObj   ball;           // The ball
    public ArrayList<GameObj> bricks;     // The bricks
    public int       score =  0;     // The score
    public int       highscore = 0;  // The players highest reached score in that game
    public int       lives =  5;     // The players remaining lives

    // we don't really need a constructor method, but include one to print a 
    // debugging message if required
    public View(int w, int h)
    {
        Debug.trace("View::<constructor>");
        width = w;
        height = h;
    }

    // start is called from Main, to start the GUI up
    // Note that it is important not to create controls etc here and
    // not in the constructor (or as initialisations to instance variables),
    // because we need things to be initialised in the right order
    public void start(Stage window) 
    {
        // breakout is basically one big drawing canvas, and all the objects are
        // drawn on it as rectangles, except for the text at the top - this
        // is a label which sits 'on top of' the canvas.

        pane = new Pane();       // a simple layout pane
        pane.setId("Breakout");  // Id to use in CSS file to style the pane if needed

        // canvas object - we set the width and height here (from the constructor), 
        // and the pane and window set themselves up to be big enough
        canvas = new Canvas(width,height);  
        pane.getChildren().add(canvas);     // add the canvas to the pane

        // infoText box for the score - a label which we position on 
        //the canvas with translations in X and Y coordinates
        infoText = new Label("Your Score = " + score); //shows the players current score
        infoText.setTranslateX(100);
        infoText.setTranslateY(10);
        pane.getChildren().add(infoText);  // add label to the pane

        infoText2 = new Label("Highest Score = " + highscore); //adds a second score to show the highest achieved
        infoText2.setTranslateX(100);
        infoText2.setTranslateY(30); // places the text beneath infotext
        pane.getChildren().add(infoText2);  // add label to the pane

        infoText3 = new Label("Lives Left = " + lives); //shows the players remaining lives
        infoText3.setTranslateX(100);
        infoText3.setTranslateY(50); // places the text beneath infotext2
        pane.getChildren().add(infoText3);  // add label to the pane


        // add the complete GUI to the scene
        Scene scene = new Scene(pane);   
        scene.getStylesheets().add("breakout.css"); // tell the app to use our css file

        // Add an event handler for key presses. We use the View object itself
        // and provide a handle method to be called when a key is pressed.
        scene.setOnKeyPressed(this);

        // put the scene in the window and display it
        window.setScene(scene);
        window.show();
    }

    // Event handler for key presses - it just passes th event to the controller
    public void handle(KeyEvent event)
    {
        // send the event to the controller
        controller.userKeyInteraction( event );
    }

    // drawing the game
    public void drawPicture()
    {
        // the ball movement is runnng 'i the background' so we have
        // add the following line to make sure
        synchronized( Model.class )   // Make thread safe (because the bal
        {
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // clear the canvas to redraw
            gc.setFill( Color.BLACK );
            gc.fillRect( 0, 0, width, height );

            // update score
            infoText.setText("Your Score = " + score); //updates the score total
            infoText2.setText("Score (without fail) = " + highscore); //matches the highest score
            infoText3.setText("Lives Left = " + lives); //updates player lives
            // draw the bat and ball
            displayGameObj( gc, ball );   // Display the Ball
            displayGameObj( gc, bat  );   // Display the Bat

            // *[2]****************************************************[2]*
            // * Display the bricks that make up the game                 *
            // * Fill in code to display bricks from the ArrayList        *
            // * Remember only a visible brick is to be displayed         *
            // ************************************************************
            for (GameObj brick: bricks) {
                if (brick.visible) {
                    displayGameObj(gc, brick);
                }
            }     
    }
}

    // Display a game object - it is just a rectangle on the canvas
    public void displayGameObj( GraphicsContext gc, GameObj go )
    {
        gc.setFill( go.colour );
        gc.fillRect( go.topX, go.topY, go.width, go.height );
    }

    // This is how the Model talks to the View
    // This method gets called BY THE MODEL, whenever the model changes
    // It has to do whatever is required to update the GUI to show the new model status
    public void update()
    {
        // Get from the model the ball, bat, bricks & score
        ball    = model.getBall();              // Ball
        bricks  = model.getBricks();            // Bricks      
        bat     = model.getBat();               // Bat
        score   = model.getScore();            // Score
        highscore = model.getHighscore();      //Highscore
        lives   = model.getLives();            // Lives
        
        //Debug.trace("Update");
        drawPicture();                     // Re draw game
    }


}