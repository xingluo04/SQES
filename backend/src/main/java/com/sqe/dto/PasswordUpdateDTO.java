package com.sqe.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 当前用户密码更新参数
 */
@Data
public class PasswordUpdateDTO {
    @NotBlank(message = "请输入旧密码")
    private String oldPassword;

    @NotBlank(message = "请输入新密码")
    private String newPassword;

    @NotBlank(message = "请确认新密码")
    private String confirmPassword;
}
