
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Maze extends JFrame {

    private int[][] values;
    private boolean[][] visited;
    private int startRow;
    private int startColumn;
    private ArrayList<JButton> buttonList;
    private int rows;
    private int columns;
    private boolean backtracking;
    private int algorithm;

    public Maze(int algorithm, int size, int startRow, int startColumn) {
        this.algorithm = algorithm;
        Random random = new Random();
        this.values = new int[size][];
        for (int i = 0; i < values.length; i++) {
            int[] row = new int[size];
            for (int j = 0; j < row.length; j++) {
                if (i > 1 || j > 1) {
                    row[j] = random.nextInt(8) % 7 == 0 ? Definitions.OBSTACLE : Definitions.EMPTY;
                } else {
                    row[j] = Definitions.EMPTY;
                }
            }
            values[i] = row;
        }
        values[0][0] = Definitions.EMPTY;
        values[size - 1][size - 1] = Definitions.EMPTY;
        this.visited = new boolean[this.values.length][this.values.length];
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.buttonList = new ArrayList<>();
        this.rows = values.length;
        this.columns = values.length;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        this.setLocationRelativeTo(null);
        GridLayout gridLayout = new GridLayout(rows, columns);
        this.setLayout(gridLayout);
        for (int i = 0; i < rows * columns; i++) {
            int value = values[i / rows][i % columns];
            JButton jButton = new JButton(String.valueOf(i));
            if (value == Definitions.OBSTACLE) {
                jButton.setBackground(Color.BLACK);
            } else {
                jButton.setBackground(Color.WHITE);
            }
            this.buttonList.add(jButton);
            this.add(jButton);
        }
        this.setVisible(true);
        this.setSize(Definitions.WINDOW_WIDTH, Definitions.WINDOW_HEIGHT);
        this.setResizable(false);
    }

    public void checkWayOut() {
        new Thread(() -> {
            boolean result = false;
            switch (this.algorithm) {
                case Definitions.ALGORITHM_DFS:
                    result = runDFS();
                    break;
                case Definitions.ALGORITHM_BRUTE_FORCE:
                case Definitions.ALGORITHM_BFS:
                default:
                    System.out.println("No defined algorithm found.");
                    System.exit(0);
            }
            JOptionPane.showMessageDialog(null,  result ? "FOUND SOLUTION" : "NO SOLUTION FOR THIS MAZE");

        }).start();
    }


    public void setSquareAsVisited(int x, int y, boolean visited) {
        try {
            if (visited) {
                if (this.backtracking) {
                    Thread.sleep(Definitions.PAUSE_BEFORE_NEXT_SQUARE * 5);
                    this.backtracking = false;
                }
                this.visited[x][y] = true;
                for (int i = 0; i < this.visited.length; i++) {
                    for (int j = 0; j < this.visited[i].length; j++) {
                        if (this.visited[i][j]) {
                            if (i == x && y == j) {
                                this.buttonList.get(i * this.rows + j).setBackground(Color.RED);
                            } else {
                                this.buttonList.get(i * this.rows + j).setBackground(Color.BLUE);
                            }
                        }
                    }
                }
            } else {
                this.visited[x][y] = false;
                this.buttonList.get(x * this.columns + y).setBackground(Color.WHITE);
                Thread.sleep(Definitions.PAUSE_BEFORE_BACKTRACK);
                this.backtracking = true;
            }
            if (!visited) {
                Thread.sleep(Definitions.PAUSE_BEFORE_NEXT_SQUARE / 4);
            } else {
                Thread.sleep(Definitions.PAUSE_BEFORE_NEXT_SQUARE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean runDFS(){
        boolean hasExit = false;
        Stack<JButton> nodes = new Stack<>();
        Map<JButton, int[]> nodesCoordinates = getNodesCoordinates();
        nodes.add(buttonList.get(0));
        while (!nodes.isEmpty()) {
            JButton currentNode = nodes.pop();
            int[] xy = nodesCoordinates.get(currentNode);
            if(!visited[xy[1]][xy[0]]) {
                setSquareAsVisited(xy[1], xy[0], true);
                for (JButton neighbor : getNeighbors(xy)) {
                    xy = nodesCoordinates.get(neighbor);
                    if (neighbor != null) {
                        if (values[xy[1]][xy[0]] != 1) {
                            if (!visited[xy[1]][xy[0]]) {
                                nodes.add(neighbor);
                            }
                        }
                    }
                }
            }
            if (visited[visited.length-1][visited.length-1]) {
                hasExit = true;
                break;
            }
        }
        return hasExit;
    }


    public JButton[] getNeighbors(int[] xy){
        JButton[] neighbors = new JButton[4];
        JButton[][] nodeMatrix = getNodeMatrix();
        int x = xy[0], y = xy[1];
        int matrixLen = visited.length;


        if (x == 0) {
           if (y == 0) {
               neighbors[0] = buttonList.get(1);
               neighbors[1] = buttonList.get(matrixLen);
           }
           else if (y == matrixLen - 1) {
               neighbors[0] = buttonList.get(matrixLen * matrixLen - 2 * matrixLen);
               neighbors[1] = buttonList.get(matrixLen * matrixLen - matrixLen + 1);
           }
           else {
               neighbors[0] = nodeMatrix[y+1][0];
               neighbors[1] = nodeMatrix[y-1][0];
               neighbors[2] = nodeMatrix[y][x+1];

           }
        } else if (x == matrixLen - 1) {
            if (y == 0) {
                neighbors[0] = nodeMatrix[0][x-1];
                neighbors[1] = nodeMatrix[1][x];
            } else if (y == matrixLen -1) {
                neighbors[0] = nodeMatrix[y][x-1];
                neighbors[1] = nodeMatrix[y-1][y];
            }
            else {
                neighbors[0] = nodeMatrix[y-1][x];
                neighbors[1] = nodeMatrix[y+1][x];
                neighbors[2] = nodeMatrix[y][x-1];
            }
        }
        else {
            if (y==0) {
                neighbors[0] = nodeMatrix[y][x+1];
                neighbors[1] = nodeMatrix[y][x-1];
                neighbors[2] = nodeMatrix[y+1][x];
            }
            else if (y == matrixLen - 1) {
                neighbors[0] = nodeMatrix[y][x+1];
                neighbors[1] = nodeMatrix[y][x-1];
                neighbors[2] = nodeMatrix[y-1][x];
            }
            else {
                neighbors[0] = nodeMatrix[y+1][x];
                neighbors[1] = nodeMatrix[y-1][x];
                neighbors[2] = nodeMatrix[y][x+1];
                neighbors[3] = nodeMatrix[y][x-1];
            }
         }

        return neighbors;
    }

    public Map<JButton, int[]> getNodesCoordinates() {
      Map<JButton, int[]> coordinates = new HashMap<>();
        int x=0,  y=0;
        for (JButton currentNode : buttonList) {
            int[] xy = {x, y};
            coordinates.put(currentNode, xy);
            x++;
            if (x == visited.length) {
                y++;
                x = 0;
            }
        }

      return coordinates;
    }


    public JButton[][] getNodeMatrix() {

        int matrixLength = visited.length;
        JButton[][] nodeMatrix = new JButton[matrixLength][matrixLength];
        int x=0, y=0;
        for (JButton currentBtn : buttonList) {
            nodeMatrix[y][x] = currentBtn;
            x++;
            if (x == matrixLength) {
                y++;
                x=0;
            }

        }

        return nodeMatrix;
    }

}
