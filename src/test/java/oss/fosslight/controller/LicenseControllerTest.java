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
class LicenseControllerTest {
    @Autowired
    MockMvc mockMvc;
    @DisplayName("saveAjax Test")
    @Test
    public void saveAjax() throws Exception {
        String licenseMasterJson = "{ \"licenseId\": \"testId\", \"licenseName\": \"testName\" }";

        mockMvc.perform(post("/license/saveAjax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(licenseMasterJson))
                .andExpect(status().isOk());
    }

    @DisplayName("deleteComment Test")
    @Test
    void deleteComment() {

    }
}
