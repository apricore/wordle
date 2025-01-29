package wordle.demo.rooms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;


    @EntityGraph(attributePaths = {"users"})
    public Optional<Room> getById(Long id) {
        return roomRepository.findById(id);
    }

    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    public Room save(Room room) {
        room.setEmptyTime(LocalDateTime.now());
        return roomRepository.save(room);
    }

    public void deleteById(Long id) {
        roomRepository.deleteById(id);
    }
}
