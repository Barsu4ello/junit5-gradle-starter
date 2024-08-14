package com.gorbunov.junit.service;

import com.gorbunov.junit.dto.User;
import com.gorbunov.junit.paramResolver.UserServiceParameterResolver;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("fast") // позволяет группировать тесты по классам и методам, которые нужно запустить, а какие нет.
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)// PER_METHOD -это стоит по умолчанию. Создается новый объект класса UserServiceTest для каждого теста.
// Чтобы методы выполнялись по порядку. Надо ставить @Order над методами. Методы без этой аннотации вызовутся в произвольном порядке.
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@TestMethodOrder(MethodOrderer.Random.class) // Всегда разный порядок
//@TestMethodOrder(MethodOrderer.MethodName.class) // В алфавитном порядке
//@TestMethodOrder(MethodOrderer.DisplayName.class) // В алфавитном порядке отображаемых имен методов(по умолчанию = названию методов)
//@TestMethodOrder лучше не использвоать чтобы методы не зависили друг от друга
@ExtendWith({
        UserServiceParameterResolver.class
})
class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");
    private static final User VLAD = User.of(3, "Vlad", "123");
    private UserService userService;

    // В JUnit 4 необходим был конструктор без параметров. В JUnit 5 такого ограничения нет.
    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

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
    void prepare(UserService userService) {
        System.out.println("Before each: " + this);
        this.userService = userService;
    }


    @Test
    @DisplayName("user will be empty if no user added")
    void usersEmptyIfNoUserAdded() {  // В названиях можно использовать snake case
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();

        MatcherAssert.assertThat(users, IsEmptyCollection.empty()); // hamcrest
        // String или Supplier<String> для описания ошибки. Лямбду если сложная логика для отложенного выполнения
        assertTrue(users.isEmpty(), () -> "User list should be empty");
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
    @Order(1)
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

    // Позволяет разграничивать логически группы с помощью вложенных классов
    @Nested
    @DisplayName("Test user login functionality")
    @Tag("login")
    class LoginTest {

        @Test
        @Tag("login")
        void loginFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);
            Optional<User> optionalUser = userService.login(IVAN.getUsername(), "incorrect password");

            assertTrue(optionalUser.isEmpty());
        }

        @Test
        @Tag("login")
        void loginFailIfPasswordIsDoesNotExists() {
            userService.add(IVAN);
            Optional<User> optionalUser = userService.login("Not existed user", "password");

            assertTrue(optionalUser.isEmpty());
        }

        @Test
        @Tag("login")
//    @org.junit.Test(expected = IllegalArgumentException.class) // JUnit 4. Старый вариант обработки ошибок в тестах
        void throwExceptionIfUserNameOrPasswordIsNull() {
            // это если писать примитивно
//        try {
//            userService.login(null, "password");
//            fail("Login should throw exception or null username");
//        } catch (IllegalArgumentException ex) {
//            assertTrue(true);
//        }

            // Если использовать функционал JUnit5
            assertThrows(IllegalArgumentException.class, () -> userService.login(null, "password"));

            //Если хотим проверить несколько кейсов
            assertAll(
                    () -> {
                        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.login(null, "password"));
                        assertThat(exception.getMessage()).isEqualTo("Username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login(VLAD.getUsername(), null))
            );
        }


        @Test
        @Tag("login")
        @Order(2)
        void loginSuccessIfUserExists() {
            userService.add(IVAN);
            Optional<User> optionalUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

            assertThat(optionalUser).isPresent(); // Библиотека AssertJ
            assertThat(optionalUser.get()).isEqualTo(IVAN);
//        assertTrue(optionalUser.isPresent());
//        assertEquals(IVAN, optionalUser.get());
        }

        @ParameterizedTest(name = "{arguments} test") // name определяет имена тестов с помощью placeholder'ов
//        @ArgumentsSource()
                //все эти аннотации используются только для 1 параметра: @NullSource @EmptySource @NullAndEmptySource @ValueSource @EnumSource
//        @NullSource // Предоставляет null аргументы в метод
//        @EmptySource // Предоставляет empty аргументы в метод
//        @NullAndEmptySource // Объединяет 2 аннотации выше
//        @ValueSource(strings = {
//                "IVAN", "PETR"
//        })
//        @EnumSource // Только с enum'ами
        @MethodSource("com.gorbunov.junit.service.UserServiceTest#getArgumentsForLoginTest") // самый частый и гибкий вариант
//        В CSV не передать Optional или другие сложные объекты. Только строки и то что из них легко конвертить(int, double)
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
//        @CsvSource({  // то же что и CsvFileSource, только не надо создавать файл. CSV описывается прям в аннотации
//                "Ivan, 123",
//                "Petr,111"
//        })
        @DisplayName("login param test")
        void loginParameterizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, PETR);

            Optional<User> userOptional = userService.login(username, password);
            assertThat(userOptional).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Ivan", "incorrect password", Optional.empty()),
                Arguments.of("incorrect username", "123", Optional.empty())
        );
    }
}
