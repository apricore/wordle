package wordle.demo.stompController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
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

    @MessageMapping("/sessionId")
    @SendToUser(value = "/queue/unique")
    public String connect(SimpMessageHeaderAccessor headerAccessor, ClientMessage clientMessage) {
        return headerAccessor.getSessionId();
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
                response.put("event", Actions.ENTER_ROOM);
                response.put("code", serverMessageCollection.getCode());
                response.put("answer", serverMessageCollection.getAnswer());
                if (serverMessageCollection.getCode() == Events.SUCCEED) {
                    response.put("users", serverMessageCollection.getUsers());
                    users = userService.findAllByRoom_Id(clientMessage.getRoomId());
                }else {
                    messagingTemplate.convertAndSend("/queue/response-" + headerAccessor.getSessionId(), response);
                }
                break;
            }
            case Actions.LETS_PLAY: {
                serverMessageCollection = new UserController(userService, roomService).play(clientMessage);
                if (serverMessageCollection.getCode() == Events.FAILED) {
                    users.add(serverMessageCollection.getUser());
                }else {
                    users = serverMessageCollection.getUsers();
                    response.put("user", serverMessageCollection.getUser());
                }
                response.put("event", Actions.LETS_PLAY);
                response.put("code", serverMessageCollection.getCode());
                break;
            }
            case Actions.LEAVE_ROOM: {
                serverMessageCollection = new UserController(userService, roomService).leaveRoom(clientMessage);
                response.put("event", Actions.LEAVE_ROOM);
                response.put("code", serverMessageCollection.getCode());
                response.put("userId", serverMessageCollection.getUser().getId());
                users = serverMessageCollection.getUsers();
                messagingTemplate.convertAndSend("/queue/response-" + headerAccessor.getSessionId(), response);
                break;
            }
            case Actions.GIVE_UP: {
                serverMessageCollection = new UserController(userService, roomService).giveUp(clientMessage);
                if (serverMessageCollection.getCode() == Events.FAILED) {
                    users.add(serverMessageCollection.getUser());
                } else {
                    users = serverMessageCollection.getUsers();
                    response.put("user", serverMessageCollection.getUser());
                }
                response.put("event", Actions.GIVE_UP);
                response.put("code", serverMessageCollection.getCode());

                Map<String, Object> answerResponse = new HashMap<>();
                answerResponse.put("event", Actions.GIVE_UP);
                answerResponse.put("answer", serverMessageCollection.getAnswer());
                answerResponse.put("user", serverMessageCollection.getUser());
                messagingTemplate.convertAndSend("/queue/response-" + headerAccessor.getSessionId(), answerResponse);
                break;
            }
            case Actions.RESET_WORD: {
                serverMessageCollection = new UserController(userService, roomService).resetWord(clientMessage);
                response.put("event", Actions.RESET_WORD);
                response.put("code", serverMessageCollection.getCode());
                if (serverMessageCollection.getCode() == Events.SUCCEED) {
                    users = serverMessageCollection.getUsers();
                    response.put("answer", serverMessageCollection.getAnswer());
                    response.put("user", serverMessageCollection.getUser());
                } else {
                    users.add(serverMessageCollection.getUser());
                }
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
