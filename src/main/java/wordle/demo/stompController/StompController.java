package wordle.demo.stompController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import wordle.demo.rooms.RoomController;
import wordle.demo.rooms.RoomService;
import wordle.demo.users.UserController;
import wordle.demo.users.UserService;

@Controller
public class StompController {

    @Autowired
    private RoomService roomService;
    @Autowired
    private UserService userService;


    private final SimpMessagingTemplate messagingTemplate;

    public StompController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/send")
    @SendToUser(value = "/queue/response", broadcast = false)
    public ServerMessage taskCentre(ClientMessage clientMessage) {

//        String sessionId = headerAccessor.getSessionId();

//        for (int i = 0; i < 10; i++) {
//            System.out.println(sessionId);
//        }

        ServerMessage serverMessage = new ServerMessage();

        switch (clientMessage.getAction()) {
            case Actions.CREATE_ROOM: serverMessage = new RoomController(roomService).createRoom(clientMessage);
            break;
            case Actions.ENTER_ROOM: serverMessage = new UserController(userService, roomService).enterRoom(clientMessage);
            break;
        }
        return serverMessage;

//        assert sessionId != null;
//        messagingTemplate.convertAndSendToUser(sessionId, "/queue/response", serverMessage);
    }
}
