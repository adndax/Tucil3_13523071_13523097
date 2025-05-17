package core;

public class Move {
    private char pieceId;
    private String direction;

    public Move(char pieceId, String direction) {
        if (!isValidDirection(direction)) {
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }
        this.pieceId = pieceId;
        this.direction = direction;
    }

    public Move(String moveString) {
        String[] parts = moveString.split("-");
        if (parts.length != 2 || parts[0].length() != 1) {
            throw new IllegalArgumentException("Invalid move format: " + moveString);
        }
        this.pieceId = parts[0].charAt(0);
        this.direction = parts[1];
        if (!isValidDirection(direction)) {
            throw new IllegalArgumentException("Invalid direction in move: " + direction);
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

    public String toString() {
        return pieceId + "-" + direction;
    }
}