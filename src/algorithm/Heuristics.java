package algorithm;

import core.Board;
import core.Piece;

public class Heuristics {
    public static double manhattanDistance(Board board) {
        Piece primary = board.getPrimaryPiece();
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();

        if (primary.isHorizontal()) {
            int pieceEndCol = primary.getCol() + primary.getSize() - 1;
            
            if (exitCol >= board.getCols()) {
                return board.getCols() - 1 - pieceEndCol;
            } else {
                return Math.abs(pieceEndCol - exitCol);
            }
        } else {
            int pieceEndRow = primary.getRow() + primary.getSize() - 1;
            
            if (exitRow >= board.getRows()) {
                return board.getRows() - 1 - pieceEndRow;
            } else {
                return Math.abs(pieceEndRow - exitRow);
            }
        }
    }

    public static double blockingVehicles(Board board) {
        Piece primary = board.getPrimaryPiece();
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        int count = 0;
        
        if (primary.isHorizontal()) {
            int primaryRow = primary.getRow();
            int primaryEndCol = primary.getCol() + primary.getSize() - 1;
            
            int startCol, endCol;
            
            if (exitCol >= board.getCols()) { 
                startCol = primaryEndCol + 1;
                endCol = board.getCols() - 1;
            } else if (exitCol < 0) { 
                startCol = 0; 
                endCol = primary.getCol() - 1;
            } else if (exitCol > primaryEndCol) { 
                startCol = primaryEndCol + 1;
                endCol = exitCol - 1; 
            } else { 
                startCol = exitCol + 1; 
                endCol = primary.getCol() - 1;
            }
            
            startCol = Math.max(0, startCol);
            endCol = Math.min(board.getCols() - 1, endCol);
            
            for (int col = startCol; col <= endCol; col++) {
                if (primaryRow >= 0 && primaryRow < board.getRows() && col >= 0 && col < board.getCols()) {
                    char cell = board.getGrid()[primaryRow][col];
                    if (cell != '.' && cell != 'K') {
                        count++;
                    }
                }
            }
        } else {
            int primaryCol = primary.getCol();
            int primaryEndRow = primary.getRow() + primary.getSize() - 1;
            
            int startRow, endRow;
            
            if (exitRow >= board.getRows()) { 
                startRow = primaryEndRow + 1;
                endRow = board.getRows() - 1; 
            } else if (exitRow < 0) { 
                startRow = 0; 
                endRow = primary.getRow() - 1;
            } else if (exitRow > primaryEndRow) { 
                startRow = primaryEndRow + 1;
                endRow = exitRow - 1; 
            } else { 
                startRow = exitRow + 1; 
                endRow = primary.getRow() - 1;
            }
            
            startRow = Math.max(0, startRow);
            endRow = Math.min(board.getRows() - 1, endRow);
            
            for (int row = startRow; row <= endRow; row++) {
                if (row >= 0 && row < board.getRows() && primaryCol >= 0 && primaryCol < board.getCols()) {
                    char cell = board.getGrid()[row][primaryCol];
                    if (cell != '.' && cell != 'K') {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }

    public static double combined(Board board) {
        return manhattanDistance(board) + 2 * blockingVehicles(board);
    }
    
    public static double getHeuristic(Board board, String heuristicName) {
        switch (heuristicName.toLowerCase()) {
            case "manhattan":
                return manhattanDistance(board);
            case "blocking":
                return blockingVehicles(board);
            case "combined":
                return combined(board);
            default:
                return manhattanDistance(board);
        }
    }
}