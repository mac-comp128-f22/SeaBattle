import edu.macalester.graphics.CanvasWindow;
import edu.macalester.graphics.Point;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SeaBattleGame {
    private static final int cellSize = 36;
    private static final int numRows = 21;
    private static final int numCols = 10;

    private Grid grid;
    private CanvasWindow canvas;
    private GameGUI screens;

    private String[][] maze;
    private int[][] playerBoard;
    private int[][] opponentBoard;
    
    
    private Map<String,  Map<String, Point>> shipCoordinates;
    private Map<Point, Boolean> shotCoordinates;


    public SeaBattleGame() {
        canvas = new CanvasWindow("Sea Battle", GameGUI.CANVAS_WIDTH, GameGUI.CANVAS_HEIGHT);
        canvas.setBackground(Color.GRAY);
        screens = new GameGUI(canvas, this);
        screens.homeScreen();
        shotCoordinates = new HashMap<>();
        shipCoordinates = new HashMap<>();
    }

    private void computerTurn() {
        int compRow = 11 + (int) (Math.random() * 10);
        int compCol = (int) (Math.random() * 10);
        Point coordinates = new Point(compCol, compRow);
        while (shotCoordinates.get(coordinates)) {
            compRow = 11 + (int) (Math.random() * 10);
            compCol = (int) (Math.random() * 10);
            coordinates = new Point(compCol, compRow);
        }
        canvas.add(grid.setCellGraphics(compCol, compRow));
        shotCoordinates.put(new Point(compCol, compRow), true);
    }

    private void playerTurn() {
        int row = Integer.parseInt(screens.coordinateField1.getText());
        int col = Integer.parseInt(screens.coordinateField2.getText());

        canvas.add(grid.setCellGraphics(row, col));
        shotCoordinates.put(new Point(row, col), true);
    }

    public void shootMissile() {
        // while ships not left do this
        playerTurn();
        computerTurn();
        // else end game  
        gameResult();    
    }

    private void gameResult() {
        if (checkWin() == 1 || checkWin() == -1) {
            if (checkWin() == 1) {
                System.out.println("Player Win!");
            } else {
                System.out.println("Computer Win!");
            }
            canvas.closeWindow();
        }
    }

    private int checkWin() {
        // TODO: update this method to use shotCoordinates
        boolean playerHasShip = false;
        boolean compHasShip = false;
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < 10; j++) {
                if (maze[i][j] == "S") {
                    playerHasShip = true;
                }
            }
        }

        for (int i = 0; i < numCols; i++) {
            for (int j = 11; j < 21; j++) {
                if (maze[i][j] == "S") {
                    compHasShip = true;
                }
            }
        }
        int res = 0;
        if (!playerHasShip) {
            res = 1;
        } else if (!compHasShip) {
            res = -1;
        }
        return res;
    }



    public void generateGrid() {
        populateGrid();
        canvas.draw();
        grid = new Grid(numCols, numRows, cellSize, maze, this);
        canvas.add(grid);
    }

    private void populateGrid() {
        int size = 10;
        Random random = new Random();
        playerBoard = new int[size][size];
        opponentBoard = new int[size][size];
        ArrayList<int[][]> gameBoards = new ArrayList<int[][]>(2);
        gameBoards.add(playerBoard);
        gameBoards.add(opponentBoard);

        for (int[][] board : gameBoards) {
            for (int i = 5; i > 0; i--) {
                // start point of the ship and direction
                int x = random.nextInt(board.length);
                int y = random.nextInt(board.length);
                boolean vertical = random.nextBoolean();

                // correct start point so that the ship could fit in the field
                if (vertical) {
                    if (y + i > size) {
                        y -= i;
                    }
                } else if (x + i > size) {
                    x -= i;
                }
                boolean isFree = true;
                // check for free space
                if (vertical) {
                    for (int m = y; m < y + i; m++) {
                        if (board[m][x] != 0) {
                            isFree = false;
                            break;
                        }
                    }
                } else {
                    for (int n = x; n < x + i; n++) {
                        if (board[y][n] != 0) {
                            isFree = false;
                            break;
                        }
                    }
                }
                if (!isFree) {  // no free space found, retry
                    i++;
                    continue;
                }

                // fill in the adjacent cells
                if (vertical) {
                    for (int m = Math.max(0, x - 1); m < Math.min(size, x + 2); m++) {
                        for (int n = Math.max(0, y - 1); n < Math.min(size, y + i + 1); n++) {
                            board[n][m] = 9;
                        }
                    }
                } else {
                    for (int m = Math.max(0, y - 1); m < Math.min(size, y + 2); m++) {
                        for (int n = Math.max(0, x - 1); n < Math.min(size, x + i + 1); n++) {
                            board[m][n] = 9;
                        }
                    }
                }
                // fill in the ship cells
                for (int j = 0; j < i; j++) {
                    board[y][x] = i;
                    if (vertical) {
                        y++;
                    } else {
                        x++;
                    }
                }
            }
        }
        maze = new String[numCols][numRows];
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < numRows; j++) {
                if (j < 10) {
                    if (playerBoard[i][j] == 0 || playerBoard[i][j] == 9) {
                        maze[i][j] = "R";
                    } else {
                        maze[i][j] = "S";
                    }
                } else if (j == 10) {
                    maze[i][j] = "W";
                } else {
                    if (opponentBoard[i][j - 11] == 0 || opponentBoard[i][j - 11] == 9) {
                        maze[i][j] = "R";
                    } else {
                        maze[i][j] = "S";
                    }
                }
                shotCoordinates.put(new Point(i, j), false);
            }
        }
    }

    public static void main(String[] args) {
        new SeaBattleGame();
    }
}
