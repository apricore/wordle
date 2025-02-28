package wordle.demo.users;

import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import wordle.demo.rooms.Room;
import wordle.demo.rooms.RoomController;
import wordle.demo.rooms.RoomService;
import wordle.demo.stompController.Actions;
import wordle.demo.stompController.ClientMessage;
import wordle.demo.stompController.Events;
import wordle.demo.stompController.ServerMessageCollection;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class UserController {

    UserService userService;
    RoomService roomService;
    Boolean validation = false;

    public UserController( UserService userService, RoomService roomService) {
        this.userService = userService;
        this.roomService = roomService;
    }

    public ServerMessageCollection enterRoom(SimpMessageHeaderAccessor headerAccessor, ClientMessage clientMessage) {
        ServerMessageCollection serverMessageCollection = new ServerMessageCollection();
        serverMessageCollection.setEvent(Actions.ENTER_ROOM);

        if (roomService.getById(clientMessage.getRoomId()).isPresent()) {

            Room room = roomService.getById(clientMessage.getRoomId()).get();
            if (room.getPassword().equals(clientMessage.getPassword())) {
                User user = new User();
                user.setUsername(clientMessage.getUsername());
                user.setRoomId(room.getId());
                user.setState(new String());
                user.setSessionId(headerAccessor.getSessionId());
                userService.save(user);

                room.setPeopleAmount(room.getPeopleAmount() + 1);
                roomService.save(room);

                serverMessageCollection.setCode(Events.SUCCEED);

                serverMessageCollection.getUsers().addAll(userService.findAllByRoom_Id(room.getId()));
            }else {
                serverMessageCollection.setCode(Events.FAILED);
            }
        }else {
            serverMessageCollection.setCode(Events.NOT_FOUND);
        }
        return serverMessageCollection;
    }

    public ServerMessageCollection play(ClientMessage clientMessage) throws IOException {
        ServerMessageCollection serverMessageCollection = new ServerMessageCollection();
        serverMessageCollection.setEvent(Actions.LETS_PLAY);
        if (userService.findById(clientMessage.getUserId()).isPresent()) {
            User user = userService.findById(clientMessage.getUserId()).get();
            Room room = roomService.getById(user.getRoomId()).get();
            String state = UserStateGiver(clientMessage.getInputWord(), room.getAnswer());
            if (state.charAt(0) == '0') {
                serverMessageCollection.setCode(Events.FAILED);
            }else {
                user.setState(user.getState() + state);
                userService.save(user);
                serverMessageCollection.setCode(Events.SUCCEED);
            }
            serverMessageCollection.setUser(user);
            serverMessageCollection.getUsers().addAll(userService.findAllByRoom_Id(user.getRoomId()));
        }else {
            serverMessageCollection.setCode(Events.NOT_FOUND);
        }
        return serverMessageCollection;
    }

    public ServerMessageCollection leaveRoom(ClientMessage clientMessage) throws IOException {
        ServerMessageCollection serverMessageCollection = new ServerMessageCollection();
        serverMessageCollection.setEvent(Actions.LEAVE_ROOM);
        if (userService.findById(clientMessage.getUserId()).isPresent()) {
            userService.findById(clientMessage.getUserId()).ifPresent(user -> {userService.delete(user);});
            serverMessageCollection.setCode(Events.SUCCEED);
        }else {
            serverMessageCollection.setCode(Events.NOT_FOUND);
        }
        return serverMessageCollection;
    }

    public String UserStateGiver(String input, String answer) throws IOException {
        char[] state = "BBBBB".toCharArray();

        ClassPathResource classPathResource = new ClassPathResource("static/answer");

        try (Scanner scanner = new Scanner(classPathResource.getInputStream())) {
            for (int a = 0; a < 3103; a++) { // Reads word by word
                String word = scanner.nextLine().toUpperCase();
                if (word.equals(input)) {
                    validation = true;
                    char[] answerArray = answer.toCharArray();
                    char[] wordArray = word.toCharArray();
                    // get all of GREEN
                    for (int i = 0; i < 5; i++) {
                        if (answerArray[i] == wordArray[i]) {
                            state[i] = 'G';
                            answerArray[i] = '0';
                        }
                    }
                    // get all of YELLOW
                    for (int i = 0; i < 5; i++) {
                        if (answerArray[i] != '0') {
                            for (int j = 0; j < 5; j++) {
                                if (answerArray[j] == wordArray[i]) {
                                    state[i] = 'Y';
                                    answerArray[j] = '0';
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return new String(state);
    }
}
