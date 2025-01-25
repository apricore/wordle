package wordle.demo.rooms;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RoomClientMessage {

    public final static String CREATE = "create-room";

    public String action;

    public String password;

}

