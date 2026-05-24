package com.sqe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 兼容旧库结构的轻量启动迁移
 */
@Service
public class SchemaMigrationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void ensureOptimizedSchema() {
        createRoleTables();
        seedRoles();
        migrateUserRoles();
        createStudentParentRelationTable();
        migrateStudentParentRelations();
    }

    private void createRoleTables() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_role (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID'," +
                "role_code VARCHAR(20) NOT NULL UNIQUE COMMENT '角色编码：admin/teacher/student/parent'," +
                "role_name VARCHAR(50) NOT NULL COMMENT '角色名称'," +
                "description VARCHAR(255) COMMENT '角色说明'," +
                "status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用'," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") COMMENT '系统角色表'");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user_role (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID'," +
                "user_id BIGINT NOT NULL COMMENT '用户ID'," +
                "role_id BIGINT NOT NULL COMMENT '角色ID'," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE KEY uk_user_role (user_id, role_id)," +
                "INDEX idx_user (user_id)," +
                "INDEX idx_role (role_id)" +
                ") COMMENT '用户角色关系表'");
    }

    private void seedRoles() {
        jdbcTemplate.update("INSERT IGNORE INTO sys_role (role_code, role_name, description, status) VALUES " +
                "('admin', '管理员', '系统管理员', 1)," +
                "('teacher', '教师', '教师用户', 1)," +
                "('student', '学生', '学生用户', 1)," +
                "('parent', '家长', '家长用户', 1)");
    }

    private void migrateUserRoles() {
        jdbcTemplate.update("INSERT IGNORE INTO sys_user_role (user_id, role_id) " +
                "SELECT u.id, r.id FROM sys_user u " +
                "JOIN sys_role r ON r.role_code = u.role " +
                "WHERE u.role IS NOT NULL AND u.role <> ''");
    }

    private void createStudentParentRelationTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS student_parent_relation (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID'," +
                "student_id BIGINT NOT NULL COMMENT '学生信息ID'," +
                "parent_user_id BIGINT NOT NULL COMMENT '家长用户ID'," +
                "relation_type VARCHAR(20) DEFAULT 'guardian' COMMENT '关系类型：father/mother/guardian/other'," +
                "is_primary_contact TINYINT DEFAULT 0 COMMENT '是否主要联系人：0-否，1-是'," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "UNIQUE KEY uk_student_parent (student_id, parent_user_id)," +
                "INDEX idx_student (student_id)," +
                "INDEX idx_parent_user (parent_user_id)" +
                ") COMMENT '学生家长关系表'");
    }

    private void migrateStudentParentRelations() {
        jdbcTemplate.update("INSERT IGNORE INTO student_parent_relation " +
                "(student_id, parent_user_id, relation_type, is_primary_contact) " +
                "SELECT id, parent_id, 'guardian', 1 FROM student_info WHERE parent_id IS NOT NULL");
    }
}
