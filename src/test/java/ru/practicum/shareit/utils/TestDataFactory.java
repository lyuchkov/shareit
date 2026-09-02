package ru.practicum.shareit.utils;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

@UtilityClass
public class TestDataFactory {

    public static UserDto buildUserDto(String email, String name) {
        return new UserDto(0, email, name);
    }

    public static ItemDto buildItemDto(String name, String description, boolean available) {
        return new ItemDto(null, name, description, available, null, null);
    }

    public static ItemDto buildItemPatch(String name, String description, Boolean available, Long requestId) {
        return new ItemDto(null, name, description, available, null, requestId);
    }
}