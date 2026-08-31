package oss.fosslight.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@Transactional
public class LicenseControllerTest {

	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.107 Safari/537.36";

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("license validation should be success when parameter is valid")
	void licenseValidationShouldBeSuccess() throws Exception {
		mockMvc.perform(post("/license/validation")
			.param("licenseId", "")
			.param("licenseName", "test license v1.1")
			.param("licenseType", "PMS")
			.param("obligationNotificationYn", "Y")
			.param("shortIdentifier", "TL-1.1")
			.param("description", "")
			.param("licenseText", "test text")
			.param("attribution", "")
			.param("licenseNicknames", "TL-1.1-clause")
			.param("comment", "")
			.param("restriction", "")
			.param("loginUserName", "admin")
			.param("loginUserRole", "ROLE_ADMIN")
			.param("sortField", "")
			.param("sortOrder", "")
			.param("hotYn", "N")
			.header("user-agent", USER_AGENT)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(result -> {
				Map<String, String> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
				assertThat(response.get("isValid")).isEqualTo("true");
			});
	}

	@Test
	@DisplayName("license validation should be fail when parameter(licenseName) is duplicated")
	void licenseValidationShouldBeFailByDuplicatedLicenseName() throws Exception {
		String licenseId = createLicense();

		mockMvc.perform(post("/license/validation")
				.param("licenseId", "")
				.param("licenseName", "test license v1.0")
				.param("licenseType", "PMS")
				.param("obligationNotificationYn", "Y")
				.param("shortIdentifier", "")
				.param("description", "")
				.param("licenseText", "test text")
				.param("attribution", "")
				.param("licenseNicknames", "")
				.param("comment", "")
				.param("restriction", "")
				.param("loginUserName", "admin")
				.param("loginUserRole", "ROLE_ADMIN")
				.param("sortField", "")
				.param("sortOrder", "")
				.param("hotYn", "N")
				.header("user-agent", USER_AGENT)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(result -> {
				Map<String, String> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
				assertThat(response.get("isValid")).isEqualTo("false");
				assertThat(response.get("licenseName")).isEqualTo(MessageFormat.format("test license v1.0 are Occupied by <a class=tabLink href=#none onclick=createTabInFrame(''{0}_License'',''#/license/edit/{0}'')>< test license v1.0 ></a>", licenseId));
			});
	}

	@Test
	@DisplayName("license validation should be fail when parameter(licenseNicknames) is duplicated")
	void licenseValidationShouldBeFailByDuplicatedLicenseNicknames() throws Exception {
		String licenseId = createLicense();

		mockMvc.perform(post("/license/validation")
				.param("licenseId", "")
				.param("licenseName", "test license v1.2")
				.param("licenseType", "PMS")
				.param("obligationNotificationYn", "Y")
				.param("shortIdentifier", "")
				.param("description", "")
				.param("licenseText", "test text")
				.param("attribution", "")
				.param("licenseNicknames", "TL-1.2-clause", "TL-1.0-clause")
				.param("comment", "")
				.param("restriction", "")
				.param("loginUserName", "admin")
				.param("loginUserRole", "ROLE_ADMIN")
				.param("sortField", "")
				.param("sortOrder", "")
				.param("hotYn", "N")
				.header("user-agent", USER_AGENT)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(result -> {
				Map<String, String> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
				assertThat(response.get("isValid")).isEqualTo("false");
				assertThat(response.get("licenseNicknames.2")).isEqualTo(MessageFormat.format("TL-1.0-clause are Occupied by <a class=tabLink href=#none onclick=createTabInFrame(''{0}_License'',''#/license/edit/{0}'')>< test license v1.0 ></a>", licenseId));
			});
	}

	@Test
	@DisplayName("license validation should be fail when parameter(shortIdentifier) is duplicated")
	void licenseValidationShouldBeFailByDuplicatedShortIdentifier() throws Exception {
		String licenseId = createLicense();

		mockMvc.perform(post("/license/validation")
				.param("licenseId", "")
				.param("licenseName", "test license v1.2")
				.param("licenseType", "PMS")
				.param("obligationNotificationYn", "Y")
				.param("shortIdentifier", "TL-1.0")
				.param("description", "")
				.param("licenseText", "test text")
				.param("attribution", "")
				.param("licenseNicknames", "TL-1.2-clause")
				.param("comment", "")
				.param("restriction", "")
				.param("loginUserName", "admin")
				.param("loginUserRole", "ROLE_ADMIN")
				.param("sortField", "")
				.param("sortOrder", "")
				.param("hotYn", "N")
				.header("user-agent", USER_AGENT)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(result -> {
				Map<String, String> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
				assertThat(response.get("isValid")).isEqualTo("false");
				assertThat(response.get("shortIdentifier")).isEqualTo(MessageFormat.format("TL-1.0 are Occupied by <a class=tabLink href=#none onclick=createTabInFrame(''{0}_License'',''#/license/edit/{0}'')>< test license v1.0 ></a>", licenseId));
			});
	}

	@Test
	@DisplayName("license validation should be fail when parameter(shortIdentifier) contains comma")
	void licenseValidationShouldBeFailByShortIdentifierWithComma() throws Exception {
		mockMvc.perform(post("/license/validation")
				.param("licenseId", "")
				.param("licenseName", "test license v1.2")
				.param("licenseType", "PMS")
				.param("obligationNotificationYn", "Y")
				.param("shortIdentifier", "TL-1,2")
				.param("description", "")
				.param("licenseText", "test text")
				.param("attribution", "")
				.param("licenseNicknames", "TL-1.2-clause")
				.param("comment", "")
				.param("restriction", "")
				.param("loginUserName", "admin")
				.param("loginUserRole", "ROLE_ADMIN")
				.param("sortField", "")
				.param("sortOrder", "")
				.param("hotYn", "N")
				.header("user-agent", USER_AGENT)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(result -> {
				Map<String, String> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
				assertThat(response.get("isValid")).isEqualTo("false");
				assertThat(response.get("shortIdentifier")).isEqualTo("',' cannot be used in this field");
			});
	}

	@Test
	@DisplayName("license validation should be fail when parameter(shortIdentifier) equals to licenseName")
	void licenseValidationShouldBeFailByShortIdentifierEqualsToLicenseName() throws Exception {
		mockMvc.perform(post("/license/validation")
				.param("licenseId", "")
				.param("licenseName", "test license v1.2")
				.param("licenseType", "PMS")
				.param("obligationNotificationYn", "Y")
				.param("shortIdentifier", "test license v1.2")
				.param("description", "")
				.param("licenseText", "test text")
				.param("attribution", "")
				.param("licenseNicknames", "TL-1.2-clause")
				.param("comment", "")
				.param("restriction", "")
				.param("loginUserName", "admin")
				.param("loginUserRole", "ROLE_ADMIN")
				.param("sortField", "")
				.param("sortOrder", "")
				.param("hotYn", "N")
				.header("user-agent", USER_AGENT)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(result -> {
				Map<String, String> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
				assertThat(response.get("isValid")).isEqualTo("false");
				assertThat(response.get("shortIdentifier")).isEqualTo("You have the same name in License Name.");
			});
	}

	@Test
	@DisplayName("license delete should be success when parameter is valid")
	void licenseDeleteShouldBeSuccess() throws Exception {
		String licenseId = createLicense();

		mockMvc.perform(post("/license/delAjax")
				.param("licenseId", licenseId)
				.param("licenseName", "test license v1.0")
				.param("licenseType", "PMS")
				.param("obligationNotificationYn", "Y")
				.param("shortIdentifier", "TL-1.0")
				.param("description", "")
				.param("licenseText", "test text")
				.param("attribution", "")
				.param("licenseNicknames", "TL-1.0-clause")
				.param("comment", "test delete reason")
				.param("restriction", "")
				.param("loginUserName", "admin")
				.param("loginUserRole", "ROLE_ADMIN")
				.param("sortField", "")
				.param("sortOrder", "")
				.param("hotYn", "N")
				.header("user-agent", USER_AGENT)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(result -> {
				Map<String, String> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
				assertThat(response.get("isValid")).isEqualTo("true");
			});
	}

	@Test
	@DisplayName("license delete should be fail when license is in use by oss")
	void licenseDeleteShouldBeFail() throws Exception {
		String licenseId = createLicense();
		String ossId = createOss(licenseId);

		mockMvc.perform(post("/license/delAjax")
				.param("licenseId", licenseId)
				.param("licenseName", "test license v1.0")
				.param("licenseType", "PMS")
				.param("obligationNotificationYn", "Y")
				.param("shortIdentifier", "TL-1.0")
				.param("description", "")
				.param("licenseText", "test text")
				.param("attribution", "")
				.param("licenseNicknames", "TL-1.0-clause")
				.param("comment", "test delete reason")
				.param("restriction", "")
				.param("loginUserName", "admin")
				.param("loginUserRole", "ROLE_ADMIN")
				.param("sortField", "")
				.param("sortOrder", "")
				.param("hotYn", "N")
				.header("user-agent", USER_AGENT)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(result -> {
				Map<String, String> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
				assertThat(response.get("isValid")).isEqualTo("false");
				assertThat(response.get("validMsg")).isEqualTo(MessageFormat.format("This license is being used by 1 open source.<br><a class=tabLink href=#none onclick=createTabInFrame(''{0}_Opensource'',''#/oss/edit/{0}'')>< testOssAdd v1 ></a>", ossId));
			});
	}

	private String createLicense() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(post("/license/saveAjax")
				.param("licenseId", "")
				.param("licenseName", "test license v1.0")
				.param("licenseType", "PMS")
				.param("obligationNotificationYn", "Y")
				.param("shortIdentifier", "TL-1.0")
				.param("description", "")
				.param("licenseText", "test text")
				.param("attribution", "")
				.param("licenseNicknames", "TL-1.0-clause")
				.param("comment", "")
				.param("restriction", "")
				.param("loginUserName", "admin")
				.param("loginUserRole", "ROLE_ADMIN")
				.param("sortField", "")
				.param("sortOrder", "")
				.param("hotYn", "N")
				.header("user-agent", USER_AGENT)
				.contentType(MediaType.APPLICATION_JSON))
			.andReturn().getResponse();

		Map<String, String> responseForLicense = new ObjectMapper().readValue(response.getContentAsString(), Map.class);
		return responseForLicense.get("licenseId");
	}

	private String createOss(String licenseId) throws Exception {
		MockHttpServletResponse response = mockMvc.perform(post("/oss/saveAjax")
				.param("ossId","")
				.param("ossName","testOssAdd")
				.param("ossVersion","v1")
				.param("copyright","")
				.param("licenseDiv","")
				.param("downloadLocation","")
				.param("homepage","")
				.param("summaryDescription","")
				.param("ossType", "")
				.param("licenseId ",licenseId)
				.param("ossLicensesJson","[{\"no\":\"1\",\"ossLicenseIdx\":\"1\",\"ossLicenseComb\":\"\",\"licenseNameEx\":\"test license v1.0\",\"ossCopyright\":\"\",\"delete\":\"<input type=\\\"button\\\" value=\\\"delete\\\" class=\\\"btnCLight darkgray\\\" onclick=\\\"exeDelete(1)\\\">\",\"licenseName\":\"test license v1.0\",\"licenseId\":\"" + licenseId + "\",\"licenseType\":\"Permissive\",\"obligationChecks\":\"YNN\"}]")
				.param("ossNicknames ","")
				.param("licenseType ","PMS")
				.param("obligationType","10")
				.param("comment","")
				.param("validationType ","HOMEPAGE")
				.param("attribution ","")
				.param("addNicknameYn ","N")
				.param("deactivateFlag ","N")
				.param("renameFlag ","N")
				.param("ossCopyFlag ","N")
				.param("linkFlag ","N")
				.param("loginUserName ","admin")
				.param("loginUserRole ","ROLE_ADMIN")
				.param("sortField ","")
				.param("sortOrder ","")
				.param("hotYn ","N")
				.header("user-agent", USER_AGENT)
				.contentType(MediaType.APPLICATION_JSON))
			.andReturn().getResponse();

		Map<String, String> responseForOss = new ObjectMapper().readValue(response.getContentAsString(), Map.class);
		return responseForOss.get("ossId");
	}
}
