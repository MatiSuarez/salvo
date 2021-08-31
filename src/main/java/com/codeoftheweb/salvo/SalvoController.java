package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.Clases.*;
import com.codeoftheweb.salvo.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping("/games")
    public Map<String, Object> game(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();

        if(!isGuest(authentication)){
            dto.put("player", playerRepository.findByUserName(authentication.getName()).makePlayerDTO());}
        else { dto.put("player","Guest");}

        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> makeGameDTO(game))
                .collect(Collectors.toList()));
        return dto;
    }


    public Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", gamePlayer.getPlayerID().makePlayerDTO());
        return dto;
    }

    public Map<String, Object> makeScoreDTO(Score score){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("player", score.getPlayerID().getId());
        dto.put("score", score.getScore());
        dto.put("finishDate", score.getFinishDate());
        return dto;
    }

    public Map<String, Object> makeGameDTO (Game game) {
        Map <String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers()
                .stream()
                .map(gamePlayer -> makeGamePlayerDTO(gamePlayer))
                .collect(Collectors.toList()));
        dto.put("scores", game.getScores()
                .stream()
                .map(s -> makeScoreDTO(s))
                .collect(Collectors.toList()));

        //OTRA FORMA DE RESOLVER SCORE
        /*dto.put("scores", game.getGamePlayers()
                .stream()
                .map( sc -> {
                    if(sc.getScore() != null){
                        return makeScoreDTO(sc.getScore());
                    }
                    else {
                        return null;
                    }
                })
        );*/

        return dto;
    }

    public Map<String, Object> makeShipDTO(Ship ship){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", ship.getShipType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    public Map<String, Object> makeSalvoDTO(Salvo salvo){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getSalvoID().getPlayerID().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }



    private Map<String, Object> makeGameViewDTO (GamePlayer gamePlayer, Salvo salvo) {
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
        dto.put("salvoes", gamePlayer.getGameID().getGamePlayers()
                .stream()
                .flatMap(slv -> slv.getSalvoes()
                        .stream().map(slv1 -> makeSalvoDTO(slv1))).collect(Collectors.toList()));

        // UTILIZANDO 'FOR' PARA LOS SALVO
        /*List <Map <String, Object>> listAux= new ArrayList<>();
                for(GamePlayer gp:gamePlayer.getGameID().getGamePlayers()){
                    for(Salvo s:gamePlayer.getSalvoes()){
                        listAux.add(this.makeSalvoDTO(s));
                    }
                }
                dto.put("salvoes", listAux); */
        return dto;
    }


    @RequestMapping("/game_view/{nn}")
    public Map <String, Object> findGame(@PathVariable Long nn) {
        GamePlayer gamePlayer = gamePlayerRepository.getById(nn);
        Salvo salvo = salvoRepository.getById(nn);
        return makeGameViewDTO(gamePlayer, salvo);
    }


    //LOGIN
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
                                             @RequestParam (value="email") String userName,
                                             @RequestParam (value="password") String password ){

        if (userName.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Faltan completar datos", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(userName) != null) {
            return new ResponseEntity<>("Usuario existente", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(userName, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
}


