package wordle.demo.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wordle.demo.rooms.Room;
import wordle.demo.rooms.RoomService;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomService roomService;


    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAllByRoom_Id(Long roomId) {
        return userRepository.findAllByRoomId(roomId);
    }

    public Optional<User> findBySessionId(String username) {
        return userRepository.findBySessionId(username);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void delete(User user) {
        Optional<Room> room = roomService.getById(user.getRoomId());
        if (room.isPresent()) {
            room.get().setPeopleAmount(room.get().getPeopleAmount() - 1);
            roomService.save(room.get());
        }
        userRepository.delete(user);
    }
}
