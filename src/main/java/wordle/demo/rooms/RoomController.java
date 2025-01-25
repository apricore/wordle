package wordle.demo.rooms;

import wordle.demo.stompController.ClientMessage;
import wordle.demo.stompController.Events;
import wordle.demo.stompController.ServerMessage;

public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    public ServerMessage createRoom(ClientMessage clientMessage) {

        Room newRoom = new Room();
        newRoom.setPassword(clientMessage.getPassword());
        newRoom.setPeopleAmount(0);
        roomService.save(newRoom);

        ServerMessage serverMessage = new ServerMessage();
        serverMessage.setRoomId(newRoom.getId());
        serverMessage.setEvent(Events.ROOM_CREATED);

        return serverMessage;
    }
}
