package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
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
        dto.put ("gamePlayers", game.getGamePlayers().stream().map(gamePlayer -> gamePlayer.makeGamePlayerDTO()).collect(Collectors.toList()) );
        return dto;
        }
}
