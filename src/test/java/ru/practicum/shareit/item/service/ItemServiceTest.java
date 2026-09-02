package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.utils.TestConstants;
import ru.practicum.shareit.utils.TestDataFactory;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Search: empty query returns empty list")
    void shouldReturnEmptyWhenSearchingBlankText() {
        Collection<ItemDto> searchResults = itemService.searchItems(TestConstants.STR_EMPTY);
        assertEquals(TestConstants.COUNT_ZERO, searchResults.size());
    }

    @Test
    @DisplayName("Create: successful item creation")
    void shouldCreateValidItemSuccessfully() {
        long ownerId = registerUser(TestConstants.EMAIL_OWNER_B, TestConstants.NAME_OWNER_B).getId();
        ItemDto requestDto = TestDataFactory.buildItemDto(TestConstants.ITEM_SCREWDRIVER, TestConstants.DESC_SCREWDRIVER, TestConstants.IS_AVAILABLE);

        ItemDto savedItem = itemService.createItem(requestDto, ownerId);

        assertNotNull(savedItem);
        assertNotNull(savedItem.getId());
        assertEquals(TestConstants.ITEM_SCREWDRIVER, savedItem.getName());
        assertEquals(ownerId, savedItem.getOwnerId());
    }

    @Test
    @DisplayName("Update: partial data updates only non-null fields")
    void shouldUpdateProvidedFieldsOnly() {
        long ownerId = registerUser(TestConstants.EMAIL_OWNER_D, TestConstants.NAME_OWNER_D).getId();
        ItemDto savedItem = itemService.createItem(TestDataFactory.buildItemDto(TestConstants.ITEM_LAPTOP, TestConstants.DESC_GAMING_LAPTOP, TestConstants.IS_AVAILABLE), ownerId);

        ItemDto updatedItem = itemService.updateItem(savedItem.getId(), TestDataFactory.buildItemPatch(TestConstants.ITEM_LAPTOP_PRO, null, null, null), ownerId);

        assertEquals(TestConstants.ITEM_LAPTOP_PRO, updatedItem.getName());
        assertEquals(TestConstants.DESC_GAMING_LAPTOP, updatedItem.getDescription());
        assertEquals(TestConstants.IS_AVAILABLE, updatedItem.getAvailable());
    }

    @Test
    @DisplayName("Create: unknown owner throws NotFoundException")
    void shouldFailToCreateItemWhenOwnerNotFound() {
        ItemDto requestDto = TestDataFactory.buildItemDto(TestConstants.ITEM_SCREWDRIVER, TestConstants.DESC_SCREWDRIVER, TestConstants.IS_AVAILABLE);

        assertThrows(NotFoundException.class, () -> itemService.createItem(requestDto, TestConstants.ID_UNKNOWN));
    }

    @Test
    @DisplayName("Search: finds only available items matching the text")
    void shouldFindAvailableItemsBySearchText() {
        long ownerId = registerUser(TestConstants.EMAIL_OWNER_G, TestConstants.NAME_OWNER_G).getId();
        itemService.createItem(TestDataFactory.buildItemDto(TestConstants.ITEM_SCREWDRIVER, TestConstants.DESC_SCREWDRIVER, TestConstants.IS_AVAILABLE), ownerId);
        itemService.createItem(TestDataFactory.buildItemDto(TestConstants.ITEM_SCREWDRIVER_BROKEN, TestConstants.DESC_BROKEN_TOOL, TestConstants.NOT_AVAILABLE), ownerId);
        itemService.createItem(TestDataFactory.buildItemDto(TestConstants.ITEM_WRENCH, TestConstants.DESC_WRENCH, TestConstants.IS_AVAILABLE), ownerId);

        Collection<ItemDto> searchResults = itemService.searchItems(TestConstants.SEARCH_QUERY_SCREW);

        assertEquals(TestConstants.COUNT_ONE, searchResults.size());
        assertEquals(TestConstants.ITEM_SCREWDRIVER, searchResults.iterator().next().getName());
    }

    @Test
    @DisplayName("Get: non-existent item ID throws NotFoundException")
    void shouldThrowWhenGettingNonExistentItem() {
        assertThrows(NotFoundException.class, () -> itemService.getItemById(TestConstants.ID_UNKNOWN));
    }

    @Test
    @DisplayName("Get by owner: returns correct list of items")
    void shouldReturnOnlyOwnerItems() {
        long firstOwner = registerUser(TestConstants.EMAIL_OWNER_E, TestConstants.NAME_OWNER_E).getId();
        long secondOwner = registerUser(TestConstants.EMAIL_OWNER_F, TestConstants.NAME_OWNER_F).getId();

        itemService.createItem(TestDataFactory.buildItemDto(TestConstants.ITEM_LAPTOP, TestConstants.DESC_GAMING_LAPTOP, TestConstants.IS_AVAILABLE), firstOwner);
        itemService.createItem(TestDataFactory.buildItemDto(TestConstants.ITEM_CAMERA, TestConstants.DESC_DSLR_CAMERA, TestConstants.IS_AVAILABLE), firstOwner);
        itemService.createItem(TestDataFactory.buildItemDto(TestConstants.ITEM_SNOWBOARD, TestConstants.DESC_SNOWBOARD, TestConstants.IS_AVAILABLE), secondOwner);

        Collection<ItemDto> firstOwnerItems = itemService.getItemsByOwner(firstOwner);

        assertEquals(TestConstants.COUNT_TWO, firstOwnerItems.size());
    }

    @Test
    @DisplayName("Update: user is not the owner throws NotFoundException")
    void shouldThrowWhenRequesterNotOwnerOnUpdate() {
        long actualOwnerId = registerUser(TestConstants.EMAIL_OWNER_C, TestConstants.NAME_OWNER_C).getId();
        long strangerId = registerUser(TestConstants.EMAIL_STRANGER, TestConstants.NAME_STRANGER).getId();
        ItemDto savedItem = itemService.createItem(TestDataFactory.buildItemDto(TestConstants.ITEM_LAPTOP, TestConstants.DESC_GAMING_LAPTOP, TestConstants.IS_AVAILABLE), actualOwnerId);

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(savedItem.getId(), TestDataFactory.buildItemPatch(TestConstants.ITEM_UPDATED_NAME, null, null, null), strangerId));
    }

    @Test
    @DisplayName("Create: blank name throws ValidationException")
    void shouldFailCreatingItemWithBlankName() {
        long ownerId = registerUser(TestConstants.EMAIL_OWNER_A, TestConstants.NAME_OWNER_A).getId();
        ItemDto requestDto = TestDataFactory.buildItemDto(TestConstants.STR_EMPTY, TestConstants.DESC_SCREWDRIVER, TestConstants.IS_AVAILABLE);

        assertThrows(ValidationException.class, () -> itemService.createItem(requestDto, ownerId));
    }

    private UserDto registerUser(String email, String name) {
        return userService.createUser(TestDataFactory.buildUserDto(email, name));
    }
}