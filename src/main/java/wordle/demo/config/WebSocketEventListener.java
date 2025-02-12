package wordle.demo.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import wordle.demo.users.UserService;

@Component
public class WebSocketEventListener {

    UserService userService;

    WebSocketEventListener(UserService userService) {
        this.userService = userService;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (sessionId != null) {
            userService.findBySessionId(sessionId).ifPresent(user -> {userService.delete(user);});
        }
    }
}
