package wordle.demo.rooms;

import wordle.demo.stompController.Actions;
import wordle.demo.stompController.ClientMessage;
import wordle.demo.stompController.Events;
import wordle.demo.stompController.ServerMessageCollection;

public class RoomController {

    private final RoomService roomService;
    RoomKiller roomKiller;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    public ServerMessageCollection createRoom(ClientMessage clientMessage) {

        Room newRoom = new Room();
        newRoom.setPassword(clientMessage.getPassword());
        newRoom.setPeopleAmount(0);
        newRoom = roomService.save(newRoom);

        ServerMessageCollection serverMessageCollection = new ServerMessageCollection();
        serverMessageCollection.setRoomId(newRoom.getId());
        serverMessageCollection.setEvent(Actions.CREATE_ROOM);
        serverMessageCollection.setCode(Events.SUCCEED);

        return serverMessageCollection;
    }

}
