package ru.practicum.shareit.user.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UserExistsException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.utils.TestConstants;
import ru.practicum.shareit.utils.TestDataFactory;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Get: non-existent user throws NotFoundException")
    void shouldThrowWhenGettingNonExistentUser() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(TestConstants.ID_UNKNOWN));
    }

    @Test
    @DisplayName("Get all: returns all saved users")
    void shouldReturnAllUsers() {
        userService.createUser(TestDataFactory.buildUserDto(TestConstants.EMAIL_USER_ALPHA, TestConstants.NAME_USER_ALPHA));
        userService.createUser(TestDataFactory.buildUserDto(TestConstants.EMAIL_USER_BETA, TestConstants.NAME_USER_BETA));

        Collection<UserDto> allUsers = userService.getUsers();

        assertEquals(TestConstants.COUNT_TWO, allUsers.size());
    }

    @Test
    @DisplayName("Create: successfully creates user and assigns ID")
    void shouldCreateUserSuccessfully() {
        UserDto savedUser = userService.createUser(TestDataFactory.buildUserDto(TestConstants.EMAIL_ALEX, TestConstants.NAME_ALEX));

        assertNotNull(savedUser);
        assertEquals(TestConstants.ID_FIRST_EXPECTED, savedUser.getId());
        assertEquals(TestConstants.EMAIL_ALEX, savedUser.getEmail());
        assertEquals(TestConstants.NAME_ALEX, savedUser.getName());
    }

    @Test
    @DisplayName("Delete: removes existing user completely")
    void shouldDeleteExistingUser() {
        UserDto savedUser = userService.createUser(TestDataFactory.buildUserDto(TestConstants.EMAIL_TO_DELETE, TestConstants.NAME_TO_DELETE));

        userService.deleteUserById(savedUser.getId());

        assertThrows(NotFoundException.class, () -> userService.getUserById(savedUser.getId()));
    }

    @Test
    @DisplayName("Create: duplicate email throws UserExistsException")
    void shouldThrowOnDuplicateEmail() {
        userService.createUser(TestDataFactory.buildUserDto(TestConstants.EMAIL_SHARED, TestConstants.NAME_CLONE_ONE));

        assertThrows(UserExistsException.class, () -> userService.createUser(TestDataFactory.buildUserDto(TestConstants.EMAIL_SHARED, TestConstants.NAME_CLONE_TWO)));
    }

    @Test
    @DisplayName("Update: unknown user throws NotFoundException")
    void shouldThrowWhenUpdatingNonExistentUser() {
        UserDto updateRequest = TestDataFactory.buildUserDto(TestConstants.EMAIL_FRESH, TestConstants.NAME_FRESH);

        assertThrows(NotFoundException.class, () -> userService.updateUser(TestConstants.ID_UNKNOWN, updateRequest));
    }

    @Test
    @DisplayName("Update: updates only specified fields")
    void shouldUpdateOnlyProvidedUserFields() {
        UserDto initialUser = userService.createUser(TestDataFactory.buildUserDto(TestConstants.EMAIL_ANNA, TestConstants.NAME_ANNA));

        UserDto patchedUser = userService.updateUser(initialUser.getId(), TestDataFactory.buildUserDto(null, TestConstants.NAME_ANNA_NEW));

        assertEquals(initialUser.getId(), patchedUser.getId());
        assertEquals(TestConstants.EMAIL_ANNA, patchedUser.getEmail());
        assertEquals(TestConstants.NAME_ANNA_NEW, patchedUser.getName());
    }

    @Test
    @DisplayName("Create: blank email throws ValidationException")
    void shouldFailCreatingUserWithBlankEmail() {
        UserDto invalidDto = TestDataFactory.buildUserDto(TestConstants.STR_EMPTY, TestConstants.NAME_ALEX);

        assertThrows(ValidationException.class, () -> userService.createUser(invalidDto));
    }
}