package com.must.courseevaluation.whitebox;

import com.must.courseevaluation.dto.UserScheduleDto;
import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.exception.ResourceNotFoundException;
import com.must.courseevaluation.repository.UserRepository;
import com.must.courseevaluation.repository.UserScheduleRepository;
import com.must.courseevaluation.service.UserScheduleService;
import com.must.courseevaluation.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 时间表服务模块白盒测试
 * 
 * 测试功能：
 * 1. 时间冲突检测 (hasTimeConflict方法)
 * 2. 添加时间表 (addUserSchedule方法)
 * 
 * 源代码分析：
 * 
 * hasTimeConflict方法:
 * - 无判定语句，简单方法
 * - 环路复杂度: V(G) = 1
 * 
 * addUserSchedule方法:
 * - 判定D1: 用户是否存在
 * - 判定D2: 时间是否冲突
 * - 环路复杂度: V(G) = 3
 * 
 * 测试方法：
 * 1. 逻辑覆盖法
 *    - 语句覆盖
 *    - 判定覆盖
 * 2. 基本路径法
 *    - 独立路径识别
 *    - 路径覆盖测试
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("时间表服务 - 白盒测试")
public class ScheduleServiceWhiteBoxTests {

    @Autowired
    private UserScheduleService userScheduleService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserScheduleRepository userScheduleRepository;

    /**
     * ==================================================
     * 一、时间冲突检测功能白盒测试
     * 
     * 方法：hasTimeConflict(Long userId, Integer dayOfWeek, Integer timePeriod)
     * 特点：无判定语句的简单方法
     * 环路复杂度：V(G) = 1
     * ==================================================
     */

