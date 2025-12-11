package com.must.courseevaluation.unit;

import com.must.courseevaluation.dto.CourseDto;
import com.must.courseevaluation.dto.CourseScheduleDto;
import com.must.courseevaluation.exception.ResourceNotFoundException;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.CourseSchedule;
import com.must.courseevaluation.model.Faculty;
import com.must.courseevaluation.model.Teacher;
import com.must.courseevaluation.repository.*;
import com.must.courseevaluation.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CourseService 单元测试
 * 使用 Mockito 模拟依赖，测试 CourseServiceImpl 的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService 单元测试")
class CourseServiceUnitTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private CourseScheduleRepository courseScheduleRepository;

    @Mock
    private UserScheduleRepository userScheduleRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course testCourse;
    private Faculty testFaculty;
    private Teacher testTeacher;
    private CourseDto testCourseDto;

    @BeforeEach
    void setUp() {
        // 初始化测试院系
        testFaculty = new Faculty();
        testFaculty.setId(1L);
        testFaculty.setName("计算机科学与工程学院");

        // 初始化测试教师
        testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setName("张教授");
        testTeacher.setTitle("教授");
        testTeacher.setFaculty(testFaculty);

        // 初始化测试课程
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCode("CS101");
        testCourse.setName("计算机导论");
        testCourse.setCredits(3.0);
        testCourse.setDescription("计算机科学入门课程");
        testCourse.setType(Course.CourseType.COMPULSORY);
        testCourse.setFaculty(testFaculty);
        testCourse.setTeacher(testTeacher);

        // 初始化测试DTO
        testCourseDto = new CourseDto();
        testCourseDto.setId(1L);
        testCourseDto.setCode("CS101");
        testCourseDto.setName("计算机导论");
        testCourseDto.setCredits(3.0);
        testCourseDto.setDescription("计算机科学入门课程");
        testCourseDto.setType("COMPULSORY");
        testCourseDto.setFacultyId(1L);
        testCourseDto.setTeacherId(1L);
    }

    // ==================== getAllCourses() 测试 ====================

    @Nested
    @DisplayName("getAllCourses() 方法测试")
    class GetAllCoursesTests {

        @Test
        @DisplayName("返回所有课程列表")
        void testGetAllCoursesSuccess() {
            // Given
            Course course2 = new Course();
            course2.setId(2L);
            course2.setCode("CS102");
            course2.setName("数据结构");
            course2.setCredits(4.0);
            course2.setType(Course.CourseType.COMPULSORY);
            course2.setFaculty(testFaculty);

            when(courseRepository.findAllWithRelations()).thenReturn(Arrays.asList(testCourse, course2));

            // When
            List<CourseDto> result = courseService.getAllCourses();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("CS101", result.get(0).getCode());
            assertEquals("CS102", result.get(1).getCode());
            verify(courseRepository).findAllWithRelations();
        }

        @Test
        @DisplayName("无课程 - 返回空列表")
        void testGetAllCoursesEmpty() {
            // Given
            when(courseRepository.findAllWithRelations()).thenReturn(Collections.emptyList());

            // When
            List<CourseDto> result = courseService.getAllCourses();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(courseRepository).findAllWithRelations();
        }
    }

    // ==================== getCourseById() 测试 ====================

    @Nested
    @DisplayName("getCourseById() 方法测试")
    class GetCourseByIdTests {

        @Test
        @DisplayName("成功获取课程")
        void testGetCourseByIdSuccess() {
            // Given
            when(courseRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testCourse));

            // When
            CourseDto result = courseService.getCourseById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("CS101", result.getCode());
            assertEquals("计算机导论", result.getName());
            verify(courseRepository).findByIdWithRelations(1L);
        }

        @Test
        @DisplayName("课程不存在 - 抛出异常")
        void testGetCourseByIdNotFound() {
            // Given
            when(courseRepository.findByIdWithRelations(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> courseService.getCourseById(999L));
            verify(courseRepository).findByIdWithRelations(999L);
        }
    }

    // ==================== getCoursesByFaculty() 测试 ====================

    @Nested
    @DisplayName("getCoursesByFaculty() 方法测试")
    class GetCoursesByFacultyTests {

        @Test
        @DisplayName("成功获取院系课程列表")
        void testGetCoursesByFacultySuccess() {
            // Given
            when(facultyRepository.existsById(1L)).thenReturn(true);
            when(courseRepository.findByFacultyIdWithRelations(1L)).thenReturn(Arrays.asList(testCourse));

            // When
            List<CourseDto> result = courseService.getCoursesByFaculty(1L);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("CS101", result.get(0).getCode());
            verify(facultyRepository).existsById(1L);
            verify(courseRepository).findByFacultyIdWithRelations(1L);
        }

        @Test
        @DisplayName("院系不存在 - 抛出异常")
        void testGetCoursesByFacultyNotFound() {
            // Given
            when(facultyRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> courseService.getCoursesByFaculty(999L));
            verify(facultyRepository).existsById(999L);
            verify(courseRepository, never()).findByFacultyIdWithRelations(anyLong());
        }
    }

    // ==================== getCoursesByTeacher() 测试 ====================

    @Nested
    @DisplayName("getCoursesByTeacher() 方法测试")
    class GetCoursesByTeacherTests {

        @Test
        @DisplayName("成功获取教师课程列表")
        void testGetCoursesByTeacherSuccess() {
            // Given
            when(teacherRepository.existsById(1L)).thenReturn(true);
            when(courseRepository.findByTeacherIdWithRelations(1L)).thenReturn(Arrays.asList(testCourse));

            // When
            List<CourseDto> result = courseService.getCoursesByTeacher(1L);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("计算机导论", result.get(0).getName());
            verify(teacherRepository).existsById(1L);
            verify(courseRepository).findByTeacherIdWithRelations(1L);
        }

        @Test
        @DisplayName("教师不存在 - 抛出异常")
        void testGetCoursesByTeacherNotFound() {
            // Given
            when(teacherRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> courseService.getCoursesByTeacher(999L));
            verify(teacherRepository).existsById(999L);
            verify(courseRepository, never()).findByTeacherIdWithRelations(anyLong());
        }
    }

    // ==================== getCoursesByType() 测试 ====================

    @Nested
    @DisplayName("getCoursesByType() 方法测试")
    class GetCoursesByTypeTests {

        @Test
        @DisplayName("按类型获取课程")
        void testGetCoursesByTypeSuccess() {
            // Given
            when(courseRepository.findByType(Course.CourseType.COMPULSORY)).thenReturn(Arrays.asList(testCourse));

            // When
            List<CourseDto> result = courseService.getCoursesByType(Course.CourseType.COMPULSORY);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("COMPULSORY", result.get(0).getType());
            verify(courseRepository).findByType(Course.CourseType.COMPULSORY);
        }

        @Test
        @DisplayName("无匹配类型课程 - 返回空列表")
        void testGetCoursesByTypeEmpty() {
            // Given
            when(courseRepository.findByType(Course.CourseType.ELECTIVE)).thenReturn(Collections.emptyList());

            // When
            List<CourseDto> result = courseService.getCoursesByType(Course.CourseType.ELECTIVE);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(courseRepository).findByType(Course.CourseType.ELECTIVE);
        }
    }

    // ==================== searchCourses() 测试 ====================

    @Nested
    @DisplayName("searchCourses() 方法测试")
    class SearchCoursesTests {

        @Test
        @DisplayName("关键词搜索 - 有结果")
        void testSearchCoursesWithResults() {
            // Given
            when(courseRepository.findByNameContaining("计算机")).thenReturn(Arrays.asList(testCourse));

            // When
            List<CourseDto> result = courseService.searchCourses("计算机");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getName().contains("计算机"));
            verify(courseRepository).findByNameContaining("计算机");
        }

        @Test
        @DisplayName("关键词搜索 - 无结果")
        void testSearchCoursesNoResults() {
            // Given
            when(courseRepository.findByNameContaining("不存在的课程")).thenReturn(Collections.emptyList());

            // When
            List<CourseDto> result = courseService.searchCourses("不存在的课程");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(courseRepository).findByNameContaining("不存在的课程");
        }
    }

    // ==================== createCourse() 测试 ====================

    @Nested
    @DisplayName("createCourse() 方法测试")
    class CreateCourseTests {

        @Test
        @DisplayName("成功创建课程")
        void testCreateCourseSuccess() {
            // Given
            CourseDto newCourseDto = new CourseDto();
            newCourseDto.setCode("CS201");
            newCourseDto.setName("算法设计");
            newCourseDto.setCredits(3.0);
            newCourseDto.setDescription("算法基础课程");
            newCourseDto.setType("COMPULSORY");
            newCourseDto.setFacultyId(1L);
            newCourseDto.setTeacherId(1L);

            when(courseRepository.existsByCode("CS201")).thenReturn(false);
            when(facultyRepository.findById(1L)).thenReturn(Optional.of(testFaculty));
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
                Course course = invocation.getArgument(0);
                course.setId(2L);
                return course;
            });

            // When
            CourseDto result = courseService.createCourse(newCourseDto);

            // Then
            assertNotNull(result);
            assertEquals("CS201", result.getCode());
            assertEquals("算法设计", result.getName());
            verify(courseRepository).existsByCode("CS201");
            verify(facultyRepository).findById(1L);
            verify(teacherRepository).findById(1L);
            verify(courseRepository).save(any(Course.class));
        }

        @Test
        @DisplayName("课程代码已存在 - 抛出异常")
        void testCreateCourseCodeExists() {
            // Given
            when(courseRepository.existsByCode("CS101")).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> courseService.createCourse(testCourseDto));
            
            assertTrue(exception.getMessage().contains("课程代码已存在"));
            verify(courseRepository).existsByCode("CS101");
            verify(courseRepository, never()).save(any(Course.class));
        }

        @Test
        @DisplayName("院系不存在 - 抛出异常")
        void testCreateCourseFacultyNotFound() {
            // Given
            CourseDto newCourseDto = new CourseDto();
            newCourseDto.setCode("CS201");
            newCourseDto.setName("算法设计");
            newCourseDto.setCredits(3.0);
            newCourseDto.setType("COMPULSORY");
            newCourseDto.setFacultyId(999L);

            when(courseRepository.existsByCode("CS201")).thenReturn(false);
            when(facultyRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> courseService.createCourse(newCourseDto));
            verify(courseRepository, never()).save(any(Course.class));
        }

        @Test
        @DisplayName("创建课程不指定教师")
        void testCreateCourseWithoutTeacher() {
            // Given
            CourseDto newCourseDto = new CourseDto();
            newCourseDto.setCode("CS201");
            newCourseDto.setName("算法设计");
            newCourseDto.setCredits(3.0);
            newCourseDto.setDescription("算法基础课程");
            newCourseDto.setType("COMPULSORY");
            newCourseDto.setFacultyId(1L);
            newCourseDto.setTeacherId(null); // 不指定教师

            when(courseRepository.existsByCode("CS201")).thenReturn(false);
            when(facultyRepository.findById(1L)).thenReturn(Optional.of(testFaculty));
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
                Course course = invocation.getArgument(0);
                course.setId(2L);
                return course;
            });

            // When
            CourseDto result = courseService.createCourse(newCourseDto);

            // Then
            assertNotNull(result);
            verify(teacherRepository, never()).findById(anyLong());
            verify(courseRepository).save(any(Course.class));
        }
    }

    // ==================== updateCourse() 测试 ====================

    @Nested
    @DisplayName("updateCourse() 方法测试")
    class UpdateCourseTests {

        @Test
        @DisplayName("成功更新课程")
        void testUpdateCourseSuccess() {
            // Given
            testCourseDto.setName("计算机导论（更新）");
            
            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(facultyRepository.findById(1L)).thenReturn(Optional.of(testFaculty));
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
            when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

            // When
            CourseDto result = courseService.updateCourse(testCourseDto);

            // Then
            assertNotNull(result);
            verify(courseRepository).findById(1L);
            verify(courseRepository).save(any(Course.class));
        }

        @Test
        @DisplayName("课程不存在 - 抛出异常")
        void testUpdateCourseNotFound() {
            // Given
            testCourseDto.setId(999L);
            when(courseRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> courseService.updateCourse(testCourseDto));
            verify(courseRepository).findById(999L);
            verify(courseRepository, never()).save(any(Course.class));
        }

        @Test
        @DisplayName("更新课程代码 - 新代码已存在")
        void testUpdateCourseCodeConflict() {
            // Given
            testCourseDto.setCode("CS999"); // 改变课程代码
            
            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(courseRepository.existsByCode("CS999")).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> courseService.updateCourse(testCourseDto));
            
            assertTrue(exception.getMessage().contains("课程代码已存在"));
            verify(courseRepository, never()).save(any(Course.class));
        }

        @Test
        @DisplayName("更新课程 - 移除教师")
        void testUpdateCourseRemoveTeacher() {
            // Given
            testCourseDto.setTeacherId(null); // 移除教师
            
            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(facultyRepository.findById(1L)).thenReturn(Optional.of(testFaculty));
            when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

            // When
            CourseDto result = courseService.updateCourse(testCourseDto);

            // Then
            assertNotNull(result);
            verify(teacherRepository, never()).findById(anyLong());
            verify(courseRepository).save(any(Course.class));
        }
    }

    // ==================== deleteCourse() 测试 ====================

    @Nested
    @DisplayName("deleteCourse() 方法测试")
    class DeleteCourseTests {

        @Test
        @DisplayName("成功删除课程")
        void testDeleteCourseSuccess() {
            // Given
            when(courseRepository.existsById(1L)).thenReturn(true);
            doNothing().when(courseRepository).deleteById(1L);

            // When
            courseService.deleteCourse(1L);

            // Then
            verify(courseRepository).existsById(1L);
            verify(courseRepository).deleteById(1L);
        }

        @Test
        @DisplayName("课程不存在 - 抛出异常")
        void testDeleteCourseNotFound() {
            // Given
            when(courseRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> courseService.deleteCourse(999L));
            verify(courseRepository).existsById(999L);
            verify(courseRepository, never()).deleteById(anyLong());
        }
    }

    // ==================== 课程时间表相关测试 ====================

    @Nested
    @DisplayName("课程时间表方法测试")
    class CourseScheduleTests {

        @Test
        @DisplayName("获取课程时间表")
        void testGetCourseSchedules() {
            // Given
            CourseSchedule schedule = new CourseSchedule();
            schedule.setId(1L);
            schedule.setCourse(testCourse);
            schedule.setDayOfWeek(1);
            schedule.setTimePeriod(1);
            schedule.setLocation("A101");

            when(courseScheduleRepository.findByCourseId(1L)).thenReturn(Arrays.asList(schedule));

            // When
            List<CourseScheduleDto> result = courseService.getCourseSchedules(1L);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1, result.get(0).getDayOfWeek());
            assertEquals("A101", result.get(0).getLocation());
            verify(courseScheduleRepository).findByCourseId(1L);
        }

        @Test
        @DisplayName("添加课程时间安排 - 成功")
        void testAddCourseScheduleSuccess() {
            // Given
            CourseScheduleDto scheduleDto = new CourseScheduleDto();
            scheduleDto.setDayOfWeek(2);
            scheduleDto.setTimePeriod(3);
            scheduleDto.setLocation("B202");

            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(courseScheduleRepository.existsByCourseIdAndDayOfWeekAndTimePeriod(1L, 2, 3)).thenReturn(false);
            when(courseScheduleRepository.save(any(CourseSchedule.class))).thenAnswer(invocation -> {
                CourseSchedule schedule = invocation.getArgument(0);
                schedule.setId(1L);
                return schedule;
            });

            // When
            CourseScheduleDto result = courseService.addCourseSchedule(1L, scheduleDto);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getDayOfWeek());
            assertEquals(3, result.getTimePeriod());
            verify(courseScheduleRepository).save(any(CourseSchedule.class));
        }

        @Test
        @DisplayName("添加课程时间安排 - 时间冲突")
        void testAddCourseScheduleConflict() {
            // Given
            CourseScheduleDto scheduleDto = new CourseScheduleDto();
            scheduleDto.setDayOfWeek(1);
            scheduleDto.setTimePeriod(1);
            scheduleDto.setLocation("A101");

            when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
            when(courseScheduleRepository.existsByCourseIdAndDayOfWeekAndTimePeriod(1L, 1, 1)).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> courseService.addCourseSchedule(1L, scheduleDto));
            
            assertTrue(exception.getMessage().contains("该时间段已有课程安排"));
            verify(courseScheduleRepository, never()).save(any(CourseSchedule.class));
        }

        @Test
        @DisplayName("删除课程时间安排 - 成功")
        void testDeleteCourseScheduleSuccess() {
            // Given
            when(courseScheduleRepository.existsById(1L)).thenReturn(true);
            doNothing().when(courseScheduleRepository).deleteById(1L);

            // When
            courseService.deleteCourseSchedule(1L);

            // Then
            verify(courseScheduleRepository).existsById(1L);
            verify(courseScheduleRepository).deleteById(1L);
        }

        @Test
        @DisplayName("删除课程时间安排 - 不存在")
        void testDeleteCourseScheduleNotFound() {
            // Given
            when(courseScheduleRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                () -> courseService.deleteCourseSchedule(999L));
            verify(courseScheduleRepository, never()).deleteById(anyLong());
        }
    }
}

