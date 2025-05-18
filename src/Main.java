import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import core.Board;
import core.GameState;
import algorithm.AStar;
import algorithm.Dijkstra;

public class Main {
    private static final String[] VALID_ALGORITHMS = {"astar", "dijkstra", "gbfs", "ucs"};
    private static final String[] VALID_HEURISTICS = {"manhattan", "blocking", "combined"};
    
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                // Jika ada argumen berarti file langsung dijalankan dari command line
                runPuzzleFromFile(args[0]);
            } else {
                // Gunakan test file default
                runPuzzleFromFile("test/test1.txt");
            }
        } catch (IOException e) {
            System.out.println("Error loading board: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run puzzle from file with algorithm and heuristic specified in the file
     */
    private static void runPuzzleFromFile(String filepath) throws IOException {
        // Baca file input untuk mendapatkan konfigurasi board, algoritma, dan heuristic
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        
        // Baca semua baris untuk mendapatkan konfigurasi papan
        StringBuilder boardConfigBuilder = new StringBuilder();
        
        String line;
        String algorithm = null;
        String heuristic = null;
        int lineCount = 0;
        int numRows = 0;
        
        while ((line = reader.readLine()) != null) {
            lineCount++;
            
            if (lineCount == 1) {
                // Baris pertama berisi dimensi papan
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    numRows = Integer.parseInt(parts[0]);
                }
                boardConfigBuilder.append(line).append("\n");
            } else if (lineCount == 2) {
                // Baris kedua berisi jumlah piece
                boardConfigBuilder.append(line).append("\n");
            } else if (lineCount <= numRows + 2) {
                // Baris-baris berikutnya berisi konfigurasi papan
                boardConfigBuilder.append(line).append("\n");
            } else if (algorithm == null) {
                // Baris setelah konfigurasi papan berisi algoritma
                algorithm = line.trim().toLowerCase();
            } else if (heuristic == null) {
                // Baris terakhir berisi heuristic (jika ada)
                heuristic = line.trim().toLowerCase();
            }
        }
        reader.close();
        
        // Validasi dan set default untuk algoritma
        boolean validAlgorithm = false;
        for (String validAlg : VALID_ALGORITHMS) {
            if (validAlg.equals(algorithm)) {
                validAlgorithm = true;
                break;
            }
        }
        
        if (!validAlgorithm) {
            algorithm = "astar"; // Default
            System.out.println("Invalid or no algorithm specified, using A* as default.");
        }
        
        // Validasi dan set default untuk heuristic
        boolean validHeuristic = false;
        for (String validHeur : VALID_HEURISTICS) {
            if (validHeur.equals(heuristic)) {
                validHeuristic = true;
                break;
            }
        }
        
        if (!validHeuristic) {
            heuristic = "manhattan"; // Default
            System.out.println("Invalid or no heuristic specified, using Manhattan distance as default.");
        }
        
        // UCS tidak menggunakan heuristic
        if (algorithm.equals("ucs")) {
            System.out.println("Note: UCS does not use heuristic functions, ignoring heuristic setting.");
        }
        
        // Buat temporary file untuk konfigurasi papan saja
        java.io.File tempFile = java.io.File.createTempFile("rushHourBoard", ".txt");
        tempFile.deleteOnExit();
        
        java.io.FileWriter writer = new java.io.FileWriter(tempFile);
        writer.write(boardConfigBuilder.toString());
        writer.close();
        
        // Load board dari file temporary
        Board board = new Board(tempFile.getAbsolutePath());
        
        System.out.println("=== Puzzle Configuration ===");
        System.out.println("Algorithm: " + algorithm);
        if (!algorithm.equals("ucs")) {
            System.out.println("Heuristic: " + heuristic);
        }
        System.out.println("\nInitial Board:");
        board.printBoard(null);
        
        // Jalankan algoritma yang sesuai
        runAlgorithm(algorithm, heuristic, board);
    }
    
    /**
     * Run the specified algorithm with the specified heuristic
     */
    private static void runAlgorithm(String algorithm, String heuristic, Board board) {
        if (!algorithm.equals("ucs")) {
            System.out.println("\n=== Running " + algorithm.toUpperCase() + " with " + heuristic + " heuristic ===");
        } else {
            System.out.println("\n=== Running " + algorithm.toUpperCase() + " ===");
        }
        
        GameState solution = null;
        
        switch (algorithm) {
            case "astar":
                AStar astar = new AStar(heuristic);
                solution = astar.solve(board);
                
                if (solution != null) {
                    System.out.println("Solution found with " + solution.getMoves().size() + " steps");
                    System.out.println("Nodes visited: " + astar.getNodesVisited());
                    System.out.println("Execution time: " + astar.getExecutionTime());
                    System.out.println("\nSolution path:");
                    astar.printSolution(solution);
                } else {
                    System.out.println("No solution found!");
                    System.out.println("Nodes visited: " + astar.getNodesVisited());
                    System.out.println("Execution time: " + astar.getExecutionTime());
                }
                break;
                
            case "dijkstra":
                Dijkstra dijkstra = new Dijkstra();
                solution = dijkstra.solve(board);
                
                if (solution != null) {
                    System.out.println("Solution found with " + solution.getMoves().size() + " steps");
                    System.out.println("Nodes visited: " + dijkstra.getNodesVisited());
                    System.out.println("Execution time: " + dijkstra.getExecutionTime());
                    System.out.println("\nSolution path:");
                    dijkstra.printSolution(solution);
                } else {
                    System.out.println("No solution found!");
                    System.out.println("Nodes visited: " + dijkstra.getNodesVisited());
                    System.out.println("Execution time: " + dijkstra.getExecutionTime());
                }
                break;

            // case "gbfs":
            //     GBFS gbfs = new GBFS(heuristic);
            //     solution = gbfs.solve(board);
                
            //     if (solution != null) {
            //         System.out.println("Solution found with " + solution.getMoves().size() + " steps");
            //         System.out.println("Nodes visited: " + gbfs.getNodesVisited());
            //         System.out.println("Execution time: " + gbfs.getExecutionTime());
            //         System.out.println("\nSolution path:");
            //         gbfs.printSolution(solution);
            //     } else {
            //         System.out.println("No solution found!");
            //         System.out.println("Nodes visited: " + gbfs.getNodesVisited());
            //         System.out.println("Execution time: " + gbfs.getExecutionTime());
            //     }
            //     break;
                
            // case "ucs":
            //     UCS ucs = new UCS();
            //     solution = ucs.solve(board);
                
            //     if (solution != null) {
            //         System.out.println("Solution found with " + solution.getMoves().size() + " steps");
            //         System.out.println("Nodes visited: " + ucs.getNodesVisited());
            //         System.out.println("Execution time: " + ucs.getExecutionTime());
            //         System.out.println("\nSolution path:");
            //         ucs.printSolution(solution);
            //     } else {
            //         System.out.println("No solution found!");
            //         System.out.println("Nodes visited: " + ucs.getNodesVisited());
            //         System.out.println("Execution time: " + ucs.getExecutionTime());
            //     }
            //     break;
                
            default:
                System.out.println("Unknown algorithm: " + algorithm);
                System.out.println("Available algorithms: astar, bidirectional, gbfs, ucs");
                break;
        }
    }
}