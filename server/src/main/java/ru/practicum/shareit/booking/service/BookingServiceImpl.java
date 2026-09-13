package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repo.BookingMapper;
import ru.practicum.shareit.booking.repo.BookingRepo;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepo;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {

    BookingRepo bookingRepo;
    ItemRepo itemRepo;
    UserRepo userRepo;

    @Override
    public BookingDto createBooking(BookingCreateDto createRequest, long userId) {
        log.info("Creating booking {} for user {}", createRequest, userId);

        if (!createRequest.getStart().isBefore(createRequest.getEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Start date must be before end date");
        }

        User booker = this.getUserOrElseThrow(userId);
        Item item = this.getItemOrElseThrow(createRequest);

        if (!item.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is not available for booking");
        }

        Booking booking = BookingMapper.fromDto(createRequest, booker, item);

        Booking saved = this.bookingRepo.save(booking);
        return BookingMapper.toDto(saved);
    }

    private Item getItemOrElseThrow(BookingCreateDto createRequest) {
        return this.itemRepo.findById(createRequest.getItemId())
                .orElseThrow(() -> new NotFoundException(
                        "Item with id %s not found".formatted(createRequest.getItemId())));
    }

    private User getUserOrElseThrow(long userId) {
        return this.userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));
    }

    @Override
    public BookingDto approveBooking(long bookingId, boolean approved, long userId) {
        log.info("Approving booking {} by user {}", bookingId, userId);

        Booking booking = this.getBookingOrElseThrow(bookingId);

        if (booking.getItem().getOwner().getId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can approve booking");
        }
        if (booking.getStatus() != Booking.BookingStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is not in waiting state");
        }

        if (approved) {
            booking.setStatus(Booking.BookingStatus.APPROVED);
        } else {
            booking.setStatus(Booking.BookingStatus.REJECTED);
        }

        Booking updated = this.bookingRepo.save(booking);
        return BookingMapper.toDto(updated);
    }

    private Booking getBookingOrElseThrow(long bookingId) {
        return this.bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id %s not found".formatted(bookingId)));
    }

    @Override
    public BookingDto getBookingById(long bookingId, long userId) {
        log.info("Getting booking {} for user {}", bookingId, userId);

        Booking booking = this.getBookingOrElseThrow(bookingId);

        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or booker can get booking");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public Collection<BookingDto> getBookingsByBooker(long userId, String state) {
        log.info("Getting bookings for booker {} with state {}", userId, state);

        this.getUserOrElseThrow(userId);

        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> bookings = this.bookingRepo.findByBookerIdAndState(userId, state, now);

        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<BookingDto> getBookingsByOwner(long userId, String state) {
        log.info("Getting bookings for owner {} with state {}", userId, state);

        this.getUserOrElseThrow(userId);

        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> bookings = this.bookingRepo.findByOwnerIdAndState(userId, state, now);

        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

}