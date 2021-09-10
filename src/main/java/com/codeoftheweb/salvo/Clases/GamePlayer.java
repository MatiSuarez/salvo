package com.codeoftheweb.salvo.Clases;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;


@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private LocalDateTime joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player playerID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game gameID;

    @OneToMany(mappedBy="gamePlayerID", fetch=FetchType.EAGER)
    Set<Ship> ships;

    @OneToMany(mappedBy="salvoID", fetch=FetchType.EAGER)
    Set<Salvo> salvoes;


    public GamePlayer () {}

    public GamePlayer(LocalDateTime joinDate, Player playerID, Game gameID) {
        this.joinDate = joinDate;
        this.playerID = playerID;
        this.gameID = gameID;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public Player getPlayerID() {
        return playerID;
    }

    public void setPlayerID(Player playerID) {
        this.playerID = playerID;
    }

    public Game getGameID() {
        return gameID;
    }

    public void setGameID(Game gameID) {
        this.gameID = gameID;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }


    public Set<Salvo> getSalvoes() {
        return salvoes;
    }

    public void setSalvoes(Set<Salvo> salvoes) {
        this.salvoes = salvoes;
    }

    public Optional<GamePlayer> getOpponent() {
        return this.getGameID().getGamePlayers().stream().filter(gp -> gp.getId() != this.getId()).findFirst();
    }

    ////OTRA FORMA DE RESOLVER SCORE
    /*public  Score getScore(){
        return playerID.getScore(this.gameID);
    }*/
}
