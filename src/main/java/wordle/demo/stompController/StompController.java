package wordle.demo.stompController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import wordle.demo.rooms.RoomController;
import wordle.demo.rooms.RoomService;

@Controller
public class StompController {

    @Autowired
    private RoomService roomService;

    @MessageMapping("/")
    @SendTo("/")
    public ServerMessage taskCentre(ClientMessage clientMessage) {

        ServerMessage serverMessage = new ServerMessage();

        switch (clientMessage.getAction()) {
            case Actions.CREATE_ROOM: serverMessage = new RoomController(roomService).createRoom(clientMessage);
            break;

        }

        return serverMessage;
    }
}
