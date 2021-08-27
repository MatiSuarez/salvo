package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.Clases.*;
import com.codeoftheweb.salvo.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository,
									  GameRepository gameRepository,
									  GamePlayerRepository gamePlayerRepository,
									  ShipRepository shipRepository,
									  SalvoRepository salvoRepository,
									  ScoreRepository scoreRepository) {
		return (args) -> {

			Player player1 = new Player ("j.bauer@ctu.gov", "24");
			Player player2 = new Player ("c.obrian@ctu.gov", "42");
			Player player3 = new Player ("t.almeida@ctu.gov","mole");
			Player player4 = new Player ("kim_bauer@gmail.com","kb");
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

			Salvo salvo1 = new Salvo(1, gameplayer1, Arrays.asList("E2"));
			salvoRepository.save(salvo1);
			Salvo salvo2 = new Salvo(1, gameplayer2, Arrays.asList("G5"));
			salvoRepository.save(salvo2);
			Salvo salvo3 = new Salvo(2, gameplayer1, Arrays.asList("F1", "F2"));
			salvoRepository.save(salvo3);


			Score score1 = new Score(LocalDateTime.now(), 1, player1, game1);
			scoreRepository.save(score1);
			Score score2 = new Score(LocalDateTime.now(), 0, player2, game1);
			scoreRepository.save(score2);
			Score score3 = new Score(LocalDateTime.now(), 0.5, player1, game2);
			scoreRepository.save(score3);
			Score score4 = new Score(LocalDateTime.now(), 0.5, player2, game2);
			scoreRepository.save(score4);
			Score score5 = new Score(LocalDateTime.now(), 1, player2, game3);
			scoreRepository.save(score5);
			Score score6 = new Score(LocalDateTime.now(), 0 , player3, game3);
			scoreRepository.save(score6);
			Score score7 = new Score(LocalDateTime.now(), 0.5, player1, game4);
			scoreRepository.save(score7);
			Score score8 = new Score(LocalDateTime.now(), 0.5 , player2, game4);
			scoreRepository.save(score8);
		};
	}

	//SEGURIDAD LOGIN/LOGOUT
	@Configuration
	class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

		@Autowired
		PlayerRepository playerRepository;

		@Override
		public void init(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(inputName -> {
				Player player = playerRepository.findByUserName(inputName);
				if (player != null) {
					return new User(player.getUserName(), player.getPassword(),
							AuthorityUtils.createAuthorityList("USER"));
				} else {
					throw new UsernameNotFoundException("Unknown player: " + inputName);
				}
			});
		}

		@Bean
		public PasswordEncoder passwordEncoder() {
			return PasswordEncoderFactories.createDelegatingPasswordEncoder();
		}
	}

	@EnableWebSecurity
	@Configuration
	class WebSecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests()
					.antMatchers("/api/login").hasAnyAuthority("PLAYER")
					.antMatchers("/api/logout").hasAnyAuthority("PLAYER")
					.antMatchers("/web/games.html").hasAnyAuthority("PLAYER")
					.and()
					.formLogin();
		}

	}
}

