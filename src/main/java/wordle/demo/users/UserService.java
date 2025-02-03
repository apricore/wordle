package wordle.demo.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
}
