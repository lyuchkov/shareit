package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.utils.TestConstants;
import ru.practicum.shareit.utils.TestDataFactory;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Search: empty query returns empty list")
    void shouldReturnEmptyWhenSearchingBlankText() {
        var createRequest = TestDataFactory.buildItemDto(
                TestConstants.ITEM_SCREWDRIVER,
                TestConstants.DESC_SCREWDRIVER,
                TestConstants.IS_AVAILABLE
        );

        assertThrows(NotFoundException.class,
                () -> this.itemService.createItem(createRequest, TestConstants.ID_UNKNOWN));
    }

    @Test
    @DisplayName("Update: partial data updates only non-null fields")
    void shouldUpdateProvidedFieldsOnly() {
        long ownerId = this.registerUser(TestConstants.EMAIL_OWNER_B, TestConstants.NAME_OWNER_B).getId();
        var createRequest = TestDataFactory.buildItemDto(
                TestConstants.ITEM_SCREWDRIVER,
                TestConstants.DESC_SCREWDRIVER,
                TestConstants.IS_AVAILABLE
        );

        ItemDto created = this.itemService.createItem(createRequest, ownerId);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(TestConstants.ITEM_SCREWDRIVER, created.getName());
        assertEquals(TestConstants.DESC_SCREWDRIVER, created.getDescription());
        assertTrue(created.getAvailable());
        assertNull(created.getLastBooking());
        assertNull(created.getNextBooking());
        assertNotNull(created.getComments());
    }

    @Test
    @DisplayName("Create: unknown owner throws NotFoundException")
    void shouldFailToCreateItemWhenOwnerNotFound() {
        long ownerId = this.registerUser(TestConstants.EMAIL_OWNER_C, TestConstants.NAME_OWNER_C).getId();
        long otherUserId = this.registerUser(TestConstants.EMAIL_STRANGER, TestConstants.NAME_STRANGER).getId();
        var createRequest = TestDataFactory.buildItemDto(
                TestConstants.ITEM_LAPTOP,
                TestConstants.DESC_GAMING_LAPTOP,
                TestConstants.IS_AVAILABLE
        );
        ItemDto created = this.itemService.createItem(createRequest, ownerId);
        var updateRequest = TestDataFactory.buildItemPatch(TestConstants.ITEM_UPDATED_NAME, null, null);

        assertThrows(NotFoundException.class,
                () -> this.itemService.updateItem(created.getId(), updateRequest, otherUserId));
    }

    @Test
    @DisplayName("Search: finds only available items matching the text")
    void shouldFindAvailableItemsBySearchText() {
        long ownerId = this.registerUser(TestConstants.EMAIL_OWNER_D, TestConstants.NAME_OWNER_D).getId();
        var createRequest = TestDataFactory.buildItemDto(
                TestConstants.ITEM_LAPTOP,
                TestConstants.DESC_GAMING_LAPTOP,
                TestConstants.IS_AVAILABLE
        );
        ItemDto created = this.itemService.createItem(createRequest, ownerId);
        var updateRequest = TestDataFactory.buildItemPatch(TestConstants.ITEM_LAPTOP_PRO, null, null);

        ItemDto updated = this.itemService.updateItem(created.getId(), updateRequest, ownerId);

        assertEquals(TestConstants.ITEM_LAPTOP_PRO, updated.getName());
        assertEquals(TestConstants.DESC_GAMING_LAPTOP, updated.getDescription());
        assertTrue(updated.getAvailable());
    }

    @Test
    @DisplayName("Get: non-existent item ID throws NotFoundException")
    void shouldThrowWhenGettingNonExistentItem() {
        assertThrows(NotFoundException.class, () -> this.itemService.getItemById(TestConstants.ID_UNKNOWN));
    }

    @Test
    @DisplayName("Get by owner: returns correct list of items")
    void shouldReturnOnlyOwnerItems() {
        long owner1 = this.registerUser(TestConstants.EMAIL_OWNER_E, TestConstants.NAME_OWNER_E).getId();
        long owner2 = this.registerUser(TestConstants.EMAIL_OWNER_F, TestConstants.NAME_OWNER_F).getId();

        this.itemService.createItem(
                TestDataFactory.buildItemDto(
                        TestConstants.ITEM_LAPTOP,
                        TestConstants.DESC_GAMING_LAPTOP,
                        TestConstants.IS_AVAILABLE),
                owner1
        );
        this.itemService.createItem(
                TestDataFactory.buildItemDto(
                        TestConstants.ITEM_CAMERA,
                        TestConstants.DESC_DSLR_CAMERA,
                        TestConstants.IS_AVAILABLE),
                owner1
        );
        this.itemService.createItem(
                TestDataFactory.buildItemDto(
                        TestConstants.ITEM_SNOWBOARD,
                        TestConstants.DESC_SNOWBOARD,
                        TestConstants.IS_AVAILABLE),
                owner2
        );

        Collection<ItemDto> owner1Items = this.itemService.getItemsByOwner(owner1);

        assertEquals(TestConstants.COUNT_TWO, owner1Items.size());
    }

    @Test
    @DisplayName("Update: user is not the owner throws NotFoundException")
    void shouldThrowWhenRequesterNotOwnerOnUpdate() {
        Collection<ItemDto> found = this.itemService.searchItems(TestConstants.STR_EMPTY);

        assertEquals(TestConstants.COUNT_ZERO, found.size());
    }

    @Test
    @DisplayName("Create: blank name throws ValidationException")
    void shouldFailCreatingItemWithBlankName() {
        long ownerId = this.registerUser(TestConstants.EMAIL_OWNER_G, TestConstants.NAME_OWNER_G).getId();

        this.itemService.createItem(
                TestDataFactory.buildItemDto(
                        TestConstants.ITEM_SCREWDRIVER,
                        TestConstants.DESC_SCREWDRIVER,
                        TestConstants.IS_AVAILABLE),
                ownerId
        );
        this.itemService.createItem(
                TestDataFactory.buildItemDto(
                        TestConstants.ITEM_SCREWDRIVER_BROKEN,
                        TestConstants.DESC_BROKEN_TOOL,
                        TestConstants.NOT_AVAILABLE),
                ownerId
        );
        this.itemService.createItem(
                TestDataFactory.buildItemDto(
                        TestConstants.ITEM_WRENCH,
                        TestConstants.DESC_WRENCH,
                        TestConstants.IS_AVAILABLE),
                ownerId
        );

        Collection<ItemDto> found = this.itemService.searchItems(TestConstants.SEARCH_QUERY_SCREW);

        assertEquals(TestConstants.COUNT_ONE, found.size());
        ItemDto only = found.iterator().next();
        assertEquals(TestConstants.ITEM_SCREWDRIVER, only.getName());
    }

    private UserDto registerUser(String email, String name) {
        return userService.createUser(TestDataFactory.buildUserDto(email, name));
    }
}