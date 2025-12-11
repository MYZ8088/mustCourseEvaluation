-- ============================================
-- Test Data for White-Box and Integration Tests
-- ============================================

-- Insert Faculties
INSERT INTO faculties (id, name, description) VALUES 
(1, '信息科技学院', '计算机科学与信息技术'),
(2, '商学院', '工商管理与金融'),
(3, '酒店与旅游管理学院', '酒店管理与旅游');

-- Insert Teachers
INSERT INTO teachers (id, name, title, email, research_field, achievements, faculty_id) VALUES 
(1, '张教授', '教授', 'zhang@must.edu.mo', '人工智能', 'AI领域专家', 1),
(2, '李教授', '副教授', 'li@must.edu.mo', '软件工程', '软件测试专家', 1),
(3, '王教授', '教授', 'wang@must.edu.mo', '数据库', '数据库专家', 1);

-- Insert Courses
INSERT INTO courses (id, code, name, credits, description, type, assessment_criteria, faculty_id, teacher_id) VALUES 
(1, 'CS101', '计算机导论', 3.0, '计算机科学基础课程', 'COMPULSORY', '考试60%+作业40%', 1, 1),
(2, 'CS201', '数据结构', 3.0, '数据结构与算法', 'COMPULSORY', '考试50%+项目50%', 1, 2),
(3, 'CS301', '软件工程', 3.0, '软件开发方法论', 'COMPULSORY', '项目70%+考试30%', 1, 2),
(4, 'CS401', '人工智能', 3.0, 'AI基础与应用', 'ELECTIVE', '项目60%+考试40%', 1, 1),
(5, 'BA101', '管理学原理', 3.0, '管理学基础', 'COMPULSORY', '考试60%+案例40%', 2, 3);


