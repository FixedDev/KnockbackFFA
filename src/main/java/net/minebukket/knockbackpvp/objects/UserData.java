package net.minebukket.knockbackpvp.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UserData {

    @Id
    private final UUID uniqueId;

    private int userDeaths = 0;
    private int userKills = 0;

    private int userCoins = 50;

    private UserData(){
        this.uniqueId = null;
    }
}