    /**
     * 1.1 语句覆盖测试 - 时间冲突检测
     * 目标：覆盖所有可执行语句（只有一条return语句）
     */
    @Nested
    @DisplayName("1.1 时间冲突检测 - 语句覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class HasTimeConflictStatementCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_SC_ST_001: 语句覆盖 - 有冲突的情况")
        @Transactional
        void testStatementCoverage_HasConflict() {
            // 准备 - 创建用户并添加时间表
            RegisterRequest req = new RegisterRequest();
            req.setUsername("scuser001");
            req.setPassword("password123");
            req.setEmail("sc001@student.must.edu.mo");
            var user = userService.register(req);

            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(1);
            scheduleDto.setTimePeriod(1);
            scheduleDto.setCourseName("测试课程");
            userScheduleService.addUserSchedule(user.getId(), scheduleDto);

            // 测试 - 检查冲突
            boolean hasConflict = userScheduleService.hasTimeConflict(user.getId(), 1, 1);

            // 验证
            assertTrue(hasConflict);  // 应该有冲突

            // 清理
            userScheduleRepository.deleteByUserId(user.getId());
            userRepository.deleteById(user.getId());
        }
    }

    /**
     * 1.2 等价类测试 - 时间冲突检测
     * 测试两种等价类：有冲突 / 无冲突
     */
    @Nested
    @DisplayName("1.2 时间冲突检测 - 等价类测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class HasTimeConflictEquivalenceTests {

        @Test
        @Order(1)
        @DisplayName("TC_SC_EQ_001: 有冲突 - 返回true")
        @Transactional
        void testHasConflict_ReturnsTrue() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("equser001");
            req.setPassword("password123");
            req.setEmail("eq001@student.must.edu.mo");
            var user = userService.register(req);

            UserScheduleDto schedule = new UserScheduleDto();
            schedule.setDayOfWeek(2);
            schedule.setTimePeriod(3);
            schedule.setCourseName("数据结构");
            userScheduleService.addUserSchedule(user.getId(), schedule);

            // 测试
            boolean result = userScheduleService.hasTimeConflict(user.getId(), 2, 3);

            // 验证
            assertTrue(result);

            // 清理
            userScheduleRepository.deleteByUserId(user.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(2)
        @DisplayName("TC_SC_EQ_002: 无冲突 - 返回false")
        @Transactional
        void testNoConflict_ReturnsFalse() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("equser002");
            req.setPassword("password123");
            req.setEmail("eq002@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - 检查一个没有课程的时间段
            boolean result = userScheduleService.hasTimeConflict(user.getId(), 5, 4);

            // 验证
            assertFalse(result);  // 应该没有冲突

            // 清理
            userRepository.deleteById(user.getId());
        }
    }

    /**
     * ==================================================
     * 二、添加时间表功能白盒测试
     * 
     * 方法：addUserSchedule(Long userId, UserScheduleDto scheduleDto)
     * 判定：
     * - D1: 用户是否存在
     * - D2: 时间是否已有课程
     * 环路复杂度：V(G) = 3
     * ==================================================
     */

    /**
     * 2.1 语句覆盖测试 - 添加时间表
     */
    @Nested
    @DisplayName("2.1 添加时间表 - 语句覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AddScheduleStatementCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_SA_ST_001: 所有语句覆盖 - 添加成功")
        @Transactional
        void testAllStatementsCovered() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("adduser001");
            req.setPassword("password123");
            req.setEmail("add001@student.must.edu.mo");
            var user = userService.register(req);

            // 测试
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(1);
            scheduleDto.setTimePeriod(2);
            scheduleDto.setCourseName("算法设计");

            // 执行
            UserScheduleDto result = userScheduleService.addUserSchedule(user.getId(), scheduleDto);

            // 验证
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(1, result.getDayOfWeek());
            assertEquals(2, result.getTimePeriod());
            assertEquals("算法设计", result.getCourseName());

            // 清理
            userScheduleRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }
    }

    /**
     * 2.2 判定覆盖测试 - 添加时间表
     * 判定D1: 用户存在? (通过异常处理)
     * 判定D2: 时间冲突?
     */
    @Nested
    @DisplayName("2.2 添加时间表 - 判定覆盖测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AddScheduleDecisionCoverageTests {

        @Test
        @Order(1)
        @DisplayName("TC_SA_DC_001: D1=False - 用户不存在")
        @Transactional
        void testD1False_UserNotFound() {
            // 测试 - 使用不存在的用户ID
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(1);
            scheduleDto.setTimePeriod(1);
            scheduleDto.setCourseName("测试课程");

            // 验证 - 应该抛出用户不存在异常
            assertThrows(ResourceNotFoundException.class, () -> {
                userScheduleService.addUserSchedule(99999L, scheduleDto);
            });
        }

        @Test
        @Order(2)
        @DisplayName("TC_SA_DC_002: D1=True, D2=True - 用户存在但时间冲突")
        @Transactional
        void testD1True_D2True_TimeConflict() {
            // 准备 - 创建用户并添加一个时间表
            RegisterRequest req = new RegisterRequest();
            req.setUsername("dcuser002");
            req.setPassword("password123");
            req.setEmail("dc002@student.must.edu.mo");
            var user = userService.register(req);

            UserScheduleDto first = new UserScheduleDto();
            first.setDayOfWeek(3);
            first.setTimePeriod(2);
            first.setCourseName("第一门课");
            userScheduleService.addUserSchedule(user.getId(), first);

            // 测试 - 尝试在相同时间添加另一门课
            UserScheduleDto second = new UserScheduleDto();
            second.setDayOfWeek(3);
            second.setTimePeriod(2);
            second.setCourseName("第二门课");

            // 验证 - 应该抛出时间冲突异常
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userScheduleService.addUserSchedule(user.getId(), second);
            });
            assertTrue(exception.getMessage().contains("时间段已有课程安排"));

            // 清理
            userScheduleRepository.deleteByUserId(user.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(3)
        @DisplayName("TC_SA_DC_003: D1=True, D2=False - 添加成功")
        @Transactional
        void testD1True_D2False_AddSuccess() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("dcuser003");
            req.setPassword("password123");
            req.setEmail("dc003@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - 添加到空闲时间段
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(4);
            scheduleDto.setTimePeriod(3);
            scheduleDto.setCourseName("新课程");

            // 执行
            UserScheduleDto result = userScheduleService.addUserSchedule(user.getId(), scheduleDto);

            // 验证
            assertNotNull(result);
            assertEquals("新课程", result.getCourseName());

            // 清理
            userScheduleRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }
    }

    /**
     * 2.3 基本路径测试 - 添加时间表
     * 环路复杂度：V(G) = 3
     * 独立路径：
     * - Path 1: 用户不存在
     * - Path 2: 用户存在，时间冲突
     * - Path 3: 用户存在，无冲突，添加成功
     */
    @Nested
    @DisplayName("2.3 添加时间表 - 基本路径测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AddScheduleBasicPathTests {

        @Test
        @Order(1)
        @DisplayName("TC_SA_BP_001: Path 1 - 用户不存在路径")
        @Transactional
        void testPath1_UserNotFound() {
            // 测试 Path 1: START → 查找用户(不存在) → 异常 → END
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(1);
            scheduleDto.setTimePeriod(1);
            scheduleDto.setCourseName("测试课程");

            // 验证
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                userScheduleService.addUserSchedule(88888L, scheduleDto);
            });
            assertTrue(exception.getMessage().contains("用户不存在"));
        }

        @Test
        @Order(2)
        @DisplayName("TC_SA_BP_002: Path 2 - 用户存在但时间冲突路径")
        @Transactional
        void testPath2_TimeConflict() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("pathuser002");
            req.setPassword("password123");
            req.setEmail("path002@student.must.edu.mo");
            var user = userService.register(req);

            UserScheduleDto first = new UserScheduleDto();
            first.setDayOfWeek(2);
            first.setTimePeriod(1);
            first.setCourseName("已有课程");
            userScheduleService.addUserSchedule(user.getId(), first);

            // 测试 Path 2: START → 查找用户(存在) → 检查冲突(有冲突) → 异常 → END
            UserScheduleDto second = new UserScheduleDto();
            second.setDayOfWeek(2);
            second.setTimePeriod(1);
            second.setCourseName("冲突课程");

            // 验证
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userScheduleService.addUserSchedule(user.getId(), second);
            });
            assertTrue(exception.getMessage().contains("时间段已有课程安排"));

            // 清理
            userScheduleRepository.deleteByUserId(user.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(3)
        @DisplayName("TC_SA_BP_003: Path 3 - 用户存在且无冲突，添加成功路径")
        @Transactional
        void testPath3_AddSuccess() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("pathuser003");
            req.setPassword("password123");
            req.setEmail("path003@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 Path 3: START → 查找用户(存在) → 检查冲突(无冲突) → 创建 → 保存 → END
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(5);
            scheduleDto.setTimePeriod(4);
            scheduleDto.setCourseName("操作系统");

            // 执行
            UserScheduleDto result = userScheduleService.addUserSchedule(user.getId(), scheduleDto);

            // 验证
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(user.getId(), result.getUserId());
            assertEquals(5, result.getDayOfWeek());
            assertEquals(4, result.getTimePeriod());
            assertEquals("操作系统", result.getCourseName());

            // 再次验证：确认时间冲突检测有效
            boolean hasConflict = userScheduleService.hasTimeConflict(user.getId(), 5, 4);
            assertTrue(hasConflict);

            // 清理
            userScheduleRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }
    }

    /**
     * 2.4 边界值测试 - 时间参数
     * 测试dayOfWeek和timePeriod的边界值
     */
    @Nested
    @DisplayName("2.4 添加时间表 - 边界值测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AddScheduleBoundaryValueTests {

        @Test
        @Order(1)
        @DisplayName("TC_SA_BV_001: dayOfWeek最小值(1) - 成功")
        @Transactional
        void testDayOfWeekMin() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("bvuser001");
            req.setPassword("password123");
            req.setEmail("bv001@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - dayOfWeek = 1 (周一)
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(1);
            scheduleDto.setTimePeriod(1);
            scheduleDto.setCourseName("周一课程");

            // 执行
            UserScheduleDto result = userScheduleService.addUserSchedule(user.getId(), scheduleDto);

            // 验证
            assertNotNull(result);
            assertEquals(1, result.getDayOfWeek());

            // 清理
            userScheduleRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(2)
        @DisplayName("TC_SA_BV_002: dayOfWeek最大值(7) - 成功")
        @Transactional
        void testDayOfWeekMax() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("bvuser002");
            req.setPassword("password123");
            req.setEmail("bv002@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - dayOfWeek = 7 (周日)
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(7);
            scheduleDto.setTimePeriod(1);
            scheduleDto.setCourseName("周日课程");

            // 执行
            UserScheduleDto result = userScheduleService.addUserSchedule(user.getId(), scheduleDto);

            // 验证
            assertNotNull(result);
            assertEquals(7, result.getDayOfWeek());

            // 清理
            userScheduleRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(3)
        @DisplayName("TC_SA_BV_003: timePeriod最小值(1) - 成功")
        @Transactional
        void testTimePeriodMin() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("bvuser003");
            req.setPassword("password123");
            req.setEmail("bv003@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - timePeriod = 1 (上午)
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(1);
            scheduleDto.setTimePeriod(1);
            scheduleDto.setCourseName("上午课程");

            // 执行
            UserScheduleDto result = userScheduleService.addUserSchedule(user.getId(), scheduleDto);

            // 验证
            assertNotNull(result);
            assertEquals(1, result.getTimePeriod());

            // 清理
            userScheduleRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(4)
        @DisplayName("TC_SA_BV_004: timePeriod最大值(4) - 成功")
        @Transactional
        void testTimePeriodMax() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("bvuser004");
            req.setPassword("password123");
            req.setEmail("bv004@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - timePeriod = 4 (晚上)
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(1);
            scheduleDto.setTimePeriod(4);
            scheduleDto.setCourseName("晚上课程");

            // 执行
            UserScheduleDto result = userScheduleService.addUserSchedule(user.getId(), scheduleDto);

            // 验证
            assertNotNull(result);
            assertEquals(4, result.getTimePeriod());

            // 清理
            userScheduleRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(5)
        @DisplayName("TC_SA_BV_005: 边界组合(1,1) - 成功")
        @Transactional
        void testBoundaryCombination_11() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("bvuser005");
            req.setPassword("password123");
            req.setEmail("bv005@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - 最小边界组合
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(1);
            scheduleDto.setTimePeriod(1);
            scheduleDto.setCourseName("周一上午");

            // 执行
            UserScheduleDto result = userScheduleService.addUserSchedule(user.getId(), scheduleDto);

            // 验证
            assertNotNull(result);

            // 清理
            userScheduleRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(6)
        @DisplayName("TC_SA_BV_006: 边界组合(7,4) - 成功")
        @Transactional
        void testBoundaryCombination_74() {
            // 准备
            RegisterRequest req = new RegisterRequest();
            req.setUsername("bvuser006");
            req.setPassword("password123");
            req.setEmail("bv006@student.must.edu.mo");
            var user = userService.register(req);

            // 测试 - 最大边界组合
            UserScheduleDto scheduleDto = new UserScheduleDto();
            scheduleDto.setDayOfWeek(7);
            scheduleDto.setTimePeriod(4);
            scheduleDto.setCourseName("周日晚上");

            // 执行
            UserScheduleDto result = userScheduleService.addUserSchedule(user.getId(), scheduleDto);

            // 验证
            assertNotNull(result);
            assertEquals(7, result.getDayOfWeek());
            assertEquals(4, result.getTimePeriod());

            // 清理
            userScheduleRepository.deleteById(result.getId());
            userRepository.deleteById(user.getId());
        }
    }
}



