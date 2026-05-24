-- 数据库优化迁移：角色表、用户角色关系、学生家长关系、年度唯一评价
USE student_quality_evaluation;

-- 角色表与用户角色关系表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(20) NOT NULL UNIQUE COMMENT '角色编码：admin/teacher/student/parent',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色说明',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统角色表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user (user_id),
    INDEX idx_role (role_id)
) COMMENT '用户角色关系表';

INSERT IGNORE INTO sys_role (role_code, role_name, description, status) VALUES
('admin', '管理员', '系统管理员', 1),
('teacher', '教师', '教师用户', 1),
('student', '学生', '学生用户', 1),
('parent', '家长', '家长用户', 1);

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.role_code = u.role
WHERE u.role IS NOT NULL AND u.role <> '';

-- 学生家长关系表
CREATE TABLE IF NOT EXISTS student_parent_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    student_id BIGINT NOT NULL COMMENT '学生信息ID',
    parent_user_id BIGINT NOT NULL COMMENT '家长用户ID',
    relation_type VARCHAR(20) DEFAULT 'guardian' COMMENT '关系类型：father/mother/guardian/other',
    is_primary_contact TINYINT DEFAULT 0 COMMENT '是否主要联系人：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_parent (student_id, parent_user_id),
    INDEX idx_student (student_id),
    INDEX idx_parent_user (parent_user_id)
) COMMENT '学生家长关系表';

INSERT IGNORE INTO student_parent_relation (student_id, parent_user_id, relation_type, is_primary_contact)
SELECT id, parent_id, 'guardian', 1
FROM student_info
WHERE parent_id IS NOT NULL;

-- 补齐日志和聚类指标字段
ALTER TABLE sys_log
    ADD COLUMN IF NOT EXISTS module VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS result VARCHAR(20) NULL,
    ADD COLUMN IF NOT EXISTS error_msg VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS cost_time BIGINT NULL;

ALTER TABLE cluster_result
    ADD COLUMN IF NOT EXISTS davies_bouldin_index DECIMAL(8,4) COMMENT 'Davies-Bouldin指数(越低越好)',
    ADD COLUMN IF NOT EXISTS calinski_harabasz_index DECIMAL(8,4) COMMENT 'Calinski-Harabasz指数(越高越好)';

-- 清理历史重复评价数据，仅保留每个学生每个学年的最新记录
DELETE FROM moral_evaluation
WHERE id NOT IN (
    SELECT keep_id FROM (
        SELECT MAX(id) AS keep_id FROM moral_evaluation GROUP BY student_id, academic_year
    ) t
);
DELETE FROM academic_evaluation
WHERE id NOT IN (
    SELECT keep_id FROM (
        SELECT MAX(id) AS keep_id FROM academic_evaluation GROUP BY student_id, academic_year
    ) t
);
DELETE FROM physical_evaluation
WHERE id NOT IN (
    SELECT keep_id FROM (
        SELECT MAX(id) AS keep_id FROM physical_evaluation GROUP BY student_id, academic_year
    ) t
);
DELETE FROM art_evaluation
WHERE id NOT IN (
    SELECT keep_id FROM (
        SELECT MAX(id) AS keep_id FROM art_evaluation GROUP BY student_id, academic_year
    ) t
);
DELETE FROM practice_evaluation
WHERE id NOT IN (
    SELECT keep_id FROM (
        SELECT MAX(id) AS keep_id FROM practice_evaluation GROUP BY student_id, academic_year
    ) t
);
DELETE FROM cluster_result
WHERE id NOT IN (
    SELECT keep_id FROM (
        SELECT MAX(id) AS keep_id FROM cluster_result GROUP BY academic_year, cluster_label
    ) t
);

-- 唯一约束。若重复数据未清理干净，这些语句会失败，应先检查重复数据。
ALTER TABLE moral_evaluation ADD UNIQUE KEY uk_student_year (student_id, academic_year);
ALTER TABLE academic_evaluation ADD UNIQUE KEY uk_student_year (student_id, academic_year);
ALTER TABLE physical_evaluation ADD UNIQUE KEY uk_student_year (student_id, academic_year);
ALTER TABLE art_evaluation ADD UNIQUE KEY uk_student_year (student_id, academic_year);
ALTER TABLE practice_evaluation ADD UNIQUE KEY uk_student_year (student_id, academic_year);
ALTER TABLE cluster_result ADD UNIQUE KEY uk_year_label (academic_year, cluster_label);
