package wordle.demo.users;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.function.ServerResponse;
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

                for (User user1 : userService.findAllByRoom_Id(room.getId())) {
                    for (int i = 0; i < 100; i++) {
                        System.out.println(user1.getUsername());
                    }
                }

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
