import edu.macalester.graphics.CanvasWindow;
import edu.macalester.graphics.Point;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The main class of the game of SeaBattle that handles the logic and running of the game.
 */
public class SeaBattleGame {
    private static final int cellSize = 36;
    private static final int numRows = 21;
    private static final int numCols = 10;

    private Grid grid;
    private CanvasWindow canvas;
    private GameGUI screens;
    private String[][] maze;
    private Map<String, Map<String, ArrayList<Point>>> shipCoordinates;
    private Map<Point, Boolean> shotCoordinates;


    /**
     * Initializes a game of SeaBattle, displaying its main screen.
     */
    public SeaBattleGame() {
        canvas = new CanvasWindow("Sea Battle", GameGUI.CANVAS_WIDTH, GameGUI.CANVAS_HEIGHT);
        canvas.setBackground(Color.GRAY);
        screens = new GameGUI(canvas, this);
        screens.homeScreen();
        shotCoordinates = new HashMap<>();
        shipCoordinates = new HashMap<>();
    }


    /***
     * Computer shooting turn, which generate a random coordinates to shoot on the player board.
     */
    private void computerTurn() {
        int compRow = (int) (Math.random() * 10);
        int compCol = (int) (Math.random() * 10);
        Point coordinates = new Point(compCol, compRow);
        while (shotCoordinates.get(coordinates)) {
            compRow = (int) (Math.random() * 10);
            compCol = (int) (Math.random() * 10);
            coordinates = new Point(compCol, compRow);
        }
        if (maze[compCol][compRow].equals("S") || maze[compCol][compRow].equals("RightShooted")) {
            canvas.add(grid.setCellGraphics(compCol, compRow, "R"));
            maze[compCol][compRow] = "RightShooted";
        } else {
            canvas.add(grid.setCellGraphics(compCol, compRow, "W"));
            maze[compCol][compRow] = "WrongShooted";
        }
        shotCoordinates.put(new Point(compCol, compRow), true);
        System.out.println(Arrays.deepToString(maze).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
    }

    /***
     * The player's shooting turn takes in user input to hit the computer's ships.
     */
    private void playerTurn() {
        int col = Integer.parseInt(screens.coordinateField1.getText());
        int row = Integer.parseInt(screens.coordinateField2.getText());
        shotCoordinates.put(new Point(col, row + 11), true);
        if (maze[col][row].equals("S") || maze[col][row].equals("RightShooted")) {
            canvas.add(grid.setCellGraphics(col, row + 11, "R"));
            maze[col][row] = "RightShooted";
        } else {
            canvas.add(grid.setCellGraphics(col, row + 11, "W"));
            maze[col][row] = "WrongShooted";
        }


    }

    /***
     * A single turn of the game, including both the player and computer movements and checking if the game is finished or not. 
     */
    public void shootMissile() {
        playerTurn();
        // need a pause
        if (youWin()) {
            screens.winMessage();
        }
        computerTurn();
        // need a pause
        if (youLose()) {
            screens.loseMessage();
        }
    }

    /***
     * Check if the player has won by checking if all of the opponent's ships have been hit.
     */
    public boolean youWin() {
        for (int i = 1; i < shipCoordinates.get("Opponent Ships").size() + 1; i++) {
            for (Point point : shipCoordinates.get("Opponent Ships").get("Ship Length:" + i)) {
                if (!shotCoordinates.get(point)) {
                    return false;
                } else {
                    continue;
                }
            }
        }
        return true;
    }

    /***
     * Check if the player has lost by checking if all of their ships have been hit.
     */
    public boolean youLose() {
        for (int i = 1; i < shipCoordinates.get("Player Ships").size() + 1; i++) {
            for (Point point : shipCoordinates.get("Player Ships").get("Ship Length:" + i)) {
                if (!shotCoordinates.get(point)) {
                    return false;
                } else {
                    continue;
                }
            }
        }
        return true;
    }

    /***
     * Create the grid and adds it to the Canvas.
     */
    public void generateGrid() {
        populateGrid();
        canvas.draw();
        grid = new Grid(numCols, numRows, cellSize, maze, this);
        canvas.add(grid);
    }

    /***
     * Randomly generates the location of the ships in the grid.
     */
    private void populateGrid() {
        int size = 10;
        Random random = new Random();
        int[][] playerBoard = new int[size][size];
        int[][] opponentBoard = new int[size][size];
        ArrayList<int[][]> gameBoards = new ArrayList<int[][]>(2);
        gameBoards.add(playerBoard);
        gameBoards.add(opponentBoard);

        for (int[][] board : gameBoards) {
            Map<String, ArrayList<Point>> boardShips = new HashMap<>();
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
                ArrayList<Point> singleShipCoordinates = new ArrayList<>(i - 1);
                for (int j = 0; j < i; j++) {
                    board[y][x] = i;
                    if (board.equals(playerBoard)) {
                        singleShipCoordinates.add(new Point(y, x + 11));
                    } else {
                        singleShipCoordinates.add(new Point(y, x));
                    }
                    if (vertical) {
                        y++;
                    } else {
                        x++;
                    }
                }
                boardShips.put("Ship Length:" + i, singleShipCoordinates);
            }
            if (board.equals(playerBoard)) {
                shipCoordinates.put("Opponent Ships", boardShips);
            } else {
                shipCoordinates.put("Player Ships", boardShips);
            }
        }

        maze = new String[numCols][numRows];
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < numRows; j++) {
                if (j < 10) {
                    if (opponentBoard[i][j] == 0 || opponentBoard[i][j] == 9) {
                        maze[i][j] = "R";
                    } else {
                        maze[i][j] = "S";
                    }
                } else if (j == 10) {
                    maze[i][j] = "W";
                } else {
                    if (playerBoard[i][j - 11] == 0 || playerBoard[i][j - 11] == 9) {
                        maze[i][j] = "R";
                    } else {
                        maze[i][j] = "S";
                    }
                }
                shotCoordinates.put(new Point(i, j), false);
            }
        }
        System.out.println(shipCoordinates.toString());
    }

    /**
     * Click here to run the game.
     */
    public static void main(String[] args) {
        new SeaBattleGame();
    }
}
