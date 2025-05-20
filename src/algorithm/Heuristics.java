package algorithm;

import core.Board;
import core.Piece;

public class Heuristics {
    // Manhattan distance heuristic - measures linear distance to exit
    public static double manhattanDistance(Board board) {
        Piece primary = board.getPrimaryPiece();
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();

        if (primary.isHorizontal()) {
            int pieceEndCol = primary.getCol() + primary.getSize() - 1;
            
            // Jika pintu keluar di luar grid, sesuaikan perhitungan jarak
            if (exitCol >= board.getCols()) {
                // Hitung jarak dari ujung kanan mobil ke tepi kanan grid
                return board.getCols() - 1 - pieceEndCol;
            } else {
                return Math.abs(pieceEndCol - exitCol);
            }
        } else {
            int pieceEndRow = primary.getRow() + primary.getSize() - 1;
            
            // Jika pintu keluar di luar grid, sesuaikan perhitungan jarak
            if (exitRow >= board.getRows()) {
                // Hitung jarak dari ujung bawah mobil ke tepi bawah grid
                return board.getRows() - 1 - pieceEndRow;
            } else {
                return Math.abs(pieceEndRow - exitRow);
            }
        }
    }

    // Blocking vehicles heuristic - counts the number of blocking vehicles
    public static double blockingVehicles(Board board) {
        Piece primary = board.getPrimaryPiece();
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        int count = 0;
        
        if (primary.isHorizontal()) {
            // Count vehicles blocking the horizontal path to exit
            int primaryRow = primary.getRow();
            int primaryEndCol = primary.getCol() + primary.getSize() - 1;
            
            // Determine direction to exit
            int startCol, endCol;
            
            if (exitCol >= board.getCols()) { // Pintu keluar di kanan grid
                startCol = primaryEndCol + 1;
                endCol = board.getCols() - 1; // Hanya hitung sampai batas grid
            } else if (exitCol < 0) { // Pintu keluar di kiri grid
                startCol = 0; // Mulai dari batas kiri grid
                endCol = primary.getCol() - 1;
            } else if (exitCol > primaryEndCol) { // Pintu keluar di kanan mobil utama
                startCol = primaryEndCol + 1;
                endCol = exitCol - 1; // -1 karena kita tidak memeriksa sel pintu keluar
            } else { // Pintu keluar di kiri mobil utama
                startCol = exitCol + 1; // +1 karena kita tidak memeriksa sel pintu keluar
                endCol = primary.getCol() - 1;
            }
            
            // Validasi batas
            startCol = Math.max(0, startCol);
            endCol = Math.min(board.getCols() - 1, endCol);
            
            // Check for blocking vehicles
            for (int col = startCol; col <= endCol; col++) {
                if (primaryRow >= 0 && primaryRow < board.getRows() && col >= 0 && col < board.getCols()) {
                    char cell = board.getGrid()[primaryRow][col];
                    if (cell != '.' && cell != 'K') {
                        count++;
                    }
                }
            }
        } else {
            // Count vehicles blocking the vertical path to exit
            int primaryCol = primary.getCol();
            int primaryEndRow = primary.getRow() + primary.getSize() - 1;
            
            // Determine direction to exit
            int startRow, endRow;
            
            if (exitRow >= board.getRows()) { // Pintu keluar di bawah grid
                startRow = primaryEndRow + 1;
                endRow = board.getRows() - 1; // Hanya hitung sampai batas grid
            } else if (exitRow < 0) { // Pintu keluar di atas grid
                startRow = 0; // Mulai dari batas atas grid
                endRow = primary.getRow() - 1;
            } else if (exitRow > primaryEndRow) { // Pintu keluar di bawah mobil utama
                startRow = primaryEndRow + 1;
                endRow = exitRow - 1; // -1 karena kita tidak memeriksa sel pintu keluar
            } else { // Pintu keluar di atas mobil utama
                startRow = exitRow + 1; // +1 karena kita tidak memeriksa sel pintu keluar
                endRow = primary.getRow() - 1;
            }
            
            // Validasi batas
            startRow = Math.max(0, startRow);
            endRow = Math.min(board.getRows() - 1, endRow);
            
            // Check for blocking vehicles
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

    // Combined heuristic - uses both Manhattan distance and blocking vehicles
    public static double combined(Board board) {
        return manhattanDistance(board) + 2 * blockingVehicles(board);
    }
    
    // Get heuristic by name
    public static double getHeuristic(Board board, String heuristicName) {
        switch (heuristicName.toLowerCase()) {
            case "manhattan":
                return manhattanDistance(board);
            case "blocking":
                return blockingVehicles(board);
            case "combined":
                return combined(board);
            default:
                return manhattanDistance(board); // Default to Manhattan distance
        }
    }
}