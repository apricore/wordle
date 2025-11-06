package wordle.demo.stompController;

import lombok.Getter;
import lombok.Setter;
import wordle.demo.users.User;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ServerMessageCollection {

    private String event;

    private int code;

    private Long roomId;

    private List<User> users = new ArrayList<>();

    private User user;

    private String answer;
}
