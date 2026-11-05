package com.example.trial;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TrialApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void searchReturnsSeededUser() throws Exception {
        mockMvc.perform(get("/api/users/search").param("name", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].NAME").value("alice"));
    }

    @Test
    void helloPageEchoesName() throws Exception {
        mockMvc.perform(get("/api/page/hello").param("name", "world"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello world")));
    }

    @Test
    void welcomeMailEscapesHtml() throws Exception {
        mockMvc.perform(get("/api/page/welcome-mail")
                        .param("name", "<script>alert(1)</script>")
                        .param("email", "a@example.test"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void accessDeniedHandlerWritesFixedJson() throws Exception {
        mockMvc.perform(get("/api/users/search").param("name", "nobody"))
                .andExpect(status().isOk());
    }
}
