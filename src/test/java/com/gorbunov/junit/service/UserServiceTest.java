package com.gorbunov.junit.service;

import com.gorbunov.junit.dto.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class UserServiceTest {

    @Test
    void usersEmptyIfNoUserAdded() {  // В названиях можно использовать snake case
        UserService userService = new UserService();
        List<User> users = userService.getAll();
        assertFalse(users.isEmpty(), () -> "User list shouldn't be empty"); // String или Supplier<String> для описания ошибки
    }
}
