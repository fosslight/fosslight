package oss.fosslight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@Transactional
public class SelfCheckControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Adding Self-Check list should be success When parameter is valid")
    void registrationShouldBeSuccess() throws Exception {

        mockMvc.perform(post("/selfCheck/saveAjax")
                .param("prjId", "")
                .param("prjName", "")
                .param("prjVersion", "")
                .param("comment", "")
                .param("copyFlag", "N")
                .param("distributeDeployModelYn","N")
                .param("excelDownloadFlag","N")
                .param("prjModelJson","")
                .param("networkServerFlag","N")
                .param("loadFromAndroidProjectFlag","N")
                .param("creatorNm","")
                .param("versionMatchedFlag","N")
                .param("resetDistributionStatus","N")
                .param("deleteMemo","")
                .param("skipPackageFlag","N")
                .param("reProcessDistributionFlag","N")
                .param("changedNoticeYn","N")
                .param("modelListAppendFlag","N")
                .param("loginUserName","user")
                .param("loginUserRole","ROLE_ADMIN")
                .param("sortField","")
                .param("sortOrder","")
                .param("hotYn","N")
                .param("creator","")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Map<String, String> responseMap = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
                    assertThat(responseMap.get("isValid")).isEqualTo("true");
                });

    }

    @Test
    @DisplayName("Adding Self-Check list should be failed when prjName is empty")
    void registrationShouldBeFail() throws Exception {
        mockMvc.perform(post("/selfCheck/saveAjax")
                .param("prjId", "")
                .param("prjVersion", "")
                .param("comment", "")
                .param("copyFlag", "N")
                .param("distributeDeployModelYn","N")
                .param("excelDownloadFlag","N")
                .param("prjModelJson","")
                .param("networkServerFlag","N")
                .param("loadFromAndroidProjectFlag","N")
                .param("creatorNm","")
                .param("versionMatchedFlag","N")
                .param("resetDistributionStatus","N")
                .param("deleteMemo","")
                .param("skipPackageFlag","N")
                .param("reProcessDistributionFlag","N")
                .param("changedNoticeYn","N")
                .param("modelListAppendFlag","N")
                .param("loginUserName","user")
                .param("loginUserRole","ROLE_ADMIN")
                .param("sortField","")
                .param("sortOrder","")
                .param("hotYn","N")
                .param("creator","")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andDo(result -> {
                    Map<String, String> responseMap = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
                    assertThat(String.valueOf(responseMap.get("success"))).isEqualTo("false");
                });

    }
}

