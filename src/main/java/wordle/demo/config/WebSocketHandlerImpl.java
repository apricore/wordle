package wordle.demo.config;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WebSocketHandlerImpl extends TextWebSocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // When a connection is established, log or initialize data if needed
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Here, handle the incoming WebSocket message (which is a TextMessage)
        String payload = message.getPayload();
        System.out.println("Received message: " + payload);

        // You can handle the logic based on the message, e.g., create a room
        String response = createRoom(payload); // Example function to process the message

        // Send a response back to the client
        try {
            session.sendMessage(new TextMessage(response)); // Send response back
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Error occurred: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Handle the connection close if needed
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    private String createRoom(String message) {
        // Here, you can add your logic to create a room
        // For example, parse the JSON payload (or other format), and then process it
        return "Room created with message: " + message;
    }
}
