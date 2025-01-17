/*
Copyright (c) 2021 Jongun Chae
Copyright (c) 2021 JaeHyeuk Lee
SPDX-License-Identifier: AGPL-3.0-only
*/
package oss.fosslight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.Project;

import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@WithMockUser(username = "user", roles = {"USER"})
@Transactional
public class PackingRequestTest {

    @Autowired
    private MockMvc mockMvc;

    private Project prj;


    @BeforeEach
    void createProjectAndOssNotice() throws Exception{
        MockHttpServletResponse prResponse = mockMvc.perform(post("/project/saveAjax")
                .param("prjId", "")
                .param("prjName", UUID.randomUUID().toString())
                .param("useCustomNoticeYn","N")
                .param("allowDownloadBitFlag","1")
                .param("allowDownloadNoticeHTMLYn","Y")
                .param("allowDownloadNoticeTextYn","N")
                .param("allowDownloadSimpleHTMLYn","N")
                .param("allowDownloadSimpleTextYn","N")
                .param("allowDownloadSPDXSheetYn","N")
                .param("allowDownloadSPDXRdfYn","N")
                .param("allowDownloadSPDXTagYn","N")
                .param("prjVersion", "v1")
                .param("noticeType", "10")
                .param("distributionType", "10")
                .param("comment", "<p>test</p>")
                .param("osType", "100")
                .param("priority", "30")
                .param("statusRequestYn", "")
                .param("listId", "")
                .param("publicYn", "Y")
                .param("secMailYn","Y")
                .param("networkServerType", "N")
                .param("prjModelJson", "[]")
                .param("verificationStatus","REQ")
                .param("distributeDeployModelYn","N")
                .param("excelDownloadFlag","N")
                .param("referenceDiv","12")
                .param("networkServerFlag","N")
                .param("loadFromAndroidProjectFlag","N")
                .param("versionMatchedFlag","N")
                .param("resetDistributionStatus","N")
                .param("skipPackageFlag","N")
                .param("userComment","")
                .param("reProcessDistributionFlag","N")
                .param("changedNoticeYn","N")
                .param("modelListAppendFlag","N")
                .param("copyFlag","N")
                .param("loginUserName","user")
                .param("loginUserRole","ROLE_ADMIN")
                .param("sortField","")
                .param("sortOrder","")
                .param("hotYn","N")
                .param("copy","false")
                .param("confirmStatusCopy","false")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        Map<String, Map<String, String>> responseMapForProject = new ObjectMapper().readValue(prResponse.getContentAsString(), Map.class);
        String prjId = responseMapForProject.get("resultData").get("prjId");
        prj = (Project) mockMvc.perform(MockMvcRequestBuilders.get("/project/edit/" + prjId)
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView().getModel().get("project");
        prj.setVerificationStatus("REQ");


    }



    Project editProject(String prjId) throws Exception {
        Project pr;
        pr = (Project) mockMvc.perform(MockMvcRequestBuilders.get("/project/edit/" + prj.getPrjId())
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView().getModel().get("project");
        return pr;
    }

    @Test
    @DisplayName("packing request review should be success When parameter is valid")
    void PackingRequestReviewSuccess() throws Exception{

        // given

        //when

        String content = new ObjectMapper().writeValueAsString(prj);

        mockMvc.perform(post("/project/verification/saveAjax")
                .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                        new BasicNameValuePair("prjId", prj.getPrjId()),
                        new BasicNameValuePair("noticeType", "10"),
                        new BasicNameValuePair("appended", ""),
                        new BasicNameValuePair("appendedTEXT", ""),
                        new BasicNameValuePair("networkServerFlag", "N"),
                        new BasicNameValuePair("setPackageJson", ""),
                        new BasicNameValuePair("setPackageFileId", ""),
                        new BasicNameValuePair("setEditCompanyYn", "Y")
                ))))
                .param("useCustomNoticeYn", "N")
                .param("allowDownloadNoticeHTMLYn", "Y")
                .param("allowDownloadNoticeTextYn", "N")
                .param("allowDownloadSimpleHTMLYn", "N")
                .param("allowDownloadSimpleTextYn", "N")
                .param("allowDownloadSPDXSheetYn", "N")
                .param("allowDownloadSPDXRdfYn", "N")
                .param("allowDownloadSPDXTagYn", "N")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        mockMvc.perform(post("/project/updateProjectStatus")
                .content(content)
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result ->
                        Assertions.assertThat(editProject(prj.getPrjId()).getVerificationStatus()).isEqualTo("REQ")
                );

        //then

    }

    @Test
    @DisplayName("packing request review should be fail When parameter is invalid")
    void PackingRequestReviewFail() throws Exception{

        //  given

        //  when

        prj.setVerificationStatus("REV");
        String content = new ObjectMapper().writeValueAsString(prj);

        mockMvc.perform(post("/project/verification/saveAjax")
                .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                        new BasicNameValuePair("prjId", prj.getPrjId()),
                        new BasicNameValuePair("noticeType", "10"),
                        new BasicNameValuePair("appended", ""),
                        new BasicNameValuePair("appendedTEXT", ""),
                        new BasicNameValuePair("networkServerFlag", "N"),
                        new BasicNameValuePair("setPackageJson", ""),
                        new BasicNameValuePair("setPackageFileId", ""),
                        new BasicNameValuePair("setEditCompanyYn", "Y")
                ))))
                .param("useCustomNoticeYn", "N")
                .param("allowDownloadNoticeHTMLYn", "Y")
                .param("allowDownloadNoticeTextYn", "N")
                .param("allowDownloadSimpleHTMLYn", "N")
                .param("allowDownloadSimpleTextYn", "N")
                .param("allowDownloadSPDXSheetYn", "N")
                .param("allowDownloadSPDXRdfYn", "N")
                .param("allowDownloadSPDXTagYn", "N")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        mockMvc.perform(post("/project/updateProjectStatus")
                .content(content)
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Assertions.assertThat(editProject(prj.getPrjId()).getVerificationStatus()).isNotEqualTo("REQ");
                });
        //  then

    }

}
