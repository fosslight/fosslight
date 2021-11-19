/*
Copyright (c) 2021 Jongun Chae
Copyright (c) 2021 Sewon Park
SPDX-License-Identifier: AGPL-3.0-only
*/

package oss.fosslight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
public class ProjectIdentificationReviewConfirmIntegrationTest {
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36";

    @Autowired
    private MockMvc mockMvc;

    private String prjId;

    @BeforeEach
    void createProject() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/project/saveAjax")
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
                .header("user-agent", USER_AGENT)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Map<String, Map<String, String>> responseMapForProject = new ObjectMapper().readValue(response.getContentAsString(), Map.class);
        prjId = responseMapForProject.get("resultData").get("prjId");

        String content = "{\"prjId\":\"" + prjId + "\",\"identificationStatus\":\"REV\"}";
        mockMvc.perform(post("/project/updateProjectStatus")
                .content(content)
                .header("user-agent", USER_AGENT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Identification Review Confirm should be success When parameter is valid")
    void identificationReviewConfirmShouldBeSuccess() throws Exception {
        //given
        String content = "{\"prjId\":\"" + prjId + "\",\"identificationStatus\":\"CONF\"}";

        //when
        mockMvc.perform(post("/project/updateProjectStatus")
                .content(content)
                .header("user-agent", USER_AGENT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> assertReviewConfirm("CONF"));

        //then
    }

    private void assertReviewConfirm(String identificationStatus) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/project/edit/" + prjId)
                .header("user-agent", USER_AGENT))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    Project project = (Project) result.getModelAndView().getModel().get("project");
                    assertThat(project.getIdentificationStatus()).isEqualTo(identificationStatus);
                });
    }

    @Test
    @DisplayName("Identification Review Confirm should be failed When identificationStatus value is not given")
    void identificationReviewConfirmFailTest() throws Exception {
        //given
        String content = "{\"prjId\":\"" + prjId + "\"}";

        //when
        mockMvc.perform(post("/project/updateProjectStatus")
                .content(content)
                .header("user-agent", USER_AGENT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> assertReviewConfirm("REV"));

        //then
    }
}
