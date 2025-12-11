package com.must.courseevaluation.unit;

import com.must.courseevaluation.service.ContentFilterService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContentFilterService 单元测试
 * 测试敏感词检测和内容过滤逻辑
 */
@DisplayName("ContentFilterService 单元测试")
class ContentFilterServiceUnitTest {

    private ContentFilterService contentFilterService;

    @BeforeEach
    void setUp() {
        contentFilterService = new ContentFilterService();
    }

    // ==================== containsSensitiveContent() 测试 ====================

    @Nested
    @DisplayName("containsSensitiveContent() 方法测试")
    class ContainsSensitiveContentTests {

        @Test
        @DisplayName("包含中文敏感词 - 返回true")
        void testContainsChineseSensitiveWord() {
            // Given
            String content = "这门课太傻逼了";

            // When
            boolean result = contentFilterService.containsSensitiveContent(content);

            // Then
            assertTrue(result, "包含敏感词应该返回true");
        }

        @Test
        @DisplayName("包含英文敏感词 - 返回true")
        void testContainsEnglishSensitiveWord() {
            // Given
            String content = "This course is shit";

            // When
            boolean result = contentFilterService.containsSensitiveContent(content);

            // Then
            assertTrue(result, "包含英文敏感词应该返回true");
        }

        @Test
        @DisplayName("不包含敏感词 - 返回false")
        void testNoSensitiveContent() {
            // Given
            String content = "这是一门很好的课程，老师讲得很清楚";

            // When
            boolean result = contentFilterService.containsSensitiveContent(content);

            // Then
            assertFalse(result, "不包含敏感词应该返回false");
        }

        @Test
        @DisplayName("空内容 - 返回false")
        void testEmptyContent() {
            // When
            boolean resultEmpty = contentFilterService.containsSensitiveContent("");
            boolean resultNull = contentFilterService.containsSensitiveContent(null);

            // Then
            assertFalse(resultEmpty, "空字符串应该返回false");
            assertFalse(resultNull, "null应该返回false");
        }

        @Test
        @DisplayName("大小写混合敏感词 - 返回true")
        void testMixedCaseSensitiveWord() {
            // Given
            String content1 = "This is SHIT";
            String content2 = "This is Shit";
            String content3 = "This is sHiT";

            // When & Then
            assertTrue(contentFilterService.containsSensitiveContent(content1), "全大写敏感词应该被检测");
            assertTrue(contentFilterService.containsSensitiveContent(content2), "首字母大写敏感词应该被检测");
            assertTrue(contentFilterService.containsSensitiveContent(content3), "混合大小写敏感词应该被检测");
        }

        @Test
        @DisplayName("敏感词在句子中间")
        void testSensitiveWordInMiddle() {
            // Given
            String content = "课程内容真的很垃圾，浪费时间";

            // When
            boolean result = contentFilterService.containsSensitiveContent(content);

            // Then
            assertTrue(result, "句子中间的敏感词应该被检测");
        }

        @Test
        @DisplayName("多个敏感词")
        void testMultipleSensitiveWords() {
            // Given
            String content = "傻逼老师，课程真垃圾";

            // When
            boolean result = contentFilterService.containsSensitiveContent(content);

            // Then
            assertTrue(result, "包含多个敏感词应该返回true");
        }

        @Test
        @DisplayName("缩写敏感词 - sb")
        void testAbbreviatedSensitiveWord() {
            // Given
            String content = "这个课程是sb课程";

            // When
            boolean result = contentFilterService.containsSensitiveContent(content);

            // Then
            assertTrue(result, "缩写敏感词应该被检测");
        }
    }

    // ==================== filterContent() 测试 ====================

    @Nested
    @DisplayName("filterContent() 方法测试")
    class FilterContentTests {

        @Test
        @DisplayName("过滤中文敏感词 - 替换为星号")
        void testFilterChineseSensitiveWord() {
            // Given
            String content = "这门课太傻逼了";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertFalse(result.contains("傻逼"), "敏感词应该被过滤");
            assertTrue(result.contains("**"), "敏感词应该被替换为星号");
            assertEquals("这门课太**了", result);
        }

        @Test
        @DisplayName("过滤英文敏感词 - 替换为星号")
        void testFilterEnglishSensitiveWord() {
            // Given
            String content = "This course is shit";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertFalse(result.contains("shit"), "敏感词应该被过滤");
            assertTrue(result.contains("****"), "4字母敏感词应该替换为4个星号");
        }

        @Test
        @DisplayName("无敏感词 - 内容不变")
        void testFilterNoSensitiveContent() {
            // Given
            String content = "这是一门很好的课程";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertEquals(content, result, "无敏感词时内容应该保持不变");
        }

