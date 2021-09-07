package com.codeoftheweb.salvo.Clases;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String type;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayerID")
    private GamePlayer gamePlayerID;

    @ElementCollection
    @Column(name="locations")
    private List<String> locations = new ArrayList<>();

    public Ship (){ }

    public Ship(String type, GamePlayer gamePlayerID, List<String> locations) {
        this.type = type;
        this.gamePlayerID = gamePlayerID;
        this.locations = locations;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GamePlayer getGamePlayerID() {
        return gamePlayerID;
    }

    public void setGamePlayerID(GamePlayer gamePlayerID) {
        this.gamePlayerID = gamePlayerID;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
}
