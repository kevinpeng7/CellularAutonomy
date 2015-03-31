package com.company;
import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.io.*;

public class FireSimulator extends JFrame {
    int window_width, window_height, pixel_height, pixel_width, y_array_length, x_array_length, forest_counter = 0,
            forest_max, generation = 0, tree_strength = 50, WIN_WIDTH_QUARTER, WIN_WIDTH_HALF,BANNER_HEIGHT;
    int[][] treeValue, treeSurroundingSum, forest;
    boolean showNumbers;
    Font font;
    Random rand = new Random();
    Scanner in = new Scanner(System.in);
    Color c;

    // Takes the user input and creates the arrays, and initial constants
    public void getMode() throws IOException{
        String temp;
        System.out.println("Type \"g\" for game of life. Type \"d\" for Fire Simulator with default values. " +
                           "Type \"s\" to set your own values for Fire Simulator.");
        temp = in.next();
        // Set default values
        if (temp.equals("d")){
            this.setDefaults();
            this.setUp();
        }
        else if (temp.equals("g")){
            runGOL();
        }
        else {
            this.setWindowSize();
            this.setUp();
        }
    }
    public void setDefaults(){
        pixel_height = 5;
        pixel_width = 5;
        window_height = 720;
        window_width = 1280;
    }
    public void runGOL() throws IOException{
        GOLSimulator w = new GOLSimulator();
        w.runSimulator();
    }
    public void sleep(){
        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
    }
    public void setWindowSize(){
        // Take user input
        System.out.println("Input the height of a pixel.");
        pixel_height = in.nextInt();
        System.out.println("Input the width of a pixel.");
        pixel_width = in.nextInt();
        System.out.println("Input the height of the window.");
        window_height = in.nextInt();
        System.out.println("Input the width of the window.");
        window_width = in.nextInt();
    }
    // This procedure initializes the variable values and populates the forest array
    public void setUp(){
        y_array_length = window_height/ pixel_height;
        x_array_length = window_width/ pixel_width;
        treeValue = new int[x_array_length][y_array_length];
        treeSurroundingSum = new int[x_array_length][y_array_length];
        forest = new int[x_array_length][y_array_length];
        for (int i = 0; i < x_array_length; i++) {
            for (int j = 0; j < y_array_length; j++) {
                forest[i][j]=tree_strength + rand.nextInt(10)-5;
            }
        }
        forest_max = x_array_length * y_array_length *tree_strength;
        if (forest_max<=0) forest_max =1; // To avoid Arithmetic Zero Division Errors.
        // Following are constant adjustment factors to allow the info banner to scale with window size.
        // Computed here once, to avoid it being computed for every iteration.
        WIN_WIDTH_HALF = window_width/2;
        WIN_WIDTH_QUARTER = window_width/4;
        BANNER_HEIGHT = window_width/60 + 6;

        font = new Font("Serif", Font.PLAIN, BANNER_HEIGHT);
    }

    public void setNumbersVisible(){
        showNumbers = true;
    }
    public Color getColor(int i,int j, int red, int green, int blue){
        if (forest[i][j]>0){
            red = this.checkRGB(red*forest[i][j]/tree_strength);
            green = this.checkRGB(green * forest[i][j] / tree_strength);
            blue = this.checkRGB(blue*forest[i][j]/tree_strength);
            c = new Color(red,green,blue);
        }
        else c = Color.BLACK;
        return c;
    }
    // Ensures that colour values are in the range 0 to 255
    public int checkRGB(int colour){
        if (colour <=0)return 0;
        else if(colour>=255)return 255;
        else return colour;
    }

