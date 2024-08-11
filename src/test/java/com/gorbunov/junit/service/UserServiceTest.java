package com.gorbunov.junit.service;

import com.gorbunov.junit.dto.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)// PER_METHOD -это стоит по умолчанию. Создается новый обеъект класса UserServiceTest для каждого теста.
class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");
    private static final User VLAD = User.of(3, "Vlad", "123");
    private UserService userService;

    // должен быть static для TestInstance.Lifecycle.PER_METHOD или менять на PER_CLASS
//    @BeforeAll
//    static void init() {
//        System.out.println("Before all");
//    }

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
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

        MatcherAssert.assertThat(users, IsEmptyCollection.empty()); // hamcrest
        assertTrue(users.isEmpty(), () -> "User list shouldn't be empty"); // String или Supplier<String> для описания ошибки
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);

        List<User> users = userService.getAll();

        assertThat(users).hasSize(2); // Библиотека AssertJ
//        assertThat(users).hasSize(2).isEqualTo(userService.getAll()); // Библиотека AssertJ // Можно chaining'ом вызывать несколько проверок
//        assertEquals(2, users.size());
    }

    @Test
    void loginSuccessIfUserExists() {
        userService.add(IVAN);
        Optional<User> optionalUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

        assertThat(optionalUser).isPresent(); // Библиотека AssertJ
        assertThat(optionalUser.get()).isEqualTo(IVAN);
//        assertTrue(optionalUser.isPresent());
//        assertEquals(IVAN, optionalUser.get());
    }

    @Test
    void userConvertedToMapById() {
        userService.add(IVAN, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();

        MatcherAssert.assertThat(users, IsMapContaining.hasKey(IVAN.getId())); // Библиотека Hamcrest. Лучше использовать AssertJ.
        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId())
                        .containsKey(IVAN.getId()),
                () -> assertThat(users).containsKey(IVAN.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );

    }

    @Test
    void loginFailIfPasswordIsNotCorrect() {
        userService.add(IVAN);
        Optional<User> optionalUser = userService.login(IVAN.getUsername(), "incorrect password");

        assertTrue(optionalUser.isEmpty());
    }

    @Test
    void loginFailIfPasswordIsDoesNotExists() {
        userService.add(IVAN);
        Optional<User> optionalUser = userService.login("Not existed user", "password");

        assertTrue(optionalUser.isEmpty());
    }

    @AfterEach
    void deleteDataFromDB() {
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
