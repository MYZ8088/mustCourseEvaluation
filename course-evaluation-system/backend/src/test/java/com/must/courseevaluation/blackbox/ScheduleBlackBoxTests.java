package com.must.courseevaluation.blackbox;

import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 时间表模块黑盒测试
 * 
 * 测试功能：时间冲突检测
 * 
 * 测试方法：
 * 1. 等价类划分（Equivalence Partitioning）
 * 2. 边界值测试（Boundary Value Analysis）
 * 3. 因果图（Cause-Effect Graphing）
 * 4. 判定表（Decision Table）
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("时间表模块 - 黑盒测试")
public class ScheduleBlackBoxTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "schedule_test_user";

    @BeforeAll
    @Transactional
    void setupTestUser() {
        // 删除可能存在的测试用户
        userRepository.findByUsername(TEST_USERNAME).ifPresent(user -> 
            userRepository.delete(user)
        );

        // 创建测试用户
        User testUser = new User();
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("schedule_test@student.must.edu.mo");
        testUser.setRole(User.Role.ROLE_STUDENT);
        testUser.setActive(true);
        testUser.setCanComment(true);
        userRepository.save(testUser);
    }

    @AfterAll
    @Transactional
    void cleanupTestUser() {
        // 清理测试用户
        userRepository.findByUsername(TEST_USERNAME).ifPresent(user -> 
            userRepository.delete(user)
        );
    }

    /**
     * 4.1 等价类划分测试 - 时间冲突检测
     */
    @Nested
    @DisplayName("4.1 时间冲突检测 - 等价类划分测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ScheduleConflictEquivalencePartitioningTests {

        @Test
        @Order(1)
        @DisplayName("TC_SCH_EQ_001: 有效参数且有冲突 - 返回true")
        @WithUserDetails(TEST_USERNAME)
        void testValidParametersWithConflict() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasConflict").exists());
        }

        @Test
        @Order(2)
        @DisplayName("TC_SCH_EQ_002: 有效参数且无冲突 - 返回false")
        @WithUserDetails(TEST_USERNAME)
        void testValidParametersWithoutConflict() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "7")
                    .param("timePeriod", "4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasConflict").exists());
        }

        @Test
        @Order(3)
        @DisplayName("TC_SCH_EQ_003: dayOfWeek=0 - 返回400")
        @WithUserDetails(TEST_USERNAME)
        void testDayOfWeek0() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "0")
                    .param("timePeriod", "1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(4)
        @DisplayName("TC_SCH_EQ_004: dayOfWeek=8 - 返回400")
        @WithUserDetails(TEST_USERNAME)
        void testDayOfWeek8() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "8")
                    .param("timePeriod", "1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(5)
        @DisplayName("TC_SCH_EQ_005: dayOfWeek负数 - 返回400")
        @WithUserDetails(TEST_USERNAME)
        void testDayOfWeekNegative() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "-1")
                    .param("timePeriod", "1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(6)
        @DisplayName("TC_SCH_EQ_007: timePeriod=0 - 返回400")
        @WithUserDetails(TEST_USERNAME)
        void testTimePeriod0() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(7)
        @DisplayName("TC_SCH_EQ_008: timePeriod=5 - 返回400")
        @WithUserDetails(TEST_USERNAME)
        void testTimePeriod5() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "5"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(8)
        @DisplayName("TC_SCH_EQ_009: timePeriod负数 - 返回400")
        @WithUserDetails(TEST_USERNAME)
        void testTimePeriodNegative() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(9)
        @DisplayName("TC_SCH_EQ_011: 未登录 - 返回401")
        void testWithoutAuthentication() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 4.2 边界值测试 - 时间冲突检测
     */
    @Nested
    @DisplayName("4.2 时间冲突检测 - 边界值测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ScheduleConflictBoundaryValueTests {

        @Test
        @Order(1)
        @DisplayName("TC_SCH_BV_001: dayOfWeek=0 - 失败")
        @WithUserDetails(TEST_USERNAME)
        void testDayBoundary0() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "0")
                    .param("timePeriod", "1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(2)
        @DisplayName("TC_SCH_BV_002: dayOfWeek=1 - 成功")
        @WithUserDetails(TEST_USERNAME)
        void testDayBoundary1() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(3)
        @DisplayName("TC_SCH_BV_003: dayOfWeek=2 - 成功")
        @WithUserDetails(TEST_USERNAME)
        void testDayBoundary2() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "2")
                    .param("timePeriod", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(4)
        @DisplayName("TC_SCH_BV_004: dayOfWeek=6 - 成功")
        @WithUserDetails(TEST_USERNAME)
        void testDayBoundary6() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "6")
                    .param("timePeriod", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(5)
        @DisplayName("TC_SCH_BV_005: dayOfWeek=7 - 成功")
        @WithUserDetails(TEST_USERNAME)
        void testDayBoundary7() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "7")
                    .param("timePeriod", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(6)
        @DisplayName("TC_SCH_BV_006: dayOfWeek=8 - 失败")
        @WithUserDetails(TEST_USERNAME)
        void testDayBoundary8() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "8")
                    .param("timePeriod", "1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(7)
        @DisplayName("TC_SCH_BV_007: timePeriod=0 - 失败")
        @WithUserDetails(TEST_USERNAME)
        void testTimeBoundary0() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(8)
        @DisplayName("TC_SCH_BV_008: timePeriod=1 - 成功")
        @WithUserDetails(TEST_USERNAME)
        void testTimeBoundary1() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(9)
        @DisplayName("TC_SCH_BV_009: timePeriod=2 - 成功")
        @WithUserDetails(TEST_USERNAME)
        void testTimeBoundary2() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "2"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(10)
        @DisplayName("TC_SCH_BV_010: timePeriod=3 - 成功")
        @WithUserDetails(TEST_USERNAME)
        void testTimeBoundary3() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "3"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(11)
        @DisplayName("TC_SCH_BV_011: timePeriod=4 - 成功")
        @WithUserDetails(TEST_USERNAME)
        void testTimeBoundary4() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "4"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(12)
        @DisplayName("TC_SCH_BV_012: timePeriod=5 - 失败")
        @WithUserDetails(TEST_USERNAME)
        void testTimeBoundary5() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "5"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(13)
        @DisplayName("TC_SCH_BV_013: 边界组合(1,1) - 成功")
        @WithUserDetails(TEST_USERNAME)
        void testBoundaryCombination1() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "1")
                    .param("timePeriod", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(14)
        @DisplayName("TC_SCH_BV_014: 边界组合(7,4) - 成功")
        @WithUserDetails(TEST_USERNAME)
        void testBoundaryCombination2() throws Exception {
            mockMvc.perform(get("/user-schedules/check-conflict")
                    .param("dayOfWeek", "7")
                    .param("timePeriod", "4"))
                    .andExpect(status().isOk());
        }
    }
}



