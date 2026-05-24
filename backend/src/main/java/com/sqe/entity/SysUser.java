package com.sqe.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户实体
 */
@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String realName;
    private String role;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /* 非数据库字段，用于接收密码参数 */
    @TableField(exist = false)
    private String rawPassword;

    /* 非数据库字段，用于返回或接收多角色 */
    @TableField(exist = false)
    private List<String> roles;
}
