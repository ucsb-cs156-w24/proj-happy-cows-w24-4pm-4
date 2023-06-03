package edu.ucsb.cs156.happiercows.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity(name = "user_commons")
public class UserCommons {
    @EmbeddedId
    @JsonIgnore
    private UserCommonsKey id;

    @MapsId("userId")
    @ManyToOne
    @JsonIgnore
    private User user;

    @MapsId("commonsId")
    @ManyToOne
    @JsonIgnore
    private Commons commons;

    private String username;

    private double totalWealth;

    private int numOfCows;

    private double cowHealth;

    @JsonInclude
    public long getUserId() {
        return user.getId();
    }

    @JsonInclude
    public long getCommonsId() {
        return commons.getId();
    }
}
