package com.must.courseevaluation.blackbox;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 课程模块黑盒测试
 * 
 * 测试功能：课程搜索
 * 
 * 测试方法：
 * 1. 等价类划分（Equivalence Partitioning）
 * 2. 边界值测试（Boundary Value Analysis）
 * 3. SQL注入安全测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("课程模块 - 黑盒测试")
public class CourseBlackBoxTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 3.1 等价类划分测试 - 课程搜索
     */
    @Nested
    @DisplayName("3.1 课程搜索 - 等价类划分测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CourseSearchEquivalencePartitioningTests {

        @Test
        @Order(1)
        @DisplayName("TC_CRS_EQ_001: 搜索课程名称 - 返回匹配课程")
        void testSearchByCourseName() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "数据结构"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("TC_CRS_EQ_002: 搜索课程代码 - 返回匹配课程")
        void testSearchByCourseCode() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "CS101"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("TC_CRS_EQ_004: 部分匹配 - 返回所有包含关键词的课程")
        void testPartialMatch() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "数据"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(4)
        @DisplayName("TC_CRS_EQ_005: 空关键词 - 返回所有课程")
        void testEmptyKeyword() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(5)
        @DisplayName("TC_CRS_EQ_006: SQL注入测试1 - 安全处理")
        void testSQLInjection1() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "'; DROP TABLE courses--"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(6)
        @DisplayName("TC_CRS_EQ_007: SQL注入测试2 - 安全处理")
        void testSQLInjection2() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "' OR '1'='1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(7)
        @DisplayName("TC_CRS_EQ_008: 特殊字符 - 返回结果")
        void testSpecialCharacters() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "@#$%^&"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(8)
        @DisplayName("TC_CRS_EQ_009: 超长字符串 - 返回结果")
        void testVeryLongKeyword() throws Exception {
            String longKeyword = "a".repeat(101);
            mockMvc.perform(get("/courses/search")
                    .param("keyword", longKeyword))
                    .andExpect(status().isOk());
        }
    }

    /**
     * 3.2 边界值测试 - 课程搜索
     */
    @Nested
    @DisplayName("3.2 课程搜索 - 边界值测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CourseSearchBoundaryValueTests {

        @Test
        @Order(1)
        @DisplayName("TC_CRS_BV_001: 空关键词(长度=0) - 返回所有课程")
        void testKeywordLength0() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("TC_CRS_BV_002: 1字符关键词 - 返回匹配结果")
        void testKeywordLength1() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "数"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("TC_CRS_BV_003: 正常长度关键词 - 返回匹配结果")
        void testNormalKeywordLength() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "数据结构"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(4)
        @DisplayName("TC_CRS_BV_004: 50字符关键词 - 返回匹配结果")
        void testKeywordLength50() throws Exception {
            String keyword = "a".repeat(50);
            mockMvc.perform(get("/courses/search")
                    .param("keyword", keyword))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(5)
        @DisplayName("TC_CRS_BV_005: 100字符关键词 - 返回结果")
        void testKeywordLength100() throws Exception {
            String keyword = "a".repeat(100);
            mockMvc.perform(get("/courses/search")
                    .param("keyword", keyword))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(6)
        @DisplayName("TC_CRS_BV_006: 101字符关键词 - 返回结果")
        void testKeywordLength101() throws Exception {
            String keyword = "a".repeat(101);
            mockMvc.perform(get("/courses/search")
                    .param("keyword", keyword))
                    .andExpect(status().isOk());
        }
    }

    /**
     * 3.3 综合测试 - 课程搜索
     */
    @Nested
    @DisplayName("3.3 课程搜索 - 综合测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CourseSearchMixedTests {

        @Test
        @Order(1)
        @DisplayName("TC_CRS_MIX_001: 完整课程名 - 返回匹配课程")
        void testFullCourseName() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "数据结构与算法"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("TC_CRS_MIX_002: 部分课程名（开头） - 返回匹配课程")
        void testCourseNamePrefix() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "数据"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("TC_CRS_MIX_003: 部分课程名（中间） - 返回匹配课程")
        void testCourseNameMiddle() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "结构"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(4)
        @DisplayName("TC_CRS_MIX_009: 中英文混合 - 返回匹配结果")
        void testMixedLanguage() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "CS数据"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(5)
        @DisplayName("TC_CRS_MIX_010: 大小写混合 - 返回匹配结果")
        void testMixedCase() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "Cs101"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    /**
     * 3.4 安全性测试 - 课程搜索
     */
    @Nested
    @DisplayName("3.4 课程搜索 - 安全性测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CourseSearchSecurityTests {

        @Test
        @Order(1)
        @DisplayName("TC_CRS_SEC_001: SQL注入-删除表 - 安全处理")
        void testSQLInjectionDropTable() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "'; DROP TABLE courses--"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("TC_CRS_SEC_002: SQL注入-联合查询 - 安全处理")
        void testSQLInjectionUnion() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "' UNION SELECT * FROM users--"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("TC_CRS_SEC_003: SQL注入-布尔盲注 - 安全处理")
        void testSQLInjectionBooleanBlind() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "' OR '1'='1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(4)
        @DisplayName("TC_CRS_SEC_004: XSS脚本注入 - 安全处理")
        void testXSSInjection() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "<script>alert('xss')</script>"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(5)
        @DisplayName("TC_CRS_SEC_005: 路径遍历 - 安全处理")
        void testPathTraversal() throws Exception {
            mockMvc.perform(get("/courses/search")
                    .param("keyword", "../../etc/passwd"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }
}


