package com.sqe.dto;

import lombok.Data;

/**
 * 当前用户资料更新参数
 */
@Data
public class ProfileUpdateDTO {
    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private String avatar;
}
