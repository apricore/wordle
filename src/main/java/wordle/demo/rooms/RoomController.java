package wordle.demo.rooms;

import org.springframework.core.io.ClassPathResource;
import wordle.demo.stompController.Actions;
import wordle.demo.stompController.ClientMessage;
import wordle.demo.stompController.Events;
import wordle.demo.stompController.ServerMessageCollection;

import java.util.Random;
import java.util.Scanner;

public class RoomController {

    private final RoomService roomService;
    String[] answerLib = new String[415];
    RoomKiller roomKiller;
    String answer;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
        ClassPathResource lib = new ClassPathResource("static/lib");
        try {
            Scanner scanner = new Scanner(lib.getInputStream());
            for (int i = 0; i < 415; i++) {
                answer = scanner.nextLine();
                answerLib[i] = answer;
            }
        }catch (Exception e) {
            answer = "APPLE";
        }
    }

    public ServerMessageCollection createRoom(ClientMessage clientMessage) {

        Room newRoom = new Room();
        newRoom.setPassword(clientMessage.getPassword());
        newRoom.setPeopleAmount(0);
        newRoom.setAnswer(randomAnswerChooser());
        newRoom = roomService.save(newRoom);

        ServerMessageCollection serverMessageCollection = new ServerMessageCollection();
        serverMessageCollection.setRoomId(newRoom.getId());
        serverMessageCollection.setEvent(Actions.CREATE_ROOM);
        serverMessageCollection.setCode(Events.SUCCEED);

        return serverMessageCollection;
    }

    public String randomAnswerChooser() {
       int a = new Random().nextInt(answerLib.length);
       answer = answerLib[a];
       for (int i = 0; i < 10; i++) {
           System.out.println(answer);
       }
       return answer.toUpperCase();
    }

}
