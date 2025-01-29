package wordle.demo.rooms;

import javax.xml.datatype.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.*;

public class RoomKiller implements Runnable{
    Thread thread;
    final RoomService roomService;
    Long id;

    RoomKiller(Long id, RoomService roomService) {
        this.roomService = roomService;
        this.id = id;
        thread = new Thread(this, "RoomKiller for room:" + id);
    }

    @Override
    public void run() {
        while(true) {
            if(roomService.getById(id).isPresent()) {
                Room room = roomService.getById(id).get();
                java.time.Duration duration = java.time.Duration.between(room.getEmptyTime(), LocalDateTime.now());
                if (room.getPeopleAmount() > 0 || duration.toMinutes()<5) {
                    try {
                        wait(300000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else if (room.getPeopleAmount() == 0) {
                    roomService.deleteById(id);
                    break;
                }
            }else {
                break;
            }
        }
    }
}
