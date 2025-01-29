package wordle.demo.stompController;

import lombok.Getter;
import lombok.Setter;
import wordle.demo.users.User;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ServerMessage {

    private String event;

    private int code;

    private Long roomId;

    private List<User> users = new ArrayList<>();
}
