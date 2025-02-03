package wordle.demo.stompController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import wordle.demo.rooms.RoomController;
import wordle.demo.rooms.RoomService;
import wordle.demo.users.User;
import wordle.demo.users.UserController;
import wordle.demo.users.UserService;

import java.io.IOException;
import java.util.*;

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
    public void SingleTaskCentre(SimpMessageHeaderAccessor headerAccessor, ClientMessage clientMessage) throws IOException {

        List<User> users = new ArrayList<>();
        ServerMessageCollection serverMessageCollection;
        Map<String, Object> response = new HashMap<>();

        switch (clientMessage.getAction()) {
            case Actions.CREATE_ROOM: {
                serverMessageCollection = new RoomController(roomService).createRoom(clientMessage);
                response.put("event", Actions.CREATE_ROOM);
                response.put("code", serverMessageCollection.getCode());
                response.put("roomId", serverMessageCollection.getRoomId());
                messagingTemplate.convertAndSend("/queue/response-" + headerAccessor.getSessionId(), response);
                break;
            }
            case Actions.ENTER_ROOM: {
                serverMessageCollection = new UserController(userService, roomService).enterRoom(headerAccessor, clientMessage);
                users = userService.findAllByRoom_Id(clientMessage.getRoomId());
                response.put("event", Actions.ENTER_ROOM);
                response.put("code", serverMessageCollection.getCode());
                response.put("users", serverMessageCollection.getUsers());
                break;
            }
            case Actions.LETS_PLAY: {
                serverMessageCollection = new UserController(userService, roomService).play(clientMessage);
                users = serverMessageCollection.getUsers();
                response.put("event", Actions.LETS_PLAY);
                response.put("code", serverMessageCollection.getCode());
                response.put("user", serverMessageCollection.getUser());
                break;
            }
        }
        if (!users.isEmpty()) {
            for (User user : users) {
                messagingTemplate.convertAndSend("/queue/response-" + user.getSessionId(), response);
            }
        }


        users.clear();
        response.clear();

    }
}
