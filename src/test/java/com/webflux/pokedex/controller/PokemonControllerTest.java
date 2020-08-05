package com.webflux.pokedex.controller;

import com.webflux.pokedex.domain.Pokemon;
import com.webflux.pokedex.domain.PokemonEvent;
import com.webflux.pokedex.repository.PokemonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PokemonController.class)
public class PokemonControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    PokemonRepository repository;

    @Test
    public void testGetAllPokemons() {
        Pokemon pokemon = new PokemonBuilder().setId("123").setNome("Chalizard").setHabilidades("Fogo").setPeso(100.85).setCategoria("Fogo").build();
        Flux<Pokemon> pokemonFlux = Flux.fromIterable(Collections.singletonList(pokemon));

        when(repository.findAll()).thenReturn(pokemonFlux);

        webTestClient.get()
                .uri("/pokemons")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Pokemon.class)
                .contains(pokemon);
    }

    @Test
    void getPokemon() {
        Pokemon pokemon = new PokemonBuilder().setId("123").setNome("Chalizard").setHabilidades("Fogo").setPeso(100.85).setCategoria("Fogo").build();

        when(repository.findById("123")).thenReturn(Mono.just(pokemon));

        webTestClient.get()
                .uri("/pokemons/123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Pokemon.class)
                .value(Pokemon::getId, equalTo("123"))
                .value(Pokemon::getNome, equalTo("Chalizard"));
    }

    @Test
    void savePokemon() {
        Pokemon pokemon = new PokemonBuilder().setId("123").setNome("Chalizard").setHabilidades("Fogo").setPeso(100.85).setCategoria("Fogo").build();

        when(repository.save(pokemon)).thenReturn(Mono.just(pokemon));

        webTestClient.post()
                .uri("/pokemons")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(pokemon))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Pokemon.class)
                .value(Pokemon::getId, equalTo("123"))
                .value(Pokemon::getHabilidades, equalTo("Fogo"));

        Mockito.verify(repository).save(pokemon);
    }

    @Test
    void updatePokemon() {
        Pokemon pokemonBefore = new PokemonBuilder().setId("123").setNome("Chalizard").setHabilidades("Fogo").setPeso(100.85).setCategoria("Fogo").build();
        Pokemon pokemonUpdated = new PokemonBuilder().setId("123").setNome("Chalizard").setHabilidades("Lança Chamas").setPeso(100.85).setCategoria("Fogo").build();

        when(repository.findById("123")).thenReturn(Mono.just(pokemonBefore));
        when(repository.save(pokemonUpdated)).thenReturn(Mono.just(pokemonUpdated));

        webTestClient.put()
                .uri("/pokemons/{id}", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(pokemonUpdated))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Pokemon.class)
                .value(Pokemon::getId, equalTo("123"))
                .value(Pokemon::getHabilidades, equalTo("Lança Chamas"));

        Mockito.verify(repository).save(pokemonUpdated);
    }

    @Test
    void deletePokemon() {

        when(repository.deleteById("123")).thenReturn(Mono.empty());

        webTestClient.delete().uri("/pokemons/{id}", "123")
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(repository).deleteById("123");
    }


    @Test
    void getPokemonEvents() {

        FluxExchangeResult<PokemonEvent> result = webTestClient.get().uri("/pokemons/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(PokemonEvent.class);


        Flux<PokemonEvent> interval = result.getResponseBody();

        StepVerifier.create(interval)
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(1))
                .expectNextCount(0)
                .expectNext(new PokemonEvent(0L, "Product Event"))
                .thenAwait(Duration.ofSeconds(1))
                .expectNextCount(1)
                .expectNext(new PokemonEvent(2L, "Product Event"))
                .thenAwait(Duration.ofSeconds(1))
                .expectNextCount(2).expectComplete();


//        webTestClient.get().uri("/pokemons/events")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(PokemonEvent.class)
//                .contains(new PokemonEvent(5L, "Product Event"));
    }


    static class PokemonBuilder {
        private String id;
        private String nome;
        private String categoria;
        private String habilidades;
        private Double peso;

        public PokemonBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public PokemonBuilder setNome(String nome) {
            this.nome = nome;
            return this;
        }

        public PokemonBuilder setCategoria(String categoria) {
            this.categoria = categoria;
            return this;
        }

        public PokemonBuilder setHabilidades(String habilidades) {
            this.habilidades = habilidades;
            return this;
        }

        public PokemonBuilder setPeso(Double peso) {
            this.peso = peso;
            return this;
        }

        Pokemon build() {
            return new Pokemon(id, nome, categoria, habilidades, peso);
        }
    }
}
