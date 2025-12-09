package com.itheima.controller;

import com.itheima.pojo.PageResult;
import com.itheima.pojo.Result;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        log.info("用户登录：{}", user.getUsername());
        User loginUser = userService.login(user.getUsername(), user.getPassword());
        if (loginUser != null) {
            return Result.success(loginUser);
        }
        return Result.error("用户名或密码错误");
    }

    /**
     * 分页查询用户
     */
    @GetMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer pageSize,
                      String name, String username, Integer role, Integer clazzId) {
        log.info("分页查询用户：page={}, pageSize={}, name={}, username={}, role={}, clazzId={}", page, pageSize, name, username, role, clazzId);
        PageResult<User> pageResult = userService.page(name, username, role, clazzId, page, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result save(@RequestBody User user) {
        log.info("新增用户：{}", user);
        userService.save(user);
        return Result.success();
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        log.info("根据ID查询用户：{}", id);
        User user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result update(@RequestBody User user) {
        log.info("修改用户：{}", user);
        userService.update(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable List<Integer> ids) {
        log.info("批量删除用户：{}", ids);
        userService.delete(ids);
        return Result.success();
    }

    /**
     * 查询所有教师
     */
    @GetMapping("/teachers")
    public Result findAllTeachers() {
        log.info("查询所有教师");
        List<User> teachers = userService.findAllTeachers();
        return Result.success(teachers);
    }

    /**
     * 查询所有学生
     */
    @GetMapping("/students")
    public Result findAllStudents() {
        log.info("查询所有学生");
        List<User> students = userService.findAllStudents();
        return Result.success(students);
    }

    /**
     * 根据班级ID查询学生列表
     */
    @GetMapping("/students/clazz/{clazzId}")
    public Result findStudentsByClazzId(@PathVariable Integer clazzId) {
        log.info("根据班级ID查询学生列表：clazzId={}", clazzId);
        List<User> students = userService.findStudentsByClazzId(clazzId);
        return Result.success(students);
    }

    /**
     * 更新学生班级信息
     */
    @PutMapping("/students/{studentId}/clazz/{clazzId}")
    public Result updateStudentClazz(@PathVariable Integer studentId, @PathVariable Integer clazzId) {
        log.info("更新学生班级信息：studentId={}, clazzId={}", studentId, clazzId);
        userService.updateStudentClazz(studentId, clazzId);
        return Result.success();
    }

    /**
     * 统计班级学生数量
     */
    @GetMapping("/students/clazz/{clazzId}/count")
    public Result countStudentsByClazzId(@PathVariable Integer clazzId) {
        log.info("统计班级学生数量：clazzId={}", clazzId);
        Integer count = userService.countStudentsByClazzId(clazzId);
        return Result.success(count);
    }

    /**
     * 修改密码
     */
    @PutMapping("/changePassword")
    public Result changePassword(@RequestBody User user) {
        log.info("修改密码：userId={}", user.getId());
        userService.changePassword(user);
        return Result.success();
    }
}