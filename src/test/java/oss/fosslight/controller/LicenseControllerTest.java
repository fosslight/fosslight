package oss.fosslight.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import javax.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@Transactional
class LicenseControllerTest {
  @Autowired
  MockMvc mockMvc;

  //해당 request에 대해 올바른 response값을 내놓은걸 확인하면 된다.
  //get("/license/edit")
  @Test
  @DisplayName("Edit Test")
  void edit() throws Exception {
    Cookie cookie = new Cookie("customData", "customString");
    //TODO: 쿠키 세팅, 포스트맨 테스트 같이 해보기
    mockMvc.perform(get("/license/edit")
            .cookie(cookie))
        .andExpect(status().isOk())
        .andExpect(view().name("/tiles/admin/license/edit"));
  }

  @Test
  void testEdit() {

  }
}