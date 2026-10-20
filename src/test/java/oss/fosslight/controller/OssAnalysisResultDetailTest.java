/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.server.ResponseStatusException;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.advice.ExceptionAdvice;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.OssAnalysis;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.repository.CodeManagerMapper;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.FileService;
import oss.fosslight.service.OssService;
import oss.fosslight.validation.T2CoValidationConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class OssAnalysisResultDetailTest {
    private static final String PROJECT_ID = "42";
    private static final String GROUP_ID = "101";
    private static final String LATEST_TITLE = "sample \ucd5c\uc2e0 \ub4f1\ub85d \uc815\ubcf4";
    private static final String CSV_HEADER = "ID|RESULT|OSS NAME|OSS VERSION|NICKNAME|LICENSE|CONCLUDED LICENSE"
            + "|MAIN LICENSE|LICENSE(SCANCODE)|NEED REVIEW LICENSE(SCANCODE)"
            + "|DOWNLOAD LOCATION|HOMEPAGE|COPYRIGHT TEXT|COMMENT\n";

    @TempDir
    Path outputDirectory;

    private OssService ossService;
    private OssController controller;
    private MockMvc mvc;
    private MockHttpSession session;
    private Environment previousEnvironment;
    private MessageSource previousMessageSource;
    private OssService previousOssService;
    private WebApplicationContext previousApplicationContext;
    private StaticWebApplicationContext applicationContext;
    private final Map<String, Object> previousValidationState = new HashMap<>();
    private final Map<String, Object> previousCodeState = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        previousEnvironment = (Environment) ReflectionTestUtils.getField(CoTopComponent.class, "appEnv");
        previousMessageSource = (MessageSource) ReflectionTestUtils.getField(CoTopComponent.class, "messageSource");
        previousOssService = (OssService) ReflectionTestUtils.getField(CommonFunction.class, "ossService");
        previousApplicationContext = (WebApplicationContext) ReflectionTestUtils.getField(
                CoTopComponent.class, "applicationContext");
        for (String field : List.of("instance", "ruleMap", "ruleAllMap", "messageConfigMap", "env")) {
            previousValidationState.put(field, ReflectionTestUtils.getField(T2CoValidationConfig.class, field));
        }
        for (String field : List.of("instance", "codes", "emptyVector", "codeManagerMapper", "licenseMapper",
                "ossMapper", "LICENSE_INFO", "LICENSE_INFO_UPPER", "LICENSE_INFO_BY_ID",
                "LICENSE_USER_GUIDE_LIST", "CD_ROLE_OUT_LICENSE", "CD_ROLE_OUT_LICENSE_ID_LIST")) {
            previousCodeState.put(field, ReflectionTestUtils.getField(CoCodeManager.class, field));
        }

        controller = new OssController();
        ossService = mock(OssService.class);
        controller.ossService = ossService;
        applicationContext = new StaticWebApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("ossService", ossService);
        applicationContext.getBeanFactory().registerSingleton("fileService", mock(FileService.class));
        applicationContext.getBeanFactory().registerSingleton("projectMapper", mock(ProjectMapper.class));
        applicationContext.getBeanFactory().registerSingleton("codeMapper", mock(CodeMapper.class));
        applicationContext.refresh();
        controller.setWebApplicationContext(applicationContext);
        MockEnvironment environment = new MockEnvironment()
                .withProperty("autoanalysis.output.path", outputDirectory.toString());
        CompositePropertySource validation = new CompositePropertySource("test-validation");
        validation.addPropertySource(new ResourcePropertySource("classpath:messages/validation.properties"));
        environment.getPropertySources().addLast(validation);
        controller.setEnvironment(environment);
        new T2CoValidationConfig().setEnvironment(environment);
        CodeManagerMapper codeMapper = mock(CodeManagerMapper.class);
        when(codeMapper.getCodeListAll()).thenReturn(List.of());
        LicenseMapper licenseMapper = mock(LicenseMapper.class);
        LicenseMaster mit = new LicenseMaster();
        mit.setLicenseId("mit-id");
        mit.setLicenseName("MIT");
        mit.setShortIdentifier("MIT");
        mit.setLicenseType("PMS");
        when(licenseMapper.getLicenseInfoInit()).thenReturn(List.of(mit));
        CoCodeManager.setCodeManagerMapper(codeMapper);
        CoCodeManager.setLicenseMapper(licenseMapper);
        CoCodeManager.setOssMapper(mock(OssMapper.class));
        BeanUtils.instantiateClass(CoCodeManager.class).init();
        StaticMessageSource messages = new StaticMessageSource();
        messages.setUseCodeAsDefaultMessage(true);
        controller.setMessageSource(messages);
        CommonFunction.setOssService(ossService);
        when(ossService.getDeactivateOssList()).thenReturn(new ArrayList<>());
        when(ossService.getOssAnalysisList(any(OssMaster.class)))
                .thenReturn(Map.of("rows", List.of(inputRow())));
        when(ossService.getNewestOssInfo(any(OssAnalysis.class))).thenAnswer(invocation -> {
            OssAnalysis source = invocation.getArgument(0);
            return new OssAnalysis(source.getGroupId(), source.getOssName(), "9.0", "",
                    "MIT", "", "", "", "", "", "true", LATEST_TITLE);
        });

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("reviewer", "", "ROLE_ADMIN"));
        session = new MockHttpSession();
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionAdvice(mock(ResponseService.class), messages))
                .alwaysDo(result -> {
                    Exception exception = result.getResolvedException();
                    if (exception != null && !(exception instanceof ResponseStatusException)) {
                        throw new AssertionError("Unexpected controller error", exception);
                    }
                })
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        controller.setEnvironment(previousEnvironment);
        controller.setMessageSource(previousMessageSource);
        CommonFunction.setOssService(previousOssService);
        controller.setWebApplicationContext(previousApplicationContext);
        applicationContext.close();
        previousValidationState.forEach((field, value) ->
                ReflectionTestUtils.setField(T2CoValidationConfig.class, field, value));
        previousCodeState.forEach((field, value) ->
                ReflectionTestUtils.setField(CoCodeManager.class, field, value));
        SecurityContextHolder.clearContext();
    }

    @Test
    void directDetailLoadsOnlyTheRequestedGroupWithoutOpeningTheList() throws Exception {
        StringBuilder csv = new StringBuilder(CSV_HEADER).append("metadata\n");
        for (int id = 1; id <= 40; id++) {
            csv.append(csvRow(Integer.toString(id), "unrelated"));
        }
        csv.append(csvRow(GROUP_ID, "first comment"));
        writeCsv(PROJECT_ID, csv.toString());
        OssMaster registered = new OssMaster();
        registered.setOssId("registered-id");
        registered.setImportantNotes("registered notes");
        when(ossService.getOssInfo(null, "sample", false)).thenReturn(registered);

        mvc.perform(get("/oss/getAnalysisResultDetail/" + GROUP_ID)
                        .param("prjId", PROJECT_ID).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("oss/ossAnalysisResultDetailpopup"))
                .andExpect(model().attribute("groupId", GROUP_ID));

        List<OssAnalysis> rows = storedRows();
        assertThat(rows).isNotEmpty().allMatch(row -> GROUP_ID.equals(row.getGroupId()));
        assertThat(rows).anyMatch(row -> row.getTitle().startsWith("Scancode")
                && "MIT(2)".equals(row.getScancodeLicense())
                && "GPL-2.0-only(1)".equals(row.getNeedReviewLicenseScanode())
                && "job-7".equals(row.getJobSeq()));
        assertThat(rows).anyMatch(row -> LATEST_TITLE.equals(row.getTitle())
                && "registered-id".equals(row.getOssId())
                && "registered notes".equals(row.getImportantNotes()));

        ArgumentCaptor<OssMaster> query = ArgumentCaptor.forClass(OssMaster.class);
        verify(ossService).getOssAnalysisList(query.capture());
        assertThat(query.getValue().getPrjId()).isEqualTo(PROJECT_ID);
        assertThat(query.getValue().getStartAnalysisFlag()).isEqualTo(CoConstDef.FLAG_YES);
        assertThat(query.getValue().getCsvComponentIdList()).containsExactly(101);
        assertThat(query.getValue().getCurPage()).isEqualTo(1);
        assertThat(query.getValue().getPageListSize()).isEqualTo(1);

        mvc.perform(post("/oss/getSessionAnalysisResultData").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupId\":\"" + GROUP_ID + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.detailData.length()").value(rows.size()));
    }

    @Test
    void directDetailRefreshesPreviouslyStoredData() throws Exception {
        writeCsv(PROJECT_ID, CSV_HEADER + "metadata\n" + csvRow(GROUP_ID, "first comment"));
        mvc.perform(get("/oss/getAnalysisResultDetail/" + GROUP_ID)
                .param("prjId", PROJECT_ID).session(session)).andExpect(status().isOk());
        assertThat(storedRows()).anyMatch(row -> "first comment".equals(row.getComment()));

        writeCsv(PROJECT_ID, CSV_HEADER + "metadata\n" + csvRow(GROUP_ID, "updated comment"));
        mvc.perform(get("/oss/getAnalysisResultDetail/" + GROUP_ID)
                .param("prjId", PROJECT_ID).session(session)).andExpect(status().isOk());
        assertThat(storedRows()).noneMatch(row -> "first comment".equals(row.getComment()));
        assertThat(storedRows()).anyMatch(row -> "updated comment".equals(row.getComment()));
    }

    @Test
    void legacyDetailUsesItsExistingSessionWithoutLoadingFiles() throws Exception {
        OssAnalysis row = inputRow();
        session.setAttribute(sessionKey(), List.of(row));

        mvc.perform(get("/oss/getAnalysisResultDetail/" + GROUP_ID).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("oss/ossAnalysisResultDetailpopup"))
                .andExpect(model().attribute("groupId", GROUP_ID));

        assertThat(storedRows()).containsExactly(row);
        verifyNoInteractions(ossService);
    }

    @Test
    void legacySessionPreparationKeepsLocationCleanupAndRegistrationMetadata() throws Exception {
        OssAnalysis row = inputRow();
        row.setTitle(LATEST_TITLE);
        row.setDownloadLocation("https://example.test/src, http://EXAMPLE.test/src,https://other.test/src");
        OssMaster registered = new OssMaster();
        registered.setOssId("registered-id");
        registered.setImportantNotes("registered notes");
        when(ossService.getOssInfo(null, "sample", false)).thenReturn(registered);

        mvc.perform(post("/oss/setSessionAnalysisResultData").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(Map.of(
                                "groupId", GROUP_ID,
                                "dataString", new ObjectMapper().writeValueAsString(List.of(row))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        assertThat(storedRows()).singleElement().satisfies(stored -> {
            assertThat(stored.getDownloadLocation()).isEqualTo(
                    "https://example.test/src,https://other.test/src");
            assertThat(stored.getOssId()).isEqualTo("registered-id");
            assertThat(stored.getImportantNotes()).isEqualTo("registered notes");
        });
        verify(ossService).getOssInfo(eq(null), eq("sample"), eq(false));
    }

    @Test
    void reviewListLinkRedirectsDirectlyAndPreservesEncodedParameters() throws Exception {
        String note = "\ud55c\uae00 & + ? {note}";
        mvc.perform(get("/oss/ossAutoAnalysis").param("prjId", PROJECT_ID)
                        .param("gridId", GROUP_ID).param("ossAnalysisStatus", "result")
                        .param("reviewNote", note).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oss/getAnalysisResultDetail/" + GROUP_ID
                        + "?prjId=" + PROJECT_ID + "&reviewNote="
                        + URLEncoder.encode(note, StandardCharsets.UTF_8)));
        verifyNoInteractions(ossService);
    }

    @Test
    void ordinaryListAndPostRequestsStillOpenTheList() throws Exception {
        mvc.perform(get("/oss/ossAutoAnalysis").param("prjId", PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("oss/ossAutoAnalysispopup"));
        mvc.perform(get("/oss/ossAutoAnalysis").param("prjId", PROJECT_ID).param("gridId", GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("oss/ossAutoAnalysispopup"));
        mvc.perform(get("/oss/ossAutoAnalysis").param("prjId", PROJECT_ID)
                        .param("gridId", GROUP_ID).param("ossAnalysisStatus", "input"))
                .andExpect(status().isOk())
                .andExpect(view().name("oss/ossAutoAnalysispopup"));
        mvc.perform(post("/oss/ossAutoAnalysis").param("prjId", PROJECT_ID).param("gridId", GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("oss/ossAutoAnalysispopup"));
    }

    @Test
    void missingResultReturnsAnExplicitNotFoundInsteadOfAnEmptyPopup() throws Exception {
        mvc.perform(get("/oss/getAnalysisResultDetail/" + GROUP_ID)
                        .param("prjId", PROJECT_ID).session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.errorMsg").isNotEmpty());
        verifyNoInteractions(ossService);
    }

    @Test
    void aGroupOutsideTheRequestedProjectCannotReuseExistingSessionData() throws Exception {
        session.setAttribute(sessionKey(), List.of(inputRow()));
        writeCsv("43", CSV_HEADER + "metadata\n" + csvRow("102", "another project"));

        mvc.perform(get("/oss/getAnalysisResultDetail/" + GROUP_ID)
                        .param("prjId", "43").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isValid").value(false));
        verifyNoInteractions(ossService);
    }

    @Test
    void unavailableAnalyzedRowReturnsAnExplicitNotFound() throws Exception {
        writeCsv(PROJECT_ID, CSV_HEADER + "metadata\n" + csvRow(GROUP_ID, "first comment"));
        when(ossService.getOssAnalysisList(any(OssMaster.class))).thenReturn(Map.of("rows", List.of()));

        mvc.perform(get("/oss/getAnalysisResultDetail/" + GROUP_ID)
                        .param("prjId", PROJECT_ID).session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isValid").value(false));
        assertThat(session.getAttribute(sessionKey())).isNull();
    }

    @Test
    void malformedResultReturnsAnExplicitError() throws Exception {
        writeCsv(PROJECT_ID, "ID|OSS NAME|OSS VERSION\nmetadata\n101|sample|1.0\n");

        mvc.perform(get("/oss/getAnalysisResultDetail/" + GROUP_ID)
                        .param("prjId", PROJECT_ID).session(session))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.errorMsg").isNotEmpty());
        assertThat(session.getAttribute(sessionKey())).isNull();
    }

    @Test
    void invalidProjectPathIsRejectedBeforeReadingAnyData() throws Exception {
        mvc.perform(get("/oss/getAnalysisResultDetail/" + GROUP_ID)
                        .param("prjId", "../outside").session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isValid").value(false));
        verifyNoInteractions(ossService);
    }

    private OssAnalysis inputRow() {
        OssAnalysis row = new OssAnalysis(GROUP_ID, "sample", "1.0", "",
                "MIT", "", "https://example.test/src", "https://example.test",
                "", "", "true", "");
        row.setComponentId(GROUP_ID);
        row.setGridId(GROUP_ID);
        row.setPrjId(PROJECT_ID);
        row.setCompleteYn(CoConstDef.FLAG_NO);
        row.setAnalysisYn(CoConstDef.FLAG_YES);
        row.setJobSeq("job-7");
        return row;
    }

    private String csvRow(String id, String comment) {
        return id + "|TRUE|sample|1.0||MIT|MIT|MIT(1)|MIT(2)|GPL-2.0-only(1)"
                + "|https://example.test/src|https://example.test|copyright|" + comment + "\n";
    }

    private void writeCsv(String projectId, String csv) throws Exception {
        Path resultDirectory = Files.createDirectories(outputDirectory.resolve(projectId).resolve("result"));
        Files.writeString(resultDirectory.resolve("analysis.csv"), csv, StandardCharsets.UTF_8);
    }

    private String sessionKey() {
        return CommonFunction.makeSessionKey("reviewer", CoConstDef.SESSION_KEY_ANALYSIS_RESULT_DATA, GROUP_ID);
    }

    @SuppressWarnings("unchecked")
    private List<OssAnalysis> storedRows() {
        Object rows = session.getAttribute(sessionKey());
        assertThat(rows).isInstanceOf(List.class);
        return (List<OssAnalysis>) rows;
    }
}
