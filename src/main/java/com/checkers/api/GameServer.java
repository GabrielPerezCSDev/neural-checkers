package main.java.com.checkers.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import main.java.com.checkers.util.GameResponse;
import main.java.com.checkers.util.GameResponseUtil;

public class GameServer {

  private HttpServer gameServer;
  private final int port;
  private final String host;
  Map<String, GameThread> gameThreads = new ConcurrentHashMap<>();

  public GameServer(String host, int port) {
    this.host = host;
    this.port = port;
}

  String generateConnectionId() {
    return UUID.randomUUID().toString().substring(0, 12); // Or Base64.getEncoder().encodeToString(bytes)
  }

  public void startServer() throws IOException {
    gameServer = HttpServer.create(new InetSocketAddress(host, port), 0);

    gameServer.createContext(
      "/",
      exchange -> {
        // Add CORS headers first
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange
          .getResponseHeaders()
          .add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
          );
        exchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        // Handle OPTIONS request (preflight)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
          exchange.sendResponseHeaders(200, -1);
          return;
        }

        if (!exchange.getRequestMethod().equals("PUT")) {
          HttpUtils.sendResponse(exchange, 405, "Method not allowed");
          return;
        }

        // Get connection details
        try {
          Map<String, String> params = HttpUtils.parseRequestBody(
            exchange.getRequestBody()
          );

          String connectionId = params.get("connection-id");
          System.out.println("Using connId: " + connectionId);
          if (connectionId != null && gameThreads.containsKey(connectionId)) {
            // Already established -> Return a failure response with a message
            GameResponse<?> gameResponse = GameResponseUtil.generateResponse(
              false,
              "User already has established connection"
            );
            String responseJson = HttpUtils.formatGameResponse(gameResponse);
            System.out.println("Existing connection");
            exchange.sendResponseHeaders(200, responseJson.length());
            exchange.getResponseBody().write(responseJson.getBytes());
            return;
          }
          // Create a new connection
          connectionId = generateConnectionId(); // e.g., UUID substring
          System.out.println("New connection from: " + connectionId);

          GameThread gameThread = new GameThread(connectionId);
          gameThreads.put(connectionId, gameThread);
          gameThread.start();
          gameThread.newGame();

          // Return a success response with the connectionId as the message
          GameResponse<?> gameResponse = GameResponseUtil.generateResponse(
            true,
            connectionId
          );
          String responseJson = HttpUtils.formatGameResponse(gameResponse);

          exchange.sendResponseHeaders(200, responseJson.length());
          exchange.getResponseBody().write(responseJson.getBytes());
        } catch (Exception e) {
          System.out.println("[Error] " + e.getMessage());
          HttpUtils.sendResponse(exchange, 400, "[Error] " + e.getMessage());
        }
      }
    );

    gameServer.createContext(
      "/reset",
      exchange -> {
        // Add CORS headers first
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange
          .getResponseHeaders()
          .add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
          );
        exchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        // Handle OPTIONS request (preflight)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
          exchange.sendResponseHeaders(200, -1);
          return;
        }

        if (!exchange.getRequestMethod().equals("POST")) {
          HttpUtils.sendResponse(exchange, 405, "Method not allowed");
          return;
        }
        try {
          Map<String, String> params = HttpUtils.parseRequestBody(
            exchange.getRequestBody()
          );
          String connectionId = HttpUtils.getConnectionId(params);
          GameThread userThread = gameThreads.get(connectionId);

          if (userThread == null) {
            HttpUtils.sendResponse(
              exchange,
              200,
              "[Refused] no active connection"
            );
            return;
          }

          userThread.resetGame();
          System.out.println(connectionId + " reset their game");
          HttpUtils.sendResponse(exchange, 200, "[Success] reset game");
        } catch (Exception e) {
          HttpUtils.sendResponse(exchange, 400, "[Error] " + e.getMessage());
        }
      }
    );

    gameServer.createContext(
      "/stop",
      exchange -> {
        // Add CORS headers first
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange
          .getResponseHeaders()
          .add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
          );
        exchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        // Handle OPTIONS request (preflight)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
          exchange.sendResponseHeaders(200, -1);
          return;
        }

        if (!exchange.getRequestMethod().equals("POST")) {
          HttpUtils.sendResponse(exchange, 405, "Method not allowed");
          return;
        }

        try {
          Map<String, String> params = HttpUtils.parseRequestBody(
            exchange.getRequestBody()
          );
          String connectionId = HttpUtils.getConnectionId(params);
          GameThread userThread = gameThreads.get(connectionId);

          if (userThread == null) {
            HttpUtils.sendResponse(
              exchange,
              200,
              "[Refused] no active connection"
            );
            return;
          }

          userThread.newGame();
          System.out.println(connectionId + " stopped their game");
          HttpUtils.sendResponse(exchange, 200, "[Success] stopped game");
        } catch (Exception e) {
          HttpUtils.sendResponse(exchange, 400, "[Error] " + e.getMessage());
        }
      }
    );

    gameServer.createContext(
      "/start",
      exchange -> {
        // Add CORS headers first
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange
          .getResponseHeaders()
          .add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
          );
        exchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        // Handle OPTIONS request (preflight)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
          exchange.sendResponseHeaders(200, -1);
          return;
        }

        if (!exchange.getRequestMethod().equals("PUT")) {
          HttpUtils.sendResponse(exchange, 405, "Method not allowed");
          return;
        }

        try {
          Map<String, String> params = HttpUtils.parseRequestBody(
            exchange.getRequestBody()
          );
          String connectionId = HttpUtils.getConnectionId(params);

          GameThread userThread = gameThreads.get(connectionId);
          if (userThread == null) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] no active connection"
            );
            return;
          }

          if (userThread.hasActiveGame()) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] User has an active game"
            );
            return;
          }

          int difficulty = Integer.parseInt(params.get("difficulty"));
          int playerColor = Integer.parseInt(params.get("playerColor"));

          GameResponse<Void> gameResponse = userThread.startGame(
            difficulty,
            playerColor
          );

          String response = HttpUtils.formatGameResponse(gameResponse);

          exchange.getResponseHeaders().set("Content-Type", "application/json");
          HttpUtils.sendResponse(exchange, 200, response);
          System.out.println(connectionId + " started a new game");
        } catch (Exception e) {
          HttpUtils.sendResponse(exchange, 400, "[Error] " + e.getMessage());
        }
      }
    );

    gameServer.createContext(
      "/legal-moves",
      exchange -> {
        // Add CORS headers first
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange
          .getResponseHeaders()
          .add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
          );
        exchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        // Handle OPTIONS request (preflight)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
          exchange.sendResponseHeaders(200, -1);
          return;
        }

        if (!exchange.getRequestMethod().equals("PUT")) {
          HttpUtils.sendResponse(exchange, 405, "Method not allowed");
          return;
        }

        try {
          Map<String, String> params = HttpUtils.parseRequestBody(
            exchange.getRequestBody()
          );
          String connectionId = HttpUtils.getConnectionId(params);

          GameThread userThread = gameThreads.get(connectionId);
          if (userThread == null) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] no active connection"
            );
            return;
          }

          if (!userThread.hasActiveGame()) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] User needs to start a game"
            );
            return;
          }

          int row = Integer.parseInt(params.get("row"));
          int col = Integer.parseInt(params.get("col"));

          if (!userThread.isValidPiece(row, col)) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] Not a valid piece"
            );
            return;
          }

          GameResponse<ArrayList<int[]>> gameResponse = userThread.getLegalMoves(
            row,
            col
          );
          String response = HttpUtils.formatGameResponse(gameResponse);

          exchange.getResponseHeaders().set("Content-Type", "application/json");
          HttpUtils.sendResponse(exchange, 200, response);
        } catch (Exception e) {
          HttpUtils.sendResponse(exchange, 400, "[Error] " + e.getMessage());
        }
      }
    );

    gameServer.createContext(
      "/get-board",
      exchange -> {
        // Add CORS headers first
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange
          .getResponseHeaders()
          .add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
          );
        exchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        // Handle OPTIONS request (preflight)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
          exchange.sendResponseHeaders(200, -1);
          return;
        }
        if (!exchange.getRequestMethod().equals("POST")) {
          HttpUtils.sendResponse(exchange, 405, "Method not allowed");
          return;
        }

        try {
          Map<String, String> params = HttpUtils.parseRequestBody(
            exchange.getRequestBody()
          );
          String connectionId = HttpUtils.getConnectionId(params);

          GameThread userThread = gameThreads.get(connectionId);
          if (userThread == null) {
            GameResponse<Void> errorResponse = new GameResponse<>(
              false,
              "[Refused] no active connection"
            );
            String errorJson = HttpUtils.formatGameResponse(errorResponse);
            exchange
              .getResponseHeaders()
              .set("Content-Type", "application/json");
            HttpUtils.sendResponse(exchange, 404, errorJson);
            return;
          }

          GameResponse<int[][]> gameResponse = userThread.getBoard();
          //cpmvert to a string
          String successJson = HttpUtils.formatGameResponse(gameResponse);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          HttpUtils.sendResponse(exchange, 200, successJson);
        } catch (Exception e) {
          // Another error scenario
          GameResponse<Void> exceptionResponse = new GameResponse<>(
            false,
            "[Error] " + e.getMessage()
          );
          String exceptionJson = HttpUtils.formatGameResponse(
            exceptionResponse
          );
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          HttpUtils.sendResponse(exchange, 400, exceptionJson);
        }
      }
    );

    gameServer.createContext(
      "/player-move",
      exchange -> {
        // Add CORS headers first
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange
          .getResponseHeaders()
          .add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
          );
        exchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        // Handle OPTIONS request (preflight)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
          exchange.sendResponseHeaders(200, -1);
          return;
        }
        if (!exchange.getRequestMethod().equals("POST")) {
          HttpUtils.sendResponse(exchange, 405, "Method not allowed");
          return;
        }

        try {
          Map<String, String> params = HttpUtils.parseRequestBody(
            exchange.getRequestBody()
          );
          String connectionId = HttpUtils.getConnectionId(params);

          GameThread userThread = gameThreads.get(connectionId);
          if (userThread == null) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] no active connection"
            );
            return;
          }

          if (!userThread.hasActiveGame()) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] User needs to start a game"
            );
            return;
          }

          int fRow = Integer.parseInt(params.get("f-row"));
          int fCol = Integer.parseInt(params.get("f-col"));
          int tRow = Integer.parseInt(params.get("t-row"));
          int tCol = Integer.parseInt(params.get("t-col"));

          GameResponse<Void> gameResponse = userThread.makePlayerMove(fRow, fCol, tRow, tCol);
          String response = HttpUtils.formatGameResponse(gameResponse);

          exchange.getResponseHeaders().set("Content-Type", "application/json");
          HttpUtils.sendResponse(exchange, 200, response);
        }catch(Error e){
          HttpUtils.sendResponse(exchange, 400, "[Error] " + e.getMessage());
        }
      }
    );

    gameServer.createContext(
      "/make-ai-move",
      exchange -> {
        // Add CORS headers first
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange
          .getResponseHeaders()
          .add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
          );
        exchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        // Handle OPTIONS request (preflight)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
          exchange.sendResponseHeaders(200, -1);
          return;
        }
        if (!exchange.getRequestMethod().equals("PUT")) {
          HttpUtils.sendResponse(exchange, 405, "Method not allowed");
          return;
        }

        try {
          Map<String, String> params = HttpUtils.parseRequestBody(
            exchange.getRequestBody()
          );
          String connectionId = HttpUtils.getConnectionId(params);

          GameThread userThread = gameThreads.get(connectionId);

          if (userThread == null) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] no active connection"
            );
            return;
          }

          if (!userThread.hasActiveGame()) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] User needs to start a game"
            );
            return;
          }


          GameResponse<Void> gameResponse = userThread.makeAIMove();
          String response = HttpUtils.formatGameResponse(gameResponse);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          HttpUtils.sendResponse(exchange, 200, response);

        }catch(Exception e){
          HttpUtils.sendResponse(exchange, 400, "[Error] " + e.getMessage());
        }
        
      }
    );

    
    gameServer.createContext(
      "/game-status",
      exchange -> {
        // Add CORS headers first
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange
          .getResponseHeaders()
          .add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
          );
        exchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        // Handle OPTIONS request (preflight)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
          exchange.sendResponseHeaders(200, -1);
          return;
        }
        if (!exchange.getRequestMethod().equals("PUT")) {
          HttpUtils.sendResponse(exchange, 405, "Method not allowed");
          return;
        }

        try {
          Map<String, String> params = HttpUtils.parseRequestBody(
            exchange.getRequestBody()
          );
          String connectionId = HttpUtils.getConnectionId(params);

          GameThread userThread = gameThreads.get(connectionId);

          if (userThread == null) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] no active connection"
            );
            return;
          }

          if (!userThread.hasActiveGame()) {
            HttpUtils.sendResponse(
              exchange,
              404,
              "[Refused] User needs to start a game"
            );
            return;
          }

          GameResponse<Integer> gameResponse = userThread.gameStatus();
          String response = HttpUtils.formatGameResponse(gameResponse);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          HttpUtils.sendResponse(exchange, 200, response);

        }catch(Exception e){
          HttpUtils.sendResponse(exchange, 400, "[Error] " + e.getMessage());
        }
      }
    );

    gameServer.start();
    System.out.println("Server started on port " + port);
  }

  public void stopServer() {
    if (gameServer != null) {
      gameServer.stop(0); // 0 means stop immediately
      System.out.println("Server stopped");
    }
  }

  public HttpServer getServer() {
    return gameServer;
  }

  private class HttpUtils {

    // Parse incoming requests
    public static Map<String, String> parseRequestBody(InputStream requestBody)
      throws IOException {
      if (requestBody == null) {
        throw new IOException("Request body is empty");
      }

      byte[] bytes = requestBody.readAllBytes();
      if (bytes.length == 0) {
        throw new IOException("Request body is empty");
      }

      String requestString = new String(bytes);
      if (requestString.trim().isEmpty()) {
        throw new IOException("Request body is empty");
      }

      Map<String, String> params = new HashMap<>();

      // Remove curly braces and extra whitespace
      requestString = requestString.replaceAll("[{}]", "").trim();

      // Split on commas, but handle quotes properly
      String[] pairs = requestString.split(",");
      for (String pair : pairs) {
        // Split on colon and handle quotes
        String[] keyValue = pair.split(":");
        if (keyValue.length == 2) {
          String key = keyValue[0].replaceAll("[\"\\s]", "");
          String value = keyValue[1].replaceAll("[\"\\s]", "");
          params.put(key, value);
        }
      }

      if (params.isEmpty()) {
        throw new IOException("No valid key-value pairs found in request body");
      }

      return params;
    }

    // Handle sending responses
    public static void sendResponse(
      HttpExchange exchange,
      int statusCode,
      String message
    ) throws IOException {
      exchange.sendResponseHeaders(statusCode, message.length());
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(message.getBytes());
      }
    }

    // Format JSON responses with generic data
    public static <T> String formatGameResponse(GameResponse<T> response) {
      if (response.getData() == null) {
        return String.format(
          "{\"success\": %b, \"message\": \"%s\"}",
          response.isSuccess(),
          response.getMessage()
        );
      }

      return String.format(
        "{\"success\": %b, \"message\": \"%s\", \"data\": %s}",
        response.isSuccess(),
        response.getMessage(),
        formatData(response.getData())
      );
    }

    private static String formatData(Object data) {
      if (data instanceof int[][]) {
        return formatBoardArray((int[][]) data);
      }
      if (
        data instanceof ArrayList<?> &&
        !((ArrayList<?>) data).isEmpty() &&
        ((ArrayList<?>) data).get(0) instanceof int[]
      ) {
        return formatMovesArray((ArrayList<int[]>) data);
      }
      return data.toString();
    }

    // Helper method specifically for board array
    private static String formatBoardArray(int[][] board) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i < board.length; i++) {
        sb.append(Arrays.toString(board[i]));
        if (i < board.length - 1) {
          sb.append(",");
        }
      }
      sb.append("]");
      return sb.toString();
    }

    private static String formatMovesArray(ArrayList<int[]> moves) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i < moves.size(); i++) {
        sb.append(Arrays.toString(moves.get(i)));
        if (i < moves.size() - 1) {
          sb.append(",");
        }
      }
      sb.append("]");
      return sb.toString();
    }

    // Get connection ID
    public static String getConnectionId(Map<String, String> params) {
      String connectionId = params.get("connection-id");
      if (connectionId == null) {
        throw new IllegalArgumentException("Missing connection-id");
      }
      return connectionId;
    }
  }
}
