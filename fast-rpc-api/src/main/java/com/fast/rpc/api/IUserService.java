package com.fast.rpc.api;

import com.fast.rpc.pojo.User;

import java.util.List;

/**
 * 用户服务
 */
public interface IUserService {

    /**
     * 根据id查询用户
     *
     * @param id
     * @return
     */
    User getById(Integer id);


    /**
     * 查询全部用户
     *
     * @return
     */
    List<User> getAll();

}
