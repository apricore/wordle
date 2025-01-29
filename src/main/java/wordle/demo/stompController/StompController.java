package wordle.demo.stompController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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

    @MessageMapping("/")
    @SendTo("/")
    public ServerMessage taskCentre(ClientMessage clientMessage) throws InterruptedException {

        ServerMessage serverMessage = new ServerMessage();

        switch (clientMessage.getAction()) {
            case Actions.CREATE_ROOM: serverMessage = new RoomController(roomService).createRoom(clientMessage);
            break;
            case Actions.ENTER_ROOM: serverMessage = new UserController(userService, roomService).enterRoom(clientMessage);
            break;
        }

        return serverMessage;
    }
}
