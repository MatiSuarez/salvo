package com.codeoftheweb.salvo.Clases;

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

    private int turn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn (name = "salvoID")
    private GamePlayer salvoID;

    @ElementCollection
    @Column(name = "salvoLocations")
    private List<String> salvoLocations = new ArrayList<>();


    public Salvo() {
    }

    public Salvo(int turn, GamePlayer salvoID, List<String> salvoLocations) {
        this.turn = turn;
        this.salvoID = salvoID;
        this.salvoLocations = salvoLocations;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }

    public void setSalvoLocations(List<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }

    public GamePlayer getSalvoID() {
        return salvoID;
    }

    public void setSalvoID(GamePlayer salvoID) {
        this.salvoID = salvoID;
    }

    public List<String> getHits(){
        List<String> hits = new ArrayList<>();
        return hits;
    }
}





