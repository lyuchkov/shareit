package ru.practicum.shareit.utils;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.user.dto.UserDto;

@UtilityClass
public class TestDataFactory {

    public static UserDto buildUserDto(String email, String name) {
        return new UserDto(0, email, name);
    }

    public static ItemCreateDto buildItemDto(String name, String description, boolean available) {
        return new ItemCreateDto(null, name, description, available);
    }

    public static ItemUpdateDto buildItemPatch(String name, String description, Boolean available) {
        return new ItemUpdateDto(null, name, description, available);
    }
}