package wordle.demo.rooms;

import org.springframework.core.io.ClassPathResource;
import wordle.demo.stompController.Actions;
import wordle.demo.stompController.ClientMessage;
import wordle.demo.stompController.Events;
import wordle.demo.stompController.ServerMessageCollection;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class RoomController {

    private final RoomService roomService;
    static ClassPathResource answerLib = new ClassPathResource("static/answer");
    static ClassPathResource wordLib = new ClassPathResource("static/lib");
    RoomKiller roomKiller;
    static int answerLength = 415;
    static int wordsLength = 21955;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
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

    static public String randomAnswerChooser() {
        String answer = "";
        try {
            Scanner scanner = new Scanner(answerLib.getInputStream());
            int length = new Random().nextInt(answerLength);
            for (int i = 0; i < length; i++) {
                answer = scanner.nextLine();
            }
        } catch (Exception e) {
            answer = "APPLE";
        }
        return answer.toUpperCase();
    }

    static public boolean validateWord(String word) throws IOException {
        try (Scanner scanner = new Scanner(wordLib.getInputStream())) {
            for (int a = 0; a < wordsLength; a++) { // Reads word by word
                if (word.equals(scanner.nextLine().toUpperCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
