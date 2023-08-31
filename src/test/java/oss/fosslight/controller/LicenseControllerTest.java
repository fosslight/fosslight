package oss.fosslight.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@Transactional
public class LicenseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @DisplayName("getLicenseId Test")
    @Test
    public void getLicenseId() throws Exception {
        String licenseName = "MIT No Attribution";

        mockMvc.perform(post("/license/getLicenseId")
                        .param("licenseName", licenseName))
                .andExpect(status().isOk());
    }

    @DisplayName("autoCompleteAjax")
    @Test
    public void autoCompleteAjax() throws Exception {

    }
}
