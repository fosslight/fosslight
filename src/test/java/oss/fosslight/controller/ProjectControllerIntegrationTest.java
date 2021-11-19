/*
Copyright (c) 2021 Jongun Chae
Copyright (c) 2021 Mingu Kang
SPDX-License-Identifier: AGPL-3.0-only
*/

package oss.fosslight.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.io.UnsupportedEncodingException;
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

    @Test
    @DisplayName("starting review the package should be success When parameter is valid")
    void startingReviewThePackageShouldBeSuccess() throws Exception {
        // given
        String prjId = createProject();
        Project project = requestReviewThePackage(prjId);

        project.setIdentificationStatus("REV");
        project.setLoginUserName("admin");
        project.setLoginUserRole("ROLE_ADMIN");

        // when
        mockMvc.perform(post("/project/updateProjectStatus")
                .content(new ObjectMapper().writeValueAsString(project))
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> assertReviewStarted(result.getResponse()));

        // then
    }

    @Test
    @DisplayName("starting review the package should be failed When prjId is empty")
    void failTestWhenStartingReview() throws Exception {
        // given
        String prjId = createProject();
        Project project = requestReviewThePackage(prjId);

        project.setPrjId("");
        project.setIdentificationStatus("REV");
        project.setLoginUserName("admin");
        project.setLoginUserRole("ROLE_ADMIN");

        // when
        mockMvc.perform(post("/project/updateProjectStatus")
                .content(new ObjectMapper().writeValueAsString(project))
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andDo(result -> {
                    Map<String, String> responseMap = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
                    assertThat(String.valueOf(responseMap.get("success"))).isEqualTo("false");
                });

        // then
    }

    private String createProject() throws Exception {
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
                .param("prjUserId", "tester")
                .param("prjId", "")
                .param("prjDivision", "")
                .param("distributeTarget", "NA")
                .param("prjModelJson", "[]")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Map<String, Map<String, String>> responses = new ObjectMapper().readValue(response.getContentAsString(), Map.class);
        return responses.get("resultData").get("prjId");
    }

    private Project requestReviewThePackage(String prjId) throws Exception {
        Project project = new Project();

        project.setPrjId(prjId);
        project.setIdentificationStatus("REQ");
        project.setCopyFlag("N");
        project.setDistributeDeployModelYn("N");
        project.setExcelDownloadFlag("N");
        project.setNetworkServerFlag("N");
        project.setLoadFromAndroidProjectFlag("N");
        project.setVersionMatchedFlag("N");
        project.setResetDistributionStatus("N");
        project.setSkipPackageFlag("N");
        project.setUserComment("review start");
        project.setReProcessDistributionFlag("N");
        project.setChangedNoticeYn("N");
        project.setModelListAppendFlag("N");
        project.setLoginUserName("tester");
        project.setLoginUserRole("ROLE_USER");
        project.setHotYn("N");

        mockMvc.perform(post("/project/updateProjectStatus")
                .content(new ObjectMapper().writeValueAsString(project))
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        return project;
    }

    private void assertReviewStarted(MockHttpServletResponse response) throws UnsupportedEncodingException, JsonProcessingException {
        Map<String, String> responseMap = new ObjectMapper().readValue(response.getContentAsString(), Map.class);
        assertThat(responseMap.get("isValid")).isEqualTo("true");
    }
}