// Note this is just a class, it has no main.
// A procedure in the main class will call the runSimulator() procedure
package com.company;
import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GOLSimulator extends JFrame {
    int J_LENGTH = 300;
    int I_LENGTH = 500;
    int PIXEL_HEIGHT = 3;
    int PIXEL_WIDTH = 3;
    int WINDOW_WIDTH = I_LENGTH * PIXEL_WIDTH;
    int WINDOW_HEIGHT = J_LENGTH * PIXEL_HEIGHT;
    boolean[][] aliveNow = new boolean[I_LENGTH][J_LENGTH];
    int[][] surroundingNum = new int[I_LENGTH][J_LENGTH];
    int BUFFER = 10; // Buffer is the space added between patterns to restrict the amount of interference
    int i_current = BUFFER, j_max = BUFFER, j_current = BUFFER; //For the random pattern import
    boolean has_more_room = true;

    // Randomly generates screen
    public void populate() {
        Random r = new Random();
        for (int i = 0; i < I_LENGTH; i++) {
            for (int j = 0; j < J_LENGTH; j++) {
                if (r.nextBoolean()) aliveNow[i][j] = true;
                else aliveNow[i][j] = false;
            }
        }
    }
    // Reads a random pattern from file and adds it to the aliveNow array
    // Patterns will be arranged in a grid-like manor, keeping track of the tallest patterns
    // and sorting rows based on the height of the tallest pattern and the buffer to avoid pattern interference.

    // Example of a file: 21p2.cells
    /*
    !Name: 21P2
    !The 38th most common oscillator.
    !www.conwaylife.com/wiki/index.php?title=21P2
    ...O
    .OOO
    O.....O
    O.OOOOO
    .O
    ....O
    ...OO
    */

    public void loadRandomPattern() throws IOException {
        File folder = new File("patterns");
        // reads all file names in the patterns folder
        ArrayList<File> allPatterns = new ArrayList<File>(Arrays.asList(folder.listFiles()));
        Random r = new Random();
        // choose a random pattern
        while(allPatterns.size()>0) {
            int pattern_index = r.nextInt(allPatterns.size());
            interpretCellsFileType(allPatterns.get(pattern_index));
            if (!has_more_room){
                allPatterns.remove(pattern_index);
                has_more_room = true;
            }
        }
        System.out.println("All patterns have been checked. Initialization complete.");
    }
    // Will randomly generate a grid of pre-designed patterns.
    // Files are read from .cells files and need to be interpreted.
    // Variables are used to handle the grid creation and checking for potential index out of bounds errors.
    public void interpretCellsFileType(File file)throws IOException{
        FileReader f = new FileReader(file);
        Scanner in = new Scanner(f);
        ArrayList<String> current_pattern = new ArrayList<String>();
        int highest_x = 0;
        // Read the file into an ArrayList
        while (in.hasNext()){
            String temp = in.nextLine();
            if (!temp.startsWith("!")){
                current_pattern.add(temp);
                if(temp.length()>highest_x)highest_x=temp.length();
            }
        }
        if (i_current +highest_x+BUFFER > I_LENGTH){ //If it will be out of bounds on right side of array, start at zero and adjust j value
            i_current =BUFFER; // reset to the left
            j_current = j_max+BUFFER; //shift down one row, based on the longest pattern's length
            j_max+=current_pattern.size()+BUFFER;
        }
        // Updates the max room we need in the j index
        else if (current_pattern.size()+BUFFER+ j_current >j_max)j_max=current_pattern.size()+BUFFER+ j_current;
        if (j_max+current_pattern.size()>= J_LENGTH){ // If the pattern will be out of bounds at the bottom, check a new pattern
            j_max= j_current;
            has_more_room = false;
            System.out.println("Pattern "+ file.getName()+ " will be out of bounds.");
            return; //cannot add pattern, so stop trying.
        }
        // add the pattern to our initial state
        for (int j = 0; j < current_pattern.size(); j++) {
            String temp =current_pattern.get(j);
            for (int i = 0; i < temp.length(); i++) {
                //System.out.print(temp.substring(i,i+1));
                if (temp.substring(i,i+1).equals("."))aliveNow[i+ i_current][j+ j_current]=false;
                else aliveNow[i+ i_current][j+ j_current]=true;
            }
            //System.out.println();
        }
        i_current += highest_x+BUFFER;
        System.out.println("Displaying pattern from file: "+file.getName());
    }
    // Reads a file of coordinates for initial state
    public void loadPattern(String file)throws IOException {
        FileReader f = new FileReader(file);
        Scanner fin = new Scanner(f);
        while(fin.hasNext()){
            aliveNow[fin.nextInt()][fin.nextInt()] = true;
        }
    }
    // Counts the number of living neighbours and returns it as an integer
    public int countLivingNeighbours(int i, int j){
        int surroundings = 0;
        for (int k = i-1; k <=i+1; k++) {
            for (int l = j-1; l <= j+1 ; l++) {
                if (0<=k && k< I_LENGTH && 0<=l && l< J_LENGTH && !(k==i && l==j) && aliveNow[k][l])
                    surroundings++;
            }
        }
        return surroundings;
    }
    // Fills a second array that contains the number of living surrounding cells
    public void fillSecondArray() {
        //Checks for edge cases. If on the edge of the screen, don't check out of index
        for (int i = 0; i < I_LENGTH; i++) {
            for (int j = 0; j < J_LENGTH; j++) {
                surroundingNum[i][j] = countLivingNeighbours(i, j);
            }
        }
    }
    // Based on a cell's number of living neighbours, we update the first array
    // by following the rules for Conway's Game of Life.
    public void updateFirstArray(){
        for (int i = 0; i < I_LENGTH; i++) {
            for (int j = 0; j < J_LENGTH; j++) {
                if(aliveNow[i][j]){
                    if(surroundingNum[i][j]<2) aliveNow[i][j]=false;
                    else if(surroundingNum[i][j]>3) aliveNow[i][j]=false;
                }
                else{
                    if(surroundingNum[i][j]==3) aliveNow[i][j]=true;
                }
            }
        }
    }
    public void sleep(){
        try {
            Thread.sleep(50);
        }
        catch (Exception e) {
        }
    }
    public void paint(Graphics g) {
        Image img = renderImage();
        g.drawImage(img, 3, 27, this);
    }
    private Image renderImage(){
        BufferedImage bufferedImage = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT,BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        int xpos = 0;
        int ypos = PIXEL_HEIGHT;
        for (int i = 0; i < I_LENGTH; i++) {
            for (int j = 0; j < J_LENGTH; j++) {
                if (aliveNow[i][j]){
                    g.setColor(Color.WHITE);
                }
                else{
                    g.setColor(Color.BLACK);
                }
                g.fillRect(xpos, ypos, PIXEL_WIDTH, PIXEL_HEIGHT);
                g.setColor(Color.BLACK);
                g.drawRect(xpos,ypos, PIXEL_WIDTH, PIXEL_HEIGHT);
                ypos += PIXEL_HEIGHT;
            }
            xpos += PIXEL_WIDTH;
            ypos = PIXEL_HEIGHT;
        }
        return bufferedImage;
    }
    public void runSimulator() throws IOException{
        //this.loadPattern("GliderGun.txt");
        //this.populate();
        this.loadRandomPattern();
        this.setTitle("Conway's Game of Life");
        this.setSize(this.WINDOW_WIDTH,this.WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setBackground(Color.white);
        this.setVisible(true);
        while(true){
            this.fillSecondArray();
            this.updateFirstArray();
            this.repaint();
            this.sleep();
        }
    }
}
