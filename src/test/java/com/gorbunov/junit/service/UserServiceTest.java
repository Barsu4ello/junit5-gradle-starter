package com.gorbunov.junit.service;

import com.gorbunov.junit.TestBase;
import com.gorbunov.junit.dao.UserDao;
import com.gorbunov.junit.dto.User;
import com.gorbunov.junit.extension.*;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("fast") // позволяет группировать тесты по классам и методам, которые нужно запустить, а какие нет.
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// PER_METHOD -это стоит по умолчанию. Создается новый объект класса UserServiceTest для каждого теста.
// Чтобы методы выполнялись по порядку. Надо ставить @Order над методами. Методы без этой аннотации вызовутся в произвольном порядке.
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@TestMethodOrder(MethodOrderer.Random.class) // Всегда разный порядок
//@TestMethodOrder(MethodOrderer.MethodName.class) // В алфавитном порядке
//@TestMethodOrder(MethodOrderer.DisplayName.class) // В алфавитном порядке отображаемых имен методов(по умолчанию = названию методов)
//@TestMethodOrder лучше не использвоать чтобы методы не зависили друг от друга
@ExtendWith({
        UserServiceParameterResolver.class,
        PostProcessingExtension.class,
        ConditionalExtension.class,
        MockitoExtension.class
//        ThrowableExtension.class
//        GlobalExtension.class
})
class UserServiceTest extends TestBase {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");
    private static final User VLAD = User.of(3, "Vlad", "123");
    @InjectMocks // Говорит: внедри сюда mock(объект с аннотацией @Mock или  @Spy)
    private UserService userService;
    @Mock(lenient = true) // = Mockito.mock(UserDao.class)
//    @Spy // = Mockito.spy(new UserDao())
    private UserDao userDao;
    @Captor // Позволяет не создавать ArgumentCaptor вручную.
    private ArgumentCaptor<Integer> argumentCaptor;

    // В JUnit 4 необходим был конструктор без параметров. В JUnit 5 такого ограничения нет.
    UserServiceTest(TestInfo testInfo) {
        System.out.println("Конструктор");
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

//    @BeforeEach
//    void prepare(UserService userService) {
//        System.out.println("Before each: " + this);
//        this.userService = userService;
//    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        // Если все тесты использют одинаковый stub, то можно использолать в @BeforeEach инициализацию этого stub
        // Но что если в каком-то тесте это поведение надо переопределить?
        // Тогда будет ошибка от Mockito Extension. Пример: throwExceptionIfDatabaseIsNotAvailable()
        // Но у нашего userDao можно указать в аннотации @Mock(lenient = true) и тогда ошибка будет игнорироваться. Не ркомендуется + Deprecated
        Mockito.doReturn(true).when(userDao).delete(Mockito.anyInt());
        // lenient = true можно указать и при создании mock вручную
//        this.userDao = Mockito.mock(UserDao.class, Mockito.withSettings().lenient());
        // lenient = true можно указать для конкретного stub. Но только префиксальный вариант. Который не предпочтителен при работе со spy.
//        Mockito.lenient().when(userDao.delete(IVAN.getId())).thenReturn(true);
//        this.userDao = Mockito.mock(UserDao.class); // создание Mock
//        this.userDao = Mockito.spy(new UserDao()); // создание Spy
//        this.userService = new UserService(userDao);
    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable() {
        Mockito.doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());

        assertThrows(RuntimeException.class, () -> userService.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
        // 1 вариант. Более предпочтительный. Универсальный.
//        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());
//        Mockito.doReturn(true).when(userDao).delete(Mockito.anyInt()); // Если все равно какой id кинуть.

        //2 вариант. Не всегда работает. Но позволяет настроить несколько возвратов значений. В первый раз - true, во второй И ПОСЛЕДУЮЩИЕ - false.
        // Не работает со Spy, так как он использует реальный userDao и в части when(userDao.delete(IVAN.getId())) вызывает метод, и мы получаем Exception.
//        Mockito.when(userDao.delete(IVAN.getId()))
//                .thenReturn(true)
//                .thenReturn(false);

        boolean deleteResult = userService.delete(IVAN.getId());
        System.out.println(userService.delete(IVAN.getId()));
        System.out.println(userService.delete(IVAN.getId()));

//        Mockito.verify(userDao, Mockito.times(2)).delete(IVAN.getId()); // Проверяем сколько раз вызывался метод

        // ArgumentCaptor позволяет отследить с какими аргументами был вызван метод mock или spy.
//        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(userDao, Mockito.times(3)).delete(argumentCaptor.capture()); // Проверяем сколько раз вызывался метод
        assertThat(argumentCaptor.getValue()).isEqualTo(1);

        Mockito.reset(userDao); // Используется для сборса количества вызовов мока. Но лучше просто на каждый тест создавать новый Mock. Как мы и делали.

        assertThat(deleteResult).isTrue();
    }


    @Test
    @DisplayName("user will be empty if no user added")
    // Позволяет игнорировать тест. Flaky(чудной, со странностями) тесты - неустойчивые тесты. Разные значения при перезапуске тестов.
    @Disabled("flaky, need to see")
    void usersEmptyIfNoUserAdded() {  // В названиях можно использовать snake case
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();

        MatcherAssert.assertThat(users, IsEmptyCollection.empty()); // hamcrest
        // String или Supplier<String> для описания ошибки. Лямбду если сложная логика для отложенного выполнения
        assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    //    @Test
    @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)
    //RepetitionInfo объект содержащий количество повторений и номер текущего повторения. Возможно, но вряд ли понадобиться
    void usersSizeIfUserAdded(RepetitionInfo repetitionInfo) {
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
    void userConvertedToMapById() throws IOException {
        if (true) {
            throw new RuntimeException();
        }
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

    //Для BDD тестов используется класс BDDMockito с аналогичными методами.
//    @Test
//    void bddTest() {
//        BDDMockito.given(userDao.delete(IVAN.getId())).willReturn(true);
//
//        BDDMockito.willReturn(true).given(userDao).delete(IVAN.getId());
//    }

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
//    @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
    class LoginTest {

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

        @Test
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
        @Order(2)
        void loginSuccessIfUserExists() {
            userService.add(IVAN);
            Optional<User> optionalUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

            assertThat(optionalUser).isPresent(); // Библиотека AssertJ
            assertThat(optionalUser.get()).isEqualTo(IVAN);
//        assertTrue(optionalUser.isPresent());
//        assertEquals(IVAN, optionalUser.get());
        }

        @Test
//        @Timeout(value = 200, unit = TimeUnit.MILLISECONDS) // тоже самое, что и через код. Но с помощью аннотации. Можно ставить над классом.
        void checkLoginFunctionalityPerformance() {
//            Optional<User> userOptional = assertTimeout(Duration.ofMillis(200L), () -> {
//                Thread.sleep(300L);
//                return userService.login("dummy", IVAN.getPassword());
//            });
            // тот же функционал что и выше. Но для выполнения выделяется отдельный поток.
            Optional<User> userOptional = assertTimeoutPreemptively(Duration.ofMillis(200L), () -> {
//                Thread.sleep(300L);
                return userService.login("dummy", IVAN.getPassword());
            });
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
        @MethodSource("com.gorbunov.junit.service.UserServiceTest#getArgumentsForLoginTest")
        // самый частый и гибкий вариант
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