    public void paint(Graphics g){
        Image img = renderImage();
        g.drawImage(img, 0, 20, this);
    }
    private Image renderImage(){
        BufferedImage bufferedImage = new BufferedImage(window_width,window_height,BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.setFont(new Font("Serif", Font.PLAIN, pixel_height-2));
        int xpos = 0;
        int ypos = 0;
        // Draws the grid of pixels, colour based on tree Value
        for (int i = 0; i < x_array_length; i++) {
            for (int j = 0; j < y_array_length; j++) {
                if (treeValue[i][j]==0){
                    g.setColor(getColor(i,j,75,150,60));
                }
                else{
                    if(treeValue[i][j]==1)g.setColor(Color.red);
                    else if(treeValue[i][j]==2)g.setColor(Color.orange);
                    else if(treeValue[i][j]==3)g.setColor(Color.yellow);
                    else if(treeValue[i][j]==4)g.setColor(Color.WHITE);
                    else g.setColor(new Color(129,187,216)); // rain
                }
                g.fillRect(xpos, ypos, pixel_width, pixel_height);
                g.setColor(Color.BLACK);
                if(showNumbers)g.drawString(Integer.toString(treeSurroundingSum[i][j]),xpos,ypos+pixel_height);
                //g.setColor(Color.BLACK);
                //g.drawRect(xpos,ypos, pixel_width, pixel_height);
                ypos += pixel_height;
            }
            xpos += pixel_width;
            ypos = pixel_height;
        }
        // Creates the info banner at the top of the screen. Scales with width changes.
        g.setColor(Color.lightGray);
        g.fillRect(WIN_WIDTH_QUARTER, 0, WIN_WIDTH_HALF, BANNER_HEIGHT + 3);
        g.setFont(font);
        g.setColor(Color.BLACK);
        g.drawString("Percent of Forest Burned: " + (100*forest_counter)/forest_max +"% \t Minutes Passed: "+ generation,window_width/4,BANNER_HEIGHT);
        return bufferedImage;
    }
    public int getSurroundingSum(int i,int j){
        int surroundings = 0;
        for (int k = i-1; k <=i+1; k++) {
            for (int l = j-1; l <= j+1 ; l++) {
                if (0<=k && k< x_array_length && 0<=l && l< y_array_length && !(k==i && l==j))
                    surroundings += treeValue[k][l];
            }
        }
        return surroundings;
    }
    public void fillSumArray(){
        generation++;
        //Checks for edge cases. If on the edge of the screen, don't check out of index
        for (int i = 0; i < x_array_length; i++) {
            for (int j = 0; j < y_array_length; j++){
                if(forest[i][j]>0){
                    treeSurroundingSum[i][j]=getSurroundingSum(i,j);
                }
                else treeSurroundingSum[i][j] = 0;

            }
        }
    }
    public void update(){
        /* Trees have 5 possible integer values from 0 to 4
        * Any tree 0 is not burning and any tree with an integer value between 1 and 4 is burning.
        * Any non burning tree can have two states: dead and alive. A non burning tree is dead if it does not have any wood left to burn, represented by the integer array called "forest"
        * RULES: Take the sum of the 8 surrounding tree values.
        * 1. If the tree is burning, the tree will stop burning if the sum is less than 3 (Not enough heat to ignite surrounding area) or greater than 6 (lack of oxygen)
        * 2. If the tree is burning, assign it the value of the sum - 2, as if to simulate flame intensity
        * 3. If the tree is burning, a trees value cannot decrease unless assigned to 1 or 0, a tree value of 4 can only be reassigned to 0
        * 4. If the tree is not burning, a sum of exactly 4 will cause ignition and assign a value of 1 to the tree, sum of 5 will assign value of 2
        * 5. A tree can burn for a set number of iterations. Currently each tree can burn randomly from 40-60 iterations.
        */
        for (int i = 0; i < x_array_length; i++) {
            for (int j = 0; j < y_array_length; j++) {
                if (treeValue[i][j]<=0) {
                    if (treeSurroundingSum[i][j]==4) treeValue[i][j]=1;
                    else if(treeSurroundingSum[i][j]==5) treeValue[i][j]=2;
                    else treeValue[i][j] = 0;
                } else{
                    if (treeSurroundingSum[i][j] <3 || treeSurroundingSum[i][j]>6) treeValue[i][j]=0;
                    else {
                        int new_value = treeSurroundingSum[i][j]-2;
                        if (new_value == 1 && treeValue[i][j]!=4) treeValue[i][j]=new_value;
                        else if (new_value>=treeValue[i][j]) treeValue[i][j] = new_value;
                        else treeValue[i][j] = 0;
                    }
                    forest[i][j]-=1;
                    forest_counter += 1;
                }
            }
        }
    }

    // Randomly generates screen
    public void populate() {
        for (int i = 0; i < x_array_length; i++) {
            for (int j = 0; j < y_array_length; j++) {
                int piece = rand.nextInt(5);
                treeValue[i][j]= piece;
            }
        }
    }
    // Randomly generates sparks
    public void setPatternStorm(){
        int probability = x_array_length*y_array_length;
        int rain_chance = probability/200;
        for (int i = 0; i < x_array_length; i++) {
            for (int j = 0; j < y_array_length; j++) {
                int temp = rand.nextInt(probability); // 1 cell per iteration on average
                if (temp == 0){
                    if (forest[i][j]>0)treeValue[i][j]=4;
                }
                else if (temp<rain_chance){ // 0.5% of cells will be filled with rain
                    treeValue[i][j] = -1;
                }
            }
        }
    }
    public void setPatternExplosion(){
        treeValue[x_array_length /2][y_array_length /2]=4;
        treeValue[x_array_length /2][y_array_length /2+2]=4;
    }
    // Reads a file for initial state
    public void loadPattern(String file)throws IOException{
        FileReader f = new FileReader(file);
        Scanner fin = new Scanner(f);
        while(fin.hasNext()){
            treeValue[fin.nextInt()][fin.nextInt()] = fin.nextInt(); // x-coordinate y-coordinate value
        }
    }
    public static void main(String[] args) throws IOException{
        FireSimulator w = new FireSimulator();
        w.getMode();
        //w.setNumbersVisible(); // Will show surrounding sum on screen
        //w.setPatternExplosion();
        w.setSize(w.window_width, w.window_height);
        w.setTitle("Kevin's Forest Fire Simulator");
        w.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        w.setVisible(true);
        while (true){
            w.setPatternStorm();
            w.fillSumArray();
            w.sleep();
            w.update();
            w.repaint();
        }
    }
}