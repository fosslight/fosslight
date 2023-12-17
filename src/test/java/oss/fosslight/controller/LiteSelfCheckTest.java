/*
Copyright (c) 2021 Jongun Chae
Copyright (c) 2021 JaeHyeuk Lee
SPDX-License-Identifier: AGPL-3.0-only
*/
package oss.fosslight.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import oss.fosslight.service.ApiSelfCheckService;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application.test.properties")
public class LiteSelfCheckTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiSelfCheckService apiSelfCheckService;

    @Test
    void noticeMailShouldBeSent () {
        var result = apiSelfCheckService.sendLicenseNoticeEmail();
        assertThat(result);
    }
}
