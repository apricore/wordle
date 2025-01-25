package wordle.demo.stompController;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServerMessage {

    private String event;

    private int roomId;
}
