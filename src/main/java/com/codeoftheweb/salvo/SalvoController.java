package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.Clases.*;
import com.codeoftheweb.salvo.Repositories.*;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.*;
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

        if (!isGuest(authentication)) {
            dto.put("player", playerRepository.findByUserName(authentication.getName()).makePlayerDTO());
        } else {
            dto.put("player", "Guest");
        }

        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> makeGameDTO(game))
                .collect(Collectors.toList()));
        return dto;
    }

    @RequestMapping("/game_view/{nn}")
    public ResponseEntity<Map<String, Object>> findGame(@PathVariable Long nn, Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.getById(nn);

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("Error", "No has iniciado sesión"), HttpStatus.UNAUTHORIZED);
        }

        if (playerRepository.findByUserName(authentication.getName()).getGamePlayers()
                .stream().anyMatch(gamePlayer1 -> gamePlayer1.getId() == nn)) {
            return new ResponseEntity<>(makeGameViewDTO(gamePlayer), HttpStatus.ACCEPTED);

        } else {
            return new ResponseEntity<>(makeMap("Error", "Player incorrecto, no hagas trampa!"), HttpStatus.UNAUTHORIZED);
        }
    }


    //CREAR NUEVO JUGADOR
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam(value = "email") String userName,
            @RequestParam(value = "password") String password) {

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

    //CREAR JUEGO
    @PostMapping("/games")
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {

        if (!isGuest(authentication)) {
            Game newGame = new Game(LocalDateTime.now());
            gameRepository.save(newGame);

            Player auth = playerRepository.findByUserName(authentication.getName());

            GamePlayer gamePlayer = new GamePlayer(LocalDateTime.now(), auth, newGame);
            gamePlayerRepository.save(gamePlayer);
            return new ResponseEntity<>(makeMap("gpid", gamePlayer.getId()), HttpStatus.CREATED);

        } else {
            return new ResponseEntity<>(makeMap("Error", "Sin accso, debes iniciar sesión!"), HttpStatus.UNAUTHORIZED);
        }
    }


    //UNIRSE A UN JUEGO
    @PostMapping("/game/{nn}/players")
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long nn, Authentication authentication) {

        if (!isGuest(authentication)) {
            Optional<Game> game = gameRepository.findById(nn);
            Player auth = playerRepository.findByUserName(authentication.getName());

            if (game.isPresent()) {
                if (game.get().getGamePlayers().size() < 2) {
                    if (game.get()
                            .getGamePlayers()
                            .stream()
                            .anyMatch(jg -> jg.getPlayerID().getId() != auth.getId())) {

                        GamePlayer gamePlayer = new GamePlayer(LocalDateTime.now(), auth, game.get());
                        gamePlayerRepository.save(gamePlayer);
                        return new ResponseEntity<>(makeMap("gpid", gamePlayer.getId()), HttpStatus.CREATED);

                    } else {

                        return new ResponseEntity<>(makeMap("Error", "No se puede unir a un mismo juego dos veces!"), HttpStatus.FORBIDDEN);
                    }

                } else {

                    return new ResponseEntity<>(makeMap("Error", "El juego esta completo!"), HttpStatus.FORBIDDEN);
                }

            } else {

                return new ResponseEntity<>(makeMap("Error", "El juego no existe!"), HttpStatus.NOT_FOUND);
            }

        } else {

            return new ResponseEntity<>(makeMap("Error", "Registrate para poder unirte al juego!"), HttpStatus.UNAUTHORIZED);
        }
    }



    //UBICACION BARCOS
    @PostMapping (path= "/games/players/{gamePlayerId}/ships")
        public ResponseEntity<Map<String, Object>> shipsLocations(@PathVariable Long gamePlayerId,
                                                                  @RequestBody List<Ship> ships,
                                                                  Authentication authentication) {

        if (!isGuest(authentication)) {
            Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);

            if (gamePlayer.isPresent()) {
                Player authPlayer = playerRepository.findByUserName(authentication.getName());

                if (authPlayer.getId() == gamePlayer.get().getPlayerID().getId()) {
                    if (gamePlayer.get().getShips().size() == 0) {
                        if (ships.size() == 5) {
                            for (Ship ship : ships) {
                                shipRepository.save(new Ship(ship.getShipType(), gamePlayer.get(), ship.getLocations()));
                            }
                            return new ResponseEntity<>(makeMap("gpid", gamePlayer.get().getId()), HttpStatus.CREATED);
                        } else {
                            return new ResponseEntity<>(makeMap("Error", "Los 5 barcos no fueron creados!"), HttpStatus.BAD_REQUEST);
                        }
                    } else {
                        return new ResponseEntity<>(makeMap("Error", "Los 5 barcos ya fueron creados!"), HttpStatus.BAD_REQUEST);
                        }
                   } else {
                        return new ResponseEntity<>(makeMap("Error", "El GamePlayer no corresponde a esta partida!"), HttpStatus.FORBIDDEN);
                        }
                } else {
                return new ResponseEntity<>(makeMap("Error", "El GamePlayer no existe"), HttpStatus.NOT_FOUND);
                        }
            } else {
            return new ResponseEntity<>(makeMap("Error", "Debes iniciar sesión!"), HttpStatus.UNAUTHORIZED);
                    }
    }



    //DTOs
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



    private Map<String, Object> makeGameViewDTO (GamePlayer gamePlayer) {
        Map <String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getGameID().getId());
        dto.put("created", gamePlayer.getGameID().getCreationDate());
        dto.put("gameState", "PLACESHIPS");
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
        dto.put("hits", makeHitsDTO());

        // UTILIZANDO 'FOR' PARA SALVO
        /*List <Map <String, Object>> listAux= new ArrayList<>();
                for(GamePlayer gp:gamePlayer.getGameID().getGamePlayers()){
                    for(Salvo s:gamePlayer.getSalvoes()){
                        listAux.add(this.makeSalvoDTO(s));
                    }
                }
                dto.put("salvoes", listAux); */
        return dto;
    }

    private Map<String, Object> makeMap(String key, Object value){
        Map<String, Object> dto = new HashMap<>();
        dto.put(key, value);
        return dto;
    }

    public Map<String, Object> makeHitsDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        List<String>  lst = new ArrayList<>();
        dto.put("self", lst);
        dto.put("opponent", lst);
        return dto;
    }


    //FIN DE LOS DTOS

}


