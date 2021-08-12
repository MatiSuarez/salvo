package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private ShipRepository shipRepository;

        @RequestMapping("/games")
        public List <Object> getGames() {
            return gameRepository
                    .findAll().stream().map(game -> makeGameDTO(game))
                    .collect(Collectors.toList());
    }

    private Map<String, Object> makeGameDTO (Game game) {
            Map <String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers()
                .stream()
                .map(gamePlayer -> makeGamePlayerDTO(gamePlayer))
                .collect(Collectors.toList()));
        return dto;
        }

        /* @RequestMapping("/game_view/{nn}")
         public Map <String, Object> findGame(@PathVariable Long nn) {
            GamePlayer gamePlayer = gamePlayerRepository.getById(nn);
            return makeGameDTO(gamePlayer.getGameID());
    } */
        public Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer){
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", gamePlayer.getId());
            dto.put("player", gamePlayer.getPlayerID().makePlayerDTO());
            return dto;
        }

    public Map<String, Object> makeShipDTO(Ship ship){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", ship.getShipType());
        dto.put("locations", ship.getLocations());
        return dto;
    }


    private Map<String, Object> makeGameViewDTO (GamePlayer gamePlayer) {
        Map <String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getGameID().getId());
        dto.put("created", gamePlayer.getGameID().getCreationDate());
        dto.put("gamePlayers", gamePlayer.getGameID().getGamePlayers()
                .stream()
                .map(gp -> makeGamePlayerDTO(gp))
                .collect(Collectors.toList()));
        dto.put("ships", gamePlayer.getShips()
                        .stream()
                        .map(ship -> makeShipDTO(ship))
                        .collect(Collectors.toList()));
        return dto;
        }


        @RequestMapping("/game_view/{nn}")
        public Map <String, Object> findGame(@PathVariable Long nn) {
            GamePlayer gamePlayer = gamePlayerRepository.getById(nn);
            return makeGameViewDTO(gamePlayer);
        }

}
