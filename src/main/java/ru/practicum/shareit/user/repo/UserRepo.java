package ru.practicum.shareit.user.repo;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepo {
    User create(User user);

    User update(User user);

    Optional<User> getById(long id);

    Optional<User> getByEmail(String email);

    Collection<User> getAll();

    void deleteById(long id);
}