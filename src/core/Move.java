package core;

public class Move {
    private char pieceId;
    private String direction;
    private int steps; // Jumlah langkah yang diambil

    // Constructor dengan jumlah langkah default = 1
    public Move(char pieceId, String direction) {
        this(pieceId, direction, 1);
    }

    // Constructor dengan langkah eksplisit
    public Move(char pieceId, String direction, int steps) {
        if (!isValidDirection(direction)) {
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }
        if (steps < 1) {
            throw new IllegalArgumentException("Steps must be at least 1");
        }
        this.pieceId = pieceId;
        this.direction = direction;
        this.steps = steps;
    }

    // Constructor dari string format
    public Move(String moveString) {
        String[] parts = moveString.split("-");
        if (parts.length < 2 || parts.length > 3 || parts[0].length() != 1) {
            throw new IllegalArgumentException("Invalid move format: " + moveString);
        }
        
        this.pieceId = parts[0].charAt(0);
        this.direction = parts[1];
        
        if (!isValidDirection(direction)) {
            throw new IllegalArgumentException("Invalid direction in move: " + direction);
        }
        
        // Parse steps if provided
        this.steps = (parts.length == 3) ? Integer.parseInt(parts[2]) : 1;
        
        if (steps < 1) {
            throw new IllegalArgumentException("Steps must be at least 1");
        }
    }

    private boolean isValidDirection(String direction) {
        return direction.equals("kiri") || direction.equals("kanan") ||
               direction.equals("atas") || direction.equals("bawah");
    }

    public char getPieceId() {
        return pieceId;
    }

    public String getDirection() {
        return direction;
    }
    
    public int getSteps() {
        return steps;
    }
    
    @Override
    public String toString() {
        if (steps == 1) {
            return pieceId + "-" + direction;
        } else {
            return pieceId + "-" + direction + "-" + steps;
        }
    }
}