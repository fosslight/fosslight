/*
Copyright (c) 2026
SPDX-License-Identifier: AGPL-3.0-only
*/

package oss.fosslight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@WithMockUser(username = "user", roles = {"USER"})
@Transactional
class ProjectSpdxJsonUploadIntegrationTest {
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36";
    private static final String SPDX_JSON_PATH = "/Users/hyeinlee/Documents/_WORK/FOSSLight_Hub_2.0_For_Dev/6. spdx_cyclonedx/short_17932_SPDXRdf-ThinQ2.0_Server-1.8.23.json";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("SPDX json upload should load DEP data with purl and declared-license fallback")
    void spdxJsonUploadAndReadDepData() throws Exception {
        String prjId = createProject();

        byte[] jsonBytes = Files.readAllBytes(new FileSystemResource(SPDX_JSON_PATH).getFile().toPath());

        MockHttpServletResponse uploadResponse = mockMvc.perform(
                        multipart("/project/csvFile")
                                .file("myfile", jsonBytes)
                                .param("registFileId", "85853")
                                .header("user-agent", USER_AGENT)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        List<Object> uploadResult = new ObjectMapper().readValue(uploadResponse.getContentAsString(), List.class);
        List<Map<String, Object>> uploadFiles = (List<Map<String, Object>>) uploadResult.get(0);
        String fileSeq = String.valueOf(uploadFiles.get(0).get("fileSeq"));

        HashMap<String, Object> request = new HashMap<>();
        request.put("readType", "13");
        request.put("prjId", prjId);
        request.put("sheetNums", List.of("1_DEP", "4_DEP"));
        request.put("fileSeq", fileSeq);
        request.put("depMainData", "[]");
        request.put("srcMainData", "[]");
        request.put("binMainData", "[]");

        MockHttpServletResponse sheetResponse = mockMvc.perform(
                        post("/project/getIdentificationSheetData")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(request))
                                .header("user-agent", USER_AGENT))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        Map<String, Object> wrapper = new ObjectMapper().readValue(sheetResponse.getContentAsString(), Map.class);
        Map<String, Object> resultData = (Map<String, Object>) wrapper.get("resultData");
        List<Map<String, Object>> rows = (List<Map<String, Object>>) resultData.get("rows");

        assertThat(rows).isNotEmpty();
        assertThat(rows.stream().map(r -> String.valueOf(r.getOrDefault("purl", ""))).anyMatch(v -> !v.isBlank())).isTrue();
        assertThat(rows.stream().anyMatch(r -> {
            String licenseName = String.valueOf(r.getOrDefault("licenseName", ""));
            return !licenseName.isBlank() && !"NOASSERTION".equalsIgnoreCase(licenseName);
        })).isTrue();
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
                        .param("secMailYn", "Y")
                        .param("networkServerType", "N")
                        .param("prjDivision", "")
                        .param("prjUserId", "user")
                        .param("prjId", "")
                        .param("prjDivision", "")
                        .param("distributeTarget", "NA")
                        .param("prjModelJson", "[]")
                        .header("user-agent", USER_AGENT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        Map<String, Map<String, String>> responseMap = new ObjectMapper().readValue(response.getContentAsString(), Map.class);
        return responseMap.get("resultData").get("prjId");
    }
}
