package com.gorbunov.junit.service;

import com.gorbunov.junit.dao.UserDao;
import com.gorbunov.junit.dto.User;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserService {

    private final List<User> users = new ArrayList<>();
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean delete(Integer userId) {
        return userDao.delete(userId);
    }

    public List<User> getAll() {

        return users;
    }

    public void add(User... users) {
        Collections.addAll(this.users, users);
    }

    public Optional<User> login(String username, String password) {
        if(username == null || password == null) {
            throw  new IllegalArgumentException("Username or password is null");
        }

        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .filter(user -> user.getPassword().equals(password))
                .findFirst();
    }

    public Map<Integer, User> getAllConvertedById() {
        return users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
