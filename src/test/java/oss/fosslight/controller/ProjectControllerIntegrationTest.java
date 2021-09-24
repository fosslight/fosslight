package oss.fosslight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import oss.fosslight.domain.Project;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@Transactional
class ProjectControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Registration should be success When parameter is valid")
    void registrationShouldBeSuccess() throws Exception {
        // given

        // when
        mockMvc.perform(post("/project/saveAjax")
                        .param("copy", "false")
                        .param("prjName", UUID.randomUUID().toString())
                        .param("prjVersion", "v1")
                        .param("noticeType", "10")
                        .param("distributionType", "10")
                        .param("comment", "<p>test</p>")
                        .param("osType", "100")
                        .param("priority", "30")
                        .param("statusRequestYn", "")
                        .param("listId", "")
                        .param("publicYn", "Y")
                        .param("networkServerType", "N")
                        .param("prjDivision", "999")
                        .param("prjUserId", "test2")
                        .param("prjId", "")
                        .param("prjDivision", "")
                        .param("distributeTarget", "NA")
                        .param("prjModelJson", "[]")
                        .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> assertProjectAdded(result.getResponse()));

        // then
    }

    private void assertProjectAdded(MockHttpServletResponse response) throws Exception {
        Map<String, Map<String, String>> responseMap = new ObjectMapper().readValue(response.getContentAsString(), Map.class);
        String prjId = responseMap.get("resultData").get("prjId");
        mockMvc.perform(MockMvcRequestBuilders.get("/project/edit/" + prjId)
                        .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                )
                .andExpect(status().isOk())
                .andExpect(result -> {
                    Project project = (Project) result.getModelAndView().getModel().get("project");
                    assertThat(project.getPrjId()).isEqualTo(prjId);
                });
    }

    @Test
    @DisplayName("Adding project should be failed when prjName is empty")
    void addProjectFailTest() throws Exception {
        // given

        // when
        mockMvc.perform(post("/project/saveAjax")
                        .param("copy", "false")
                        .param("prjVersion", "v1")
                        .param("noticeType", "10")
                        .param("distributionType", "10")
                        .param("comment", "<p>test</p>")
                        .param("osType", "100")
                        .param("priority", "30")
                        .param("statusRequestYn", "")
                        .param("listId", "")
                        .param("publicYn", "Y")
                        .param("networkServerType", "N")
                        .param("prjDivision", "999")
                        .param("prjUserId", "test2")
                        .param("prjId", "")
                        .param("prjDivision", "")
                        .param("distributeTarget", "NA")
                        .param("prjModelJson", "[]")
                        .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Map<String, String> responseMap = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
                    assertThat(responseMap.get("isValid")).isEqualTo("false");
                });

        // then
    }
}