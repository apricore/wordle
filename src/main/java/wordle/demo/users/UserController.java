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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserController {

    UserService userService;
    RoomService roomService;

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
                serverMessageCollection.setAnswer(room.getAnswer());
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

            String inputWord = clientMessage.getInputWord().toUpperCase();
            if (RoomController.validateWord(inputWord)) {
                serverMessageCollection.setCode(Events.SUCCEED);
                if (inputWord.equals(room.getAnswer())) {
                    // Add '.' at the end of user state to indicate that the user has finished.
                    user.setState(user.getState() + inputWord + ".");
                } else {
                    user.setState(user.getState() + inputWord);
                }
                userService.save(user);
            } else {
                serverMessageCollection.setCode(Events.FAILED);
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
            userService.findById(clientMessage.getUserId()).ifPresent(user -> {
                userService.delete(user);
                serverMessageCollection.setUser(user);
            });
            serverMessageCollection.setUsers(userService.findAllByRoom_Id(serverMessageCollection.getUser().getRoomId()));
            serverMessageCollection.setCode(Events.SUCCEED);
        }else {
            serverMessageCollection.setCode(Events.NOT_FOUND);
        }
        return serverMessageCollection;
    }

    public ServerMessageCollection giveUp(ClientMessage clientMessage) throws IOException {
        ServerMessageCollection serverMessageCollection = new ServerMessageCollection();
        if (userService.findById(clientMessage.getUserId()).isPresent()) {
            User user = userService.findById(clientMessage.getUserId()).get();
            Room room = roomService.getById(user.getRoomId()).get();
            serverMessageCollection.setAnswer(room.getAnswer());
            // Add '.' at the end of user state to indicate the user's finishing state.
            user.setState(user.getState() + ".");
            userService.save(user);
            serverMessageCollection.setCode(Events.SUCCEED);
            serverMessageCollection.setUser(user);
            serverMessageCollection.getUsers().addAll(userService.findAllByRoom_Id(user.getRoomId()));
        } else {
            serverMessageCollection.setCode(Events.NOT_FOUND);
        }
        return serverMessageCollection;
    }

    public ServerMessageCollection resetWord(ClientMessage clientMessage) throws IOException {
        ServerMessageCollection serverMessageCollection = new ServerMessageCollection();
        if (!userService.findById(clientMessage.getUserId()).isPresent()) {
            serverMessageCollection.setCode(Events.NOT_FOUND);
            return serverMessageCollection;
        }

        User user = userService.findById(clientMessage.getUserId()).get();
        Room room = roomService.getById(user.getRoomId()).get();
        List<User> users = new ArrayList<>(userService.findAllByRoom_Id(user.getRoomId()));
        boolean allFinished = true;
        if (!users.isEmpty()) for (User user1 : users) {
            String state = user1.getState();
            if (state.length() < 30 && !state.contains(".")) {
                allFinished = false;
                break;
            }
        }
        serverMessageCollection.setUser(user);
        if (!allFinished) {
            serverMessageCollection.setCode(Events.FAILED);
            return serverMessageCollection;
        }

        String inputWord = clientMessage.getInputWord();
        if (inputWord != null) {
            inputWord = inputWord.toUpperCase();
            if (!RoomController.validateWord(inputWord)) {
                serverMessageCollection.setCode(Events.NOT_FOUND);
                return serverMessageCollection;
            }
            serverMessageCollection.setCode(Events.SUCCEED);
            serverMessageCollection.setAnswer(inputWord);
            serverMessageCollection.getUsers().addAll(users);
            room.setAnswer(inputWord);
            roomService.save(room);
            for (User u : users) {
                u.setState("");
                userService.save(u);
            }
            // Setting '_' in user state to indicate that the user is setting the word for others to play.
            // '.' at the end means the user is at a finished state.
            user.setState("_.");
            userService.save(user);
            return serverMessageCollection;
        }
        inputWord = RoomController.randomAnswerChooser();
        serverMessageCollection.setCode(Events.SUCCEED);
        serverMessageCollection.setAnswer(inputWord);
        serverMessageCollection.getUsers().addAll(users);
        room.setAnswer(inputWord);
        roomService.save(room);
        for (User u : users) {
            u.setState("");
            userService.save(u);
        }
        return serverMessageCollection;
    }
}
