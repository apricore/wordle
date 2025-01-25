package wordle.demo.stompController;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientMessage {

    private String action;

    private String password;
}
