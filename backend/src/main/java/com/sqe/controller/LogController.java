package com.sqe.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqe.common.Result;
import com.sqe.entity.SysLog;
import com.sqe.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/log")
public class LogController {

    @Autowired
    private LogService logService;

    /* 分页查询日志 */
    @GetMapping("/page")
    public Result<Page<SysLog>> page(@RequestParam(defaultValue = "1") int current,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String module,
                                      @RequestParam(required = false) String result,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(logService.pageList(current, size, keyword, module, result, startTime, endTime));
    }

    /* 按时间清理日志 */
    @DeleteMapping("/clean")
    public Result<?> clean(@RequestParam
                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        logService.cleanBefore(beforeTime);
        return Result.success();
    }
}
