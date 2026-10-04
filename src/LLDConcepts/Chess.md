// package src.LLDConcepts;
// import java.util.*;

// class Board {
//     private static final int players;
//     private static final int dimensions;
//     private static final int totalPieces;
    
//     public Board(int players, int dimensions, int totalP) {
//         this.players = players;
//         this.dimensions = dimensions;
//         this.totalPieces = totalP;
//     }
// }

// abstract class Piece {
//     public String player;
//     public int x;
//     public int y;
//     public void assignPosition(String player, int x, int y) {
//         this.player = player;
//         this.x = x;
//         this.y = y;
//     }

//     List<Integer> getCurrentPosition() {
//         return new ArrayList<>(Arrays.asList(2, 5));
//     }
//     abstract void move();
//     abstract List<Integer> getvalidPositions(Piece piece);
//     abstract boolean move(int x, int y);
// }

// class Movestrategy {
//     Piece piece;
//     Movestrategy(Piece piece) {
//         this.piece = piece;
//     }

//     void movePiece() {
//         piece.move();
//     }
// }

// class Pawn extends Piece {
//     public void move() {

//     }
// }

// class King extends Piece {
    
// }

// class Queen implements Piece {
    
// }

// class Rook implements Piece {
    
// }

// class Bishop implements Piece {
    
// }

// class Knight implements Piece {
    
// }

// public class ChessPrac {   
//     public static void main() {
//         Board board = new Board(2, 8, 16);
//         Piece kingB = new King("black", 0, 0);
//         Piece queenB = new Queen("black");

//         for(int i = 0; i<8; i++) {
//             Piece pawn = new King("black", 0, 0);
//         }
//         Piece knightB = new Knight("black");
//         Piece knightW = new Knight("white");
//         Piece bishopB = new Bishop("black");
//         Piece bishopW = new Bishop("white");
//         Piece rookB = new Rook("black");
//         Piece rookW = new Rook("white");


//     }
// }
