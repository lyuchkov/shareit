package ru.practicum.shareit.request.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

public interface ItemRequestRepo extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequestor_IdOrderByCreatedDesc(Long requestorId);

    List<ItemRequest> findByRequestor_IdNotOrderByCreatedDesc(Long requestorId);
}