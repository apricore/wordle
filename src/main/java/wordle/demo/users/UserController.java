package wordle.demo.users;

import wordle.demo.rooms.Room;
import wordle.demo.rooms.RoomService;
import wordle.demo.stompController.Actions;
import wordle.demo.stompController.ClientMessage;
import wordle.demo.stompController.Events;
import wordle.demo.stompController.ServerMessage;

public class UserController {

    UserService userService;
    RoomService roomService;

    public UserController(UserService userService, RoomService roomService) {
        this.userService = userService;
        this.roomService = roomService;
    }

    public ServerMessage enterRoom(ClientMessage clientMessage) {
        ServerMessage serverMessage = new ServerMessage();
        serverMessage.setEvent(Actions.ENTER_ROOM);

        if (roomService.getById(clientMessage.getRoomId()).isPresent()) {

            Room room = roomService.getById(clientMessage.getRoomId()).get();
            if (room.getPassword().equals(clientMessage.getPassword())) {
                User user = new User();
                user.setUsername(clientMessage.getUsername());
                user.setRoomId(room.getId());
                user.setState("whatever");
                userService.save(user);

                room.setPeopleAmount(room.getPeopleAmount() + 1);

                serverMessage.setCode(Events.SUCCEED);

                serverMessage.getUsers().addAll(userService.findAllByRoom_Id(room.getId()));
            }else {
                serverMessage.setCode(Events.FAILED);
            }
        }else {
            serverMessage.setCode(Events.NOT_FOUND);
        }
        return serverMessage;
    }
}
