package com.gorbunov.junit.service;

import com.gorbunov.junit.dto.User;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)// PER_METHOD -это стоит по умолчанию. Создается новый обеъект класса UserServiceTest для каждого теста.
class UserServiceTest {

    private UserService userService;

    // должен быть static для TestInstance.Lifecycle.PER_METHOD или менять на PER_CLASS
//    @BeforeAll
//    static void init() {
//        System.out.println("Before all");
//    }

    @BeforeAll
    void init() {
        System.out.println("Before all: "  + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        userService = new UserService();
    }


    @Test
    void usersEmptyIfNoUserAdded() {  // В названиях можно использовать snake case
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();
        assertTrue(users.isEmpty(), () -> "User list shouldn't be empty"); // String или Supplier<String> для описания ошибки
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(new User());
        userService.add(new User());

        List<User> users = userService.getAll();
        assertEquals(2, users.size());
    }

    @AfterEach
    void deleteDataFromDB(){
        System.out.println("After each: " + this);
    }

//    @AfterAll
//    static void closeConnectionPool() {
//        System.out.println("After all");
//    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all: " + this);
    }
}
