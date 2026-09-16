package ru.practicum.shareit.comment.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.item.id IN (?1) ORDER BY c.created DESC")
    List<Comment> findByItemIds(List<Long> itemIds);
}