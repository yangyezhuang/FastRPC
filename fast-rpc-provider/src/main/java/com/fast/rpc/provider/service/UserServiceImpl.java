package com.fast.rpc.provider.service;

import com.fast.rpc.pojo.User;
import com.fast.rpc.provider.anno.RpcService;
import com.fast.rpc.api.IUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RpcService
@Service
public class UserServiceImpl implements IUserService {
    private static final Map<Integer, User> userMap = new HashMap();

    // 构造数据
    static {
        userMap.put(1, new User(1, "tom"));
        userMap.put(2, new User(2, "alex"));
        userMap.put(3, new User(3, "mimi"));
    }

    @Override
    public User getById(Integer id) {
        return userMap.get(id);
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        for (Map.Entry<Integer, User> entry : userMap.entrySet()) {
            users.add(entry.getValue());
        }
        return users;
    }
}
