-- 基础角色与演示账号数据
USE student_quality_evaluation;

INSERT IGNORE INTO sys_role (role_code, role_name, description, status) VALUES
('admin', '管理员', '系统管理员', 1),
('teacher', '教师', '教师用户', 1),
('student', '学生', '学生用户', 1),
('parent', '家长', '家长用户', 1);

-- 密码为 123456。建议生产环境初始化后立即修改。
INSERT IGNORE INTO sys_user (username, password, real_name, role, phone, email, gender, status) VALUES
('admin', '$2a$10$0pfkOW1uL9d2wZz5gshhgeoaUdE8EFQzO2m/lTfXq.Pn41AiHSeZq', '系统管理员', 'admin', '13800000000', 'admin@example.com', 0, 1),
('teacher2201', '$2a$10$0pfkOW1uL9d2wZz5gshhgeoaUdE8EFQzO2m/lTfXq.Pn41AiHSeZq', '测试教师', 'teacher', '13800000001', 'teacher@example.com', 1, 1),
('2200770128', '$2a$10$0pfkOW1uL9d2wZz5gshhgeoaUdE8EFQzO2m/lTfXq.Pn41AiHSeZq', '测试学生', 'student', '13800000002', 'student@example.com', 1, 1),
('parent1', '$2a$10$0pfkOW1uL9d2wZz5gshhgeoaUdE8EFQzO2m/lTfXq.Pn41AiHSeZq', '测试家长', 'parent', '13800000003', 'parent@example.com', 2, 1);

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.role_code = u.role;

INSERT IGNORE INTO class_info (class_name, grade, department, teacher_id)
SELECT '软件2201班', '2022级', '软件工程学院', u.id
FROM sys_user u
WHERE u.username = 'teacher2201';

INSERT IGNORE INTO student_info (user_id, student_no, class_id, parent_id, enrollment_year)
SELECT su.id, '2200770128', ci.id, pu.id, 2022
FROM sys_user su
JOIN class_info ci ON ci.class_name = '软件2201班'
JOIN sys_user pu ON pu.username = 'parent1'
WHERE su.username = '2200770128';

INSERT IGNORE INTO student_parent_relation (student_id, parent_user_id, relation_type, is_primary_contact)
SELECT si.id, pu.id, 'guardian', 1
FROM student_info si
JOIN sys_user pu ON pu.username = 'parent1'
WHERE si.student_no = '2200770128';
