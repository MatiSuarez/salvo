package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository) {
		return (args) -> {

			Player player1 = new Player ("j.bauer@ctu.gov");
			Player player2 = new Player ("c.obrian@ctu.gov");
			Player player3 = new Player ("t.almeida@ctu.gov");
			Player player4 = new Player ("d.palmer@whitehouse.gov");
			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);


			Game game1 = new Game (LocalDateTime.now());
			Game game2 = new Game (LocalDateTime.now().plusHours(1));
			Game game3 = new Game (LocalDateTime.now().plusHours(2));
			Game game4 = new Game (LocalDateTime.now().plusHours(2));
			Game game5 = new Game (LocalDateTime.now().plusHours(2));
			Game game6 = new Game (LocalDateTime.now().plusHours(2));
			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);
			gameRepository.save(game4);
			gameRepository.save(game5);
			gameRepository.save(game6);

			GamePlayer gameplayer1 = new GamePlayer(LocalDateTime.now(), player1, game1);
			gamePlayerRepository.save(gameplayer1);
			GamePlayer gameplayer2 = new GamePlayer(LocalDateTime.now(), player2, game1);
			gamePlayerRepository.save(gameplayer2);

			GamePlayer gameplayer3 = new GamePlayer(LocalDateTime.now(), player2, game2);
			gamePlayerRepository.save(gameplayer3);
			GamePlayer gameplayer4 = new GamePlayer(LocalDateTime.now(), player2, game2);
			gamePlayerRepository.save(gameplayer4);

			GamePlayer gameplayer5 = new GamePlayer(LocalDateTime.now(), player3, game3);
			gamePlayerRepository.save(gameplayer5);
			GamePlayer gameplayer6 = new GamePlayer(LocalDateTime.now(), player1, game3);
			gamePlayerRepository.save(gameplayer6);

			GamePlayer gameplayer7 = new GamePlayer(LocalDateTime.now(), player1, game4);
			gamePlayerRepository.save(gameplayer7);
			GamePlayer gameplayer8 = new GamePlayer(LocalDateTime.now(), player2, game4);
			gamePlayerRepository.save(gameplayer8);

			GamePlayer gameplayer9 = new GamePlayer(LocalDateTime.now(), player3, game5);
			gamePlayerRepository.save(gameplayer9);
			GamePlayer gameplayer10 = new GamePlayer(LocalDateTime.now(), player1, game5);
			gamePlayerRepository.save(gameplayer10);

			GamePlayer gameplayer11 = new GamePlayer(LocalDateTime.now(), player4, game6);
			gamePlayerRepository.save(gameplayer11);


			Ship ship1 = new Ship ( "Patrol Boat", gameplayer1, Arrays.asList("B4", "B5"));
			shipRepository.save(ship1);

			Ship ship2 = new Ship ( "Destroyer", gameplayer1, Arrays.asList("H2", "H3", "H4"));
			shipRepository.save(ship2);

			Ship ship3 = new Ship ( "Submarine", gameplayer2, Arrays.asList("E1", "F1", "G1"));
			shipRepository.save(ship3);

		};
	}
}

