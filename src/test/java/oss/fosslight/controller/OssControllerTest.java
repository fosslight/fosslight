/*
Copyright (c) 2021 Jongun Chae
Copyright (c) 2021 JaeHyeuk Lee
SPDX-License-Identifier: AGPL-3.0-only
*/
package oss.fosslight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@WithMockUser(username = "user", roles = {"USER"})
@Transactional
public class OssControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("oss list add should be success When parameter is valid")
    void ossListAddShouldBeSuccess() throws Exception{
        mockMvc.perform(post("/oss/saveAjax")
                .param("ossId","")
                .param("ossName","testOssAdd")
                .param("ossVersion","v1")
                .param("copyright","")
                .param("licenseDiv","")
                .param("downloadLocation","")
                .param("homepage","")
                .param("summaryDescription","")
                .param("ossType", "")
                .param("licenseId ","")
                .param("ossLicensesJson","[]")
                .param("ossNicknames ","")
                .param("licenseType ","PMS")
                .param("obligationType","10")
                .param("comment","")
                .param("validationType ","HOMEPAGE")
                .param("attribution ","")
                .param("addNicknameYn ","N")
                .param("deactivateFlag ","N")
                .param("loginUserName ","user")
                .param("loginUserRole ","ROLE_ADMIN")
                .param("sortField ","")
                .param("sortOrder ","")
                .param("hotYn ","N")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> assertOssAdded(result.getResponse()));

    }

    @Test
    @DisplayName("oss list add should be fail When parameter(ossName) is invalid")
    void ossListAddShouldBeFail() throws Exception{
        mockMvc.perform(post("/oss/saveAjax")
                .param("ossId","")
                .param("ossVersion","v1")
                .param("copyright","")
                .param("licenseDiv","")
                .param("downloadLocation","")
                .param("homepage","")
                .param("summaryDescription","")
                .param("ossType", "")
                .param("licenseId ","")
                .param("ossLicensesJson","[]")
                .param("ossNicknames ","")
                .param("licenseType ","PMS")
                .param("obligationType","10")
                .param("comment","")
                .param("validationType ","HOMEPAGE")
                .param("attribution ","")
                .param("addNicknameYn ","N")
                .param("deactivateFlag ","N")
                .param("loginUserName ","user")
                .param("loginUserRole ","ROLE_ADMIN")
                .param("sortField ","")
                .param("sortOrder ","")
                .param("hotYn ","N")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Map<String,String> responseMap = new ObjectMapper().readValue(result.getResponse().getContentAsString(),Map.class);
                    assertThat(responseMap.get("resCd")).isEqualTo("00");
                });

    }

    private void assertOssAdded(MockHttpServletResponse response) throws Exception {
        Map<String, String> responseMap = new ObjectMapper().readValue(response.getContentAsString(), Map.class);
        String ossId = responseMap.get("ossId");
        mockMvc.perform(MockMvcRequestBuilders.get("/oss/edit/" + ossId)
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String oss = (String) result.getModelAndView().getModel().get("ossId");
                    assertThat(oss).isEqualTo(ossId);
                });
    }
}
