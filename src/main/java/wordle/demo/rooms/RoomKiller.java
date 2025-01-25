package wordle.demo.rooms;

public class RoomKiller implements Runnable{
    Thread thread;


    RoomKiller(int id) {
        thread = new Thread(this, "RoomKiller for room:" + id);
    }

    @Override
    public void run() {

    }
}
