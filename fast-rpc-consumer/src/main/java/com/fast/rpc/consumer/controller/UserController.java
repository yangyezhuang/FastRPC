package com.fast.rpc.consumer.controller;

import com.fast.rpc.api.IUserService;
import com.fast.rpc.consumer.anno.RpcReference;
import com.fast.rpc.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户控制类
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @RpcReference
    IUserService userService;

    @RequestMapping("/{id}")
    public User getUserById(@PathVariable("id") int id) {
        return userService.getById(id);
    }


    @GetMapping("/list")
    public List<User> getAllUsers() {
        return userService.getAll();
    }
}
