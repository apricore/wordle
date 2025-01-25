package wordle.demo.rooms;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RoomServerMessage {

    public final static String CREATED = "room-created";

    public String event;

    public int roomId;

}
