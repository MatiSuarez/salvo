package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.Clases.*;
import com.codeoftheweb.salvo.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Object> register(@RequestParam(value = "email") String userName, @RequestParam(value = "password") String password) {

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

                        //SI NO SE CUMPLE LO ANTERIOR

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
    @PostMapping(path = "/games/players/{gamePlayerId}/ships")
    public ResponseEntity<Map<String, Object>> createShips(@PathVariable Long gamePlayerId, @RequestBody List<Ship> ships, Authentication authentication) {
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);

        if (!isGuest(authentication)) {
            if (gamePlayer.isPresent()) {
                Player currentPlayer = playerRepository.findByUserName(authentication.getName());
                if (currentPlayer.getId() == gamePlayer.get().getPlayerID().getId()) {
                    if (gamePlayer.get().getShips().size() == 0) {
                        if (ships.size() == 5) {
                            for (Ship newShip : ships) {
                                shipRepository.save(new Ship(newShip.getType(), gamePlayer.get(), newShip.getShipLocations()));

                            }
                            return new ResponseEntity<>(makeMap("gpid", gamePlayer.get().getId()), HttpStatus.CREATED);

                            //SI NO SE CUMPLE LO ANTERIOR
                        } else {
                            return new ResponseEntity<>(makeMap("Error", "Los 5 barcos no fueron creados!"), HttpStatus.FORBIDDEN);
                        }

                    } else {
                        return new ResponseEntity<>(makeMap("Error", "Los 5 barcos ya fueron creados!"), HttpStatus.FORBIDDEN);
                    }

                } else {
                    return new ResponseEntity<>(makeMap("Error", "El GamePlayer no corresponde a esta partida!"), HttpStatus.UNAUTHORIZED);
                }

            } else {
                return new ResponseEntity<>(makeMap("Error", "El GamePlayer no existe"), HttpStatus.NOT_FOUND);
            }

        } else {
            return new ResponseEntity<>(makeMap("Error", "Tenes iniciar sesión!"), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/games/players/{gameplayerid}/ships")
    public ResponseEntity<Map> getShips(@PathVariable Long gameplayerid, Authentication authentication) {

        if (gamePlayerRepository.findById(gameplayerid).isPresent()) {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("ship", gamePlayerRepository.findById(gameplayerid).get().getShips().stream().map(this::makeShipDTO).collect(Collectors.toList()));
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        }

        return new ResponseEntity<>(makeMap("Error", "Gameplayer inexistente"), HttpStatus.FORBIDDEN);
    }


    //UBICACION SALVOS
    @PostMapping(path = "/games/players/{gamePlayerId}/salvoes")
    public ResponseEntity<Map<String, Object>> placeSalvoes(@PathVariable Long gamePlayerId, @RequestBody Salvo salvo, Authentication authentication) {
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);

        if (!isGuest(authentication)) {
            if (gamePlayer.isPresent()) {
                Player currentPlayer = playerRepository.findByUserName(authentication.getName());
                if (currentPlayer.getId() == gamePlayer.get().getPlayerID().getId()) {
                    if (gamePlayer.get().getSalvoes().size() + 1 == salvo.getTurn()) {
                        Optional<GamePlayer> rival = gamePlayer.get().getOpponent();
                        if (rival.isPresent()) {

                            Salvo currentSalvo = new Salvo(salvo.getTurn(), gamePlayer.get(), salvo.getSalvoLocations());
                            salvoRepository.save(currentSalvo);
                        return new ResponseEntity<>(makeMap("Turno", salvo.getTurn()), HttpStatus.CREATED);
                    } else {
                            return new ResponseEntity<>(makeMap("Error", "Todavia no es tu turno!"), HttpStatus.FORBIDDEN);
                        }
                    } else {

                        //SI NO HAY RIVAL
                        if (gamePlayer.get().getSalvoes().size() == 0) {
                            salvoRepository.save(new Salvo(salvo.getTurn(), gamePlayer.get(), salvo.getSalvoLocations()));
                            return new ResponseEntity<>(makeMap("Turno", salvo.getTurn()), HttpStatus.CREATED);
                        } else {
                            return new ResponseEntity<>(makeMap("Error", "Tenes que espera ra un rival!"), HttpStatus.FORBIDDEN);
                        }
                    }
                } else {

                    //SI NO SE CUMPLE LO ANTERIOR
                    return new ResponseEntity<>(makeMap("Error", "Turno repetido!"), HttpStatus.FORBIDDEN);
                }
            } else {
                return new ResponseEntity<>(makeMap("Error", "Gameplayer incorrecto"), HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(makeMap("Error", "Tenes que iniciar sesión!"), HttpStatus.UNAUTHORIZED);
        }
    }


    @GetMapping("/games/players/{gameplayerid}/salvoes")
    public ResponseEntity<Map> getSalvoes(@PathVariable Long gameplayerid, Authentication authentication) {

        if (gamePlayerRepository.findById(gameplayerid).isPresent()) {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("salvo", gamePlayerRepository.findById(gameplayerid).get().getSalvoes().stream().map(this::makeSalvoDTO).collect(Collectors.toList()));
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        }

        return new ResponseEntity<>(makeMap("Error", "Gameplayer inexistente"), HttpStatus.FORBIDDEN);
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
        dto.put("type", ship.getType());
        dto.put("locations", ship.getShipLocations());
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
}


