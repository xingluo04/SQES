package com.sqe.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生家长关系实体
 */
@Data
@TableName("student_parent_relation")
public class StudentParentRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long parentUserId;
    private String relationType;
    private Integer isPrimaryContact;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
