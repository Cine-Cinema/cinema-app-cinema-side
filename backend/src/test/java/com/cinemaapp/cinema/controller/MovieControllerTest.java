package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.dto.MovieResponse;
import com.cinemaapp.cinema.exception.ResourceNotFoundException;
import com.cinemaapp.cinema.service.MovieService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @Test
    void getAllMovies_shouldReturnMovieList() throws Exception {

        MovieResponse movie = new MovieResponse(
                1L,
                "Interstellar",
                "A team travels through space.",
                169
        );

        given(movieService.getAllMovies())
                .willReturn(List.of(movie));

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Interstellar"))
                .andExpect(jsonPath("$[0].durationMinutes").value(169));
    }

    @Test
    void getMovieById_whenMovieDoesNotExist_shouldReturn404() throws Exception {

        given(movieService.getMovieById(999L))
                .willThrow(new ResourceNotFoundException(
                        "Movie not found with id: 999"
                ));

        mockMvc.perform(get("/api/movies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Movie not found with id: 999"));
    }

    @Test
    void createMovie_withInvalidData_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "description": "Testing validation",
                                    "durationMinutes": -50
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Movie title is required"))
                .andExpect(jsonPath("$.durationMinutes")
                        .value("Movie duration must be greater than zero"));
    }
}