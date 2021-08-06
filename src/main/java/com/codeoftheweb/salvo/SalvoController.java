package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SalvoController {

        @Autowired
        private GameRepository gameRepository;

        @RequestMapping("/games")
        public List <Game> getAll() {
            return gameRepository.findAll();
    }

}