        @Test
        @DisplayName("空内容 - 返回原值")
        void testFilterEmptyContent() {
            // When
            String resultEmpty = contentFilterService.filterContent("");
            String resultNull = contentFilterService.filterContent(null);

            // Then
            assertEquals("", resultEmpty, "空字符串应该返回空字符串");
            assertNull(resultNull, "null应该返回null");
        }

        @Test
        @DisplayName("过滤多个敏感词")
        void testFilterMultipleSensitiveWords() {
            // Given
            String content = "傻逼老师讲的垃圾课程";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertFalse(result.contains("傻逼"), "第一个敏感词应该被过滤");
            assertFalse(result.contains("垃圾"), "第二个敏感词应该被过滤");
            assertTrue(result.contains("**老师讲的**课程") || result.contains("**老师讲的**课程"));
        }

        @Test
        @DisplayName("大小写混合敏感词过滤")
        void testFilterMixedCaseSensitiveWord() {
            // Given
            String content = "This is FUCK you";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertFalse(result.toUpperCase().contains("FUCK"), "大写敏感词应该被过滤");
            assertTrue(result.contains("****"), "敏感词应该被替换为星号");
        }

        @Test
        @DisplayName("敏感词替换长度正确")
        void testFilterReplacementLength() {
            // Given - 测试不同长度的敏感词
            String content1 = "sb"; // 2字符
            String content2 = "shit"; // 4字符
            String content3 = "asshole"; // 7字符

            // When
            String result1 = contentFilterService.filterContent(content1);
            String result2 = contentFilterService.filterContent(content2);
            String result3 = contentFilterService.filterContent(content3);

            // Then
            assertEquals("**", result1, "2字符敏感词应该替换为2个星号");
            assertEquals("****", result2, "4字符敏感词应该替换为4个星号");
            assertEquals("*******", result3, "7字符敏感词应该替换为7个星号");
        }

        @Test
        @DisplayName("保留非敏感内容")
        void testFilterPreservesNonSensitiveContent() {
            // Given
            String content = "这门课程的教学质量很高，老师很认真负责";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertEquals(content, result, "没有敏感词时应该完整保留原内容");
        }

        @Test
        @DisplayName("连续的敏感词")
        void testFilterConsecutiveSensitiveWords() {
            // Given
            String content = "傻逼废物";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertFalse(result.contains("傻逼"), "第一个敏感词应该被过滤");
            assertFalse(result.contains("废物"), "第二个敏感词应该被过滤");
            assertEquals("****", result);
        }
    }

    // ==================== 边界情况测试 ====================

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("只有敏感词")
        void testOnlySensitiveWord() {
            // Given
            String content = "傻逼";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertEquals("**", result, "只有敏感词时应该全部替换为星号");
        }

        @Test
        @DisplayName("敏感词在开头")
        void testSensitiveWordAtStart() {
            // Given
            String content = "傻逼这个课程";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertTrue(result.startsWith("**"), "开头的敏感词应该被过滤");
            assertEquals("**这个课程", result);
        }

        @Test
        @DisplayName("敏感词在结尾")
        void testSensitiveWordAtEnd() {
            // Given
            String content = "这个老师是傻逼";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertTrue(result.endsWith("**"), "结尾的敏感词应该被过滤");
            assertEquals("这个老师是**", result);
        }

        @Test
        @DisplayName("特殊字符附近的敏感词")
        void testSensitiveWordNearSpecialChars() {
            // Given
            String content = "课程评价：傻逼！";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertFalse(result.contains("傻逼"));
            assertEquals("课程评价：**！", result);
        }

        @Test
        @DisplayName("数字和敏感词混合")
        void testSensitiveWordWithNumbers() {
            // Given
            String content = "这门课评分1分，太垃圾了";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertTrue(result.contains("1分"), "数字应该保留");
            assertFalse(result.contains("垃圾"), "敏感词应该被过滤");
        }

        @Test
        @DisplayName("长文本中的敏感词")
        void testSensitiveWordInLongText() {
            // Given
            String content = "这是一门计算机科学的基础课程，主要介绍编程语言和算法。" +
                           "老师讲得很清楚，但是作业太多了，真的很垃圾。" +
                           "不过总体来说还是推荐大家选修这门课。";

            // When
            String result = contentFilterService.filterContent(content);

            // Then
            assertFalse(result.contains("垃圾"));
            assertTrue(result.contains("计算机科学"));
            assertTrue(result.contains("推荐大家选修"));
        }
    }
}




