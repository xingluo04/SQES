package com.sqe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqe.entity.SysLog;
import com.sqe.mapper.SysLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * 操作日志服务
 */
@Service
public class LogService extends ServiceImpl<SysLogMapper, SysLog> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void ensureLogColumns() {
        addColumnIfMissing("module", "VARCHAR(50) NULL");
        addColumnIfMissing("result", "VARCHAR(20) NULL");
        addColumnIfMissing("error_msg", "VARCHAR(500) NULL");
        addColumnIfMissing("cost_time", "BIGINT NULL");
    }

    /* 分页查询日志 */
    public Page<SysLog> pageList(int current, int size, String keyword, String module,
                                 String result, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysLog::getUsername, keyword)
                    .or().like(SysLog::getOperation, keyword)
                    .or().like(SysLog::getParams, keyword));
        }
        if (StringUtils.hasText(module)) {
            wrapper.eq(SysLog::getModule, module);
        }
        if (StringUtils.hasText(result)) {
            wrapper.eq(SysLog::getResult, result);
        }
        if (startTime != null) {
            wrapper.ge(SysLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(SysLog::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    public boolean cleanBefore(LocalDateTime beforeTime) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(SysLog::getCreateTime, beforeTime);
        return this.remove(wrapper);
    }

    private void addColumnIfMissing(String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = 'sys_log' AND column_name = ?",
                Integer.class,
                columnName);
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE sys_log ADD COLUMN " + columnName + " " + definition);
        }
    }
}
