package src.LLDConcepts;

import java.util.*;

class Game {
    private int cellCount;
    private int users;
    private Map<Integer, Integer> snakes;
    private Map<Integer, Integer> ladders;
    private int[] position;

    public Game(int cellCount, int users, Map<Integer, Integer> snakes, 
                     Map<Integer, Integer> ladders) {
        this.cellCount = cellCount;
        this.users = users;
        this.snakes = snakes;
        this.ladders = ladders;
        this.position = new int[users];
    }

    public void start() {
        int st = 0;
        while(true) {
            st = st%(this.users);
            int diceRollValue = 0;
            int three = 3;
            int diceRoll;
            do{
                diceRoll = (int) (Math.random() * 6) + 1;
                three--;
                if(three == 0 && diceRoll == 6) {
                    st++;
                    break;
                }
                diceRollValue+=diceRoll;
            }while(diceRoll == 6);
            if(three == 0 && diceRoll == 6) {
                continue;
            }
            System.out.printf("User %d is rolling and get diceRoll value as %d%n", st+1, diceRollValue);
            int lastPlayerPosition = position[st];
            if (lastPlayerPosition + diceRollValue > this.cellCount) {
                System.out.printf("User %d gets an invalid move to cell %d%n", st + 1, lastPlayerPosition + diceRollValue);
                st++;
                continue;
            }
            int currPosition = lastPlayerPosition + diceRollValue;
            
            while(snakes.containsKey(currPosition) || ladders.containsKey(currPosition)) {
                if(snakes.containsKey(currPosition)) {
                    currPosition = snakes.get(currPosition);
                    System.out.println("User is bitten by snake");
                } else {
                    currPosition = ladders.get(currPosition);
                    System.out.println("User get the ladder");
                }
            }
            System.out.printf("User %d current position is %d%n", st+1, currPosition);

            if(currPosition == (this.cellCount)) {
                System.out.printf("User %d wins", st+1);
                break;
            }
            position[st] = currPosition;
            st++;
        }
    }
}

public class SnakeAndLadder {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("cellCount");
        int cellCount = sc.nextInt();
        System.out.println("users");
        int users = sc.nextInt();
        System.out.println("snakes number");
        int snakesCnt = sc.nextInt();
        System.out.println("Ladder no");
        int ladderCnt = sc.nextInt();
        System.out.println("Snake position");
        Map<Integer, Integer> snakes = new HashMap<>();
        System.out.println("Ladder position");
        Map<Integer, Integer> ladders = new HashMap<>();

        while(snakesCnt--!=0) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            snakes.put(x, y);
        }

        while(ladderCnt--!=0) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            ladders.put(x, y);
        }
        Game game = new Game(cellCount, users, snakes, ladders);
        game.start();
        sc.close();
    }
}
