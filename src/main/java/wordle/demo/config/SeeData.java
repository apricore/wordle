package wordle.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import wordle.demo.rooms.Room;
import wordle.demo.rooms.RoomService;
import wordle.demo.users.User;
import wordle.demo.users.UserService;

import javax.xml.crypto.Data;

@Component
public class SeeData implements CommandLineRunner {

    @Autowired
    private RoomService roomService;
    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        Room room = new Room();
        room.setPassword("s");
        room.setPeopleAmount(0);
        roomService.save(room);

        User user = new User();
        user.setState("haha");
        user.setUsername("jesse");
        user.setRoomId(room.getId());
        userService.save(user);
    }
}
