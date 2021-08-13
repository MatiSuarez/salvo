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
    @Column(name="salvoID")
    private GamePlayer salvoID;

    @ElementCollection
    @Column(name="salvoLocations")
    private List<String> salvoLocations = new ArrayList<>();


    public Salvo (){}

    public Salvo(String turnNumber, GamePlayer salvoID, List<String> salvoLocations) {
        this.turnNumber = turnNumber;
        this.salvoID = salvoID;
        this.salvoLocations = salvoLocations;
    }

    public String getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(String turnNumber) {
        this.turnNumber = turnNumber;
    }

    public GamePlayer getSalvoID() {
        return salvoID;
    }

    public void setSalvoID(GamePlayer salvoID) {
        this.salvoID = salvoID;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }

    public void setSalvoLocations(List<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }
}
