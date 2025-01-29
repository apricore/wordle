package wordle.demo.users;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import wordle.demo.rooms.Room;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotEmpty
    private String username;

    private String state;

//    @NotNull
//    @ManyToOne
////    @JsonBackReference
//    @JoinColumn(name = "room_id", referencedColumnName = "id", nullable = false)
//    private Room room;

    private Long roomId;

}
