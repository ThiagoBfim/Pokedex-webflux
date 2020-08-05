package com.webflux.pokedex.config;

import com.webflux.pokedex.domain.Pokemon;
import com.webflux.pokedex.repository.PokemonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;

@Configuration
public class BeanFactory {

    @Bean
    CommandLineRunner init(ReactiveMongoOperations operations, PokemonRepository repository) {
        return args -> Flux.just(
                new Pokemon(null, "Bulbassauro", "Semente", "OverGrow", 6.09),
                new Pokemon(null, "Charizard", "Fogo", "Blaze", 90.05),
                new Pokemon(null, "Caterpie", "Minhoca", "Poeira do Escudo", 2.09),
                new Pokemon(null, "Blastoise", "Marisco", "Torrente", 6.09))
                .flatMap(repository::save)
                .thenMany(repository.findAll())
                .subscribe(System.out::println);
    }
}
