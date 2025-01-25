package wordle.demo.rooms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("rooms", roomService.getAll());
        return "index";
    }
}
