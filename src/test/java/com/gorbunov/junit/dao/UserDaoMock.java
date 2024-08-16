package com.gorbunov.junit.dao;

import org.mockito.stubbing.Answer1;

import java.util.HashMap;
import java.util.Map;

// Пример как создаются моки в Mockito
public class UserDaoMock extends UserDao{

    private Map<Integer, Boolean> answers = new HashMap<>();
//    private Answer1<Integer, Boolean> answers1; // Этот объект использует Mockito вместо Map.

    @Override
    public boolean delete(Integer userId) {
        return answers.getOrDefault(userId, false);
    }
}
