package ru.practicum.shareit.item.service;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemMapper;
import ru.practicum.shareit.item.repo.ItemRepo;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImpl implements ItemService {

    UserService userService;
    ItemRepo itemRepository;

    @Override
    public ItemDto createItem(@Valid ItemDto itemDto, long userId) {
        log.info("Creating item {} for user {}", itemDto, userId);
        validateUserExists(userId);

        Item item = ItemMapper.fromDto(itemDto, userId);
        Item stored = this.itemRepository.create(item);
        return ItemMapper.toDto(stored);
    }

    @Override
    public ItemDto updateItem(long itemId, ItemDto itemDto, long userId) {
        log.info("Updating item {} for user {}", itemDto, userId);
        validateUserExists(userId);
        Item existedItem = this.itemRepository.getById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id %s not found".formatted(itemId)));

        if (existedItem.getOwnerId() == null || existedItem.getOwnerId() != userId) {
            throw new NotFoundException("Owner with id %s not found on item, or it is not equal to userId".formatted(itemId));
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existedItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existedItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existedItem.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getRequestId() != null) {
            existedItem.setRequestId(itemDto.getRequestId());
        }

        Item updated = this.itemRepository.update(existedItem);
        return ItemMapper.toDto(updated);
    }

    @Override
    public ItemDto getItemById(long itemId) {
        log.info("Getting item by id {}", itemId);
        return this.itemRepository.getById(itemId).map(ItemMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Item with id %s not found".formatted(itemId)));
    }

    @Override
    public Collection<ItemDto> getItemsByOwner(long userId) {
        log.info("Getting items by owner {}", userId);
        validateUserExists(userId);
        return this.itemRepository.getByOwnerId(userId)
                .stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        log.info("Searching items by text {}", text);

        return this.itemRepository.searchAvailableByText(text)
                .stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    private void validateUserExists(long userId) {
        userService.getUserById(userId);
    }
}