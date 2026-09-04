package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    Long id;

    @NotBlank(message = "Item name must not be blank")
    String name;

    @NotBlank(message = "Item description must not be blank")
    String description;

    @NotNull(message = "Item availability must be specified")
    Boolean available;

    Long ownerId;
    Long requestId;
}