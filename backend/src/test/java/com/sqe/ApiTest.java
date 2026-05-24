package com.sqe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 后端接口自动化测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTest {

    @Autowired
    private MockMvc mockMvc;

    private static String adminToken;
    private static String teacherToken;
    private static String studentToken;
    private static String parentToken;
    private static final ObjectMapper mapper = new ObjectMapper();

    /* 管理员登录测试 */
    @Test
    @Order(1)
    void testAdminLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("admin"))
                .andReturn();
        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).get("data");
        adminToken = data.get("token").asText();
    }

    /* 教师登录测试 */
    @Test
    @Order(2)
    void testTeacherLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"teacher2201\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("teacher"))
                .andReturn();
        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).get("data");
        teacherToken = data.get("token").asText();
    }

    /* 学生登录测试 */
    @Test
    @Order(3)
    void testStudentLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"2200770128\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("student"))
                .andReturn();
        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).get("data");
        studentToken = data.get("token").asText();
    }

    /* 家长登录测试 */
    @Test
    @Order(4)
    void testParentLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"parent1\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("parent"))
                .andReturn();
        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).get("data");
        parentToken = data.get("token").asText();
    }

    /* 错误密码登录测试 */
    @Test
    @Order(5)
    void testLoginWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    /* 用户列表查询测试 */
    @Test
    @Order(10)
    void testUserPage() throws Exception {
        mockMvc.perform(get("/api/user/page?current=1&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    /* 获取当前用户信息测试 */
    @Test
    @Order(11)
    void testUserInfo() throws Exception {
        mockMvc.perform(get("/api/user/info")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /* 班级列表测试 */
    @Test
    @Order(20)
    void testClassList() throws Exception {
        mockMvc.perform(get("/api/class/list")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /* 学生分页查询测试 */
    @Test
    @Order(30)
    void testStudentPage() throws Exception {
        mockMvc.perform(get("/api/student/page?current=1&size=10")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /* 新增学生时自动创建学生账号并绑定userId */
    @Test
    @Order(31)
    void testCreateStudentCreatesUserBinding() throws Exception {
        String studentNo = "2099" + System.currentTimeMillis();
        String body = "{"
                + "\"studentNo\":\"" + studentNo + "\","
                + "\"realName\":\"测试学生\","
                + "\"classId\":1,"
                + "\"gender\":1,"
                + "\"enrollmentYear\":2099,"
                + "\"phone\":\"13800000000\","
                + "\"email\":\"student-test@example.com\""
                + "}";

        mockMvc.perform(post("/api/student")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/student/page?current=1&size=10&keyword=" + studentNo)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].studentNo").value(studentNo))
                .andExpect(jsonPath("$.data.records[0].userId").isNumber())
                .andExpect(jsonPath("$.data.records[0].realName").value("测试学生"));
    }

    /* 品德评价分页测试 */
    @Test
    @Order(40)
    void testMoralPage() throws Exception {
        mockMvc.perform(get("/api/evaluation/moral/page?current=1&size=10")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /* 学业评价分页测试 */
    @Test
    @Order(41)
    void testAcademicPage() throws Exception {
        mockMvc.perform(get("/api/evaluation/academic/page?current=1&size=10")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /* 综合评价分页测试 */
    @Test
    @Order(50)
    void testComprehensivePage() throws Exception {
        mockMvc.perform(get("/api/evaluation/comprehensive/page?current=1&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /* 学生查看自己的评价测试 */
    @Test
    @Order(60)
    void testMyEvaluation() throws Exception {
        mockMvc.perform(get("/api/evaluation/comprehensive/my")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /* 家长查看孩子列表测试 */
    @Test
    @Order(61)
    void testChildrenList() throws Exception {
        mockMvc.perform(get("/api/student/children")
                        .header("Authorization", "Bearer " + parentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /* 数据看板概览测试 */
    @Test
    @Order(70)
    void testDashboardOverview() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.studentCount").exists());
    }

    /* 维度平均分测试 */
    @Test
    @Order(71)
    void testDimensionAvg() throws Exception {
        mockMvc.perform(get("/api/dashboard/dimension-avg?academicYear=2023-2024")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /* 聚类结果查询测试 */
    @Test
    @Order(80)
    void testClusterResults() throws Exception {
        mockMvc.perform(get("/api/cluster/results?academicYear=2023-2024")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /* 通知列表测试 */
    @Test
    @Order(90)
    void testNoticePage() throws Exception {
        mockMvc.perform(get("/api/notice/page?current=1&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /* 日志列表测试 */
    @Test
    @Order(91)
    void testLogPage() throws Exception {
        mockMvc.perform(get("/api/log/page?current=1&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    /* 非管理员不能访问日志 */
    @Test
    @Order(92)
    void testLogPageForbiddenForTeacher() throws Exception {
        mockMvc.perform(get("/api/log/page?current=1&size=10")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    /* 写操作自动记录日志，查询操作不记录 */
    @Test
    @Order(93)
    void testWriteOperationCreatesLogAndGetDoesNot() throws Exception {
        long beforeCount = getLogTotal();

        mockMvc.perform(get("/api/user/page?current=1&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        Assertions.assertEquals(beforeCount, getLogTotal());

        mockMvc.perform(put("/api/user/resetPwd/2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult result = mockMvc.perform(get("/api/log/page?current=1&size=1&keyword=重置密码")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.records[0].module").value("用户管理"))
                .andExpect(jsonPath("$.data.records[0].result").value("SUCCESS"))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Assertions.assertFalse(content.contains("123456"));
    }

    /* 按时间清理日志 */
    @Test
    @Order(94)
    void testCleanLogBeforeTime() throws Exception {
        mockMvc.perform(delete("/api/log/clean?beforeTime=2099-01-01 00:00:00")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/log/page?current=1&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].operation").value("清理操作日志"));
    }

    /* 未认证访问测试 */
    @Test
    @Order(100)
    void testUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/page"))
                .andExpect(status().isForbidden());
    }

    private long getLogTotal() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/log/page?current=1&size=1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).get("data");
        return data.get("total").asLong();
    }
}
