package com.codeoftheweb.salvo;

import jdk.javadoc.doclet.Taglet;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String turnNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="salvoID")
    private GamePlayer gamePlayerID;

    @ElementCollection
    @Column(name="locations")
    private List<String> locations = new ArrayList<>();


    public Salvo (){}

    public Salvo(String turnNumber, GamePlayer gamePlayerID, List<String> locations) {
        this.turnNumber = turnNumber;
        this.gamePlayerID = gamePlayerID;
        this.locations = locations;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(String turnNumber) {
        this.turnNumber = turnNumber;
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
