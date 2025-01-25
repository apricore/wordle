package wordle.demo.rooms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public Optional<Room> getById(int id) {
        return roomRepository.findById(id);
    }

    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    public Room save(Room room) {
        room.setEmptyTime(LocalDateTime.now());
        return roomRepository.save(room);
    }
}
