package com.sqe.service;

import com.sqe.dto.PasswordUpdateDTO;
import com.sqe.dto.ProfileUpdateDTO;
import com.sqe.entity.SysUser;
import com.sqe.mapper.SysUserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceProfileTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserService userService = new UserService();

    UserServiceProfileTest() {
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
    }

    @Test
    void updateProfileOnlyUpdatesCurrentUserEditableFields() {
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setRealName("新姓名");
        dto.setGender(0);
        dto.setPhone("13900000000");
        dto.setEmail("new@example.com");
        dto.setAvatar("/uploads/avatar.png");

        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        boolean updated = userService.updateProfile(7L, dto);

        Assertions.assertTrue(updated);
        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(captor.capture());
        SysUser user = captor.getValue();
        Assertions.assertEquals(7L, user.getId());
        Assertions.assertEquals("新姓名", user.getRealName());
        Assertions.assertEquals(0, user.getGender());
        Assertions.assertEquals("13900000000", user.getPhone());
        Assertions.assertEquals("new@example.com", user.getEmail());
        Assertions.assertEquals("/uploads/avatar.png", user.getAvatar());
        Assertions.assertNull(user.getRole());
        Assertions.assertNull(user.getStatus());
        Assertions.assertNull(user.getPassword());
    }

    @Test
    void updatePasswordRejectsWrongOldPasswordWithoutSaving() {
        SysUser existing = new SysUser();
        existing.setId(7L);
        existing.setPassword(passwordEncoder.encode("old-pass"));

        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("wrong-pass");
        dto.setNewPassword("new-pass");
        dto.setConfirmPassword("new-pass");

        when(userMapper.selectById(7L)).thenReturn(existing);

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.updatePassword(7L, dto));

        Assertions.assertEquals("旧密码错误", ex.getMessage());
        verify(userMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void updatePasswordSavesEncodedNewPasswordWhenOldPasswordMatches() {
        SysUser existing = new SysUser();
        existing.setId(7L);
        existing.setPassword(passwordEncoder.encode("old-pass"));

        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("old-pass");
        dto.setNewPassword("new-pass");
        dto.setConfirmPassword("new-pass");

        when(userMapper.selectById(7L)).thenReturn(existing);
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        boolean updated = userService.updatePassword(7L, dto);

        Assertions.assertTrue(updated);
        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(captor.capture());
        SysUser saved = captor.getValue();
        Assertions.assertEquals(7L, saved.getId());
        Assertions.assertTrue(passwordEncoder.matches("new-pass", saved.getPassword()));
        Assertions.assertFalse(passwordEncoder.matches("old-pass", saved.getPassword()));
    }
}
