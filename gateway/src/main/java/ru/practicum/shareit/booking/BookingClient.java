package ru.practicum.shareit.booking;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.AbstractClient;
import ru.practicum.shareit.properties.GatewayProperties;

import java.util.Map;

@Service
public class BookingClient extends AbstractClient {

    private static final String API_PREFIX = "/bookings";

    public BookingClient(GatewayProperties properties, RestTemplateBuilder builder) {
        super(builder, properties.getUrl() + API_PREFIX);
    }

    public ResponseEntity<Object> bookItem(long userId, BookingCreateDto createDto) {
        return this.post("", userId, createDto);
    }

    public ResponseEntity<Object> approve(long userId, long bookingId, boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return this.patch("/" + bookingId + "?approved={approved}", userId, parameters);
    }

    public ResponseEntity<Object> getBooking(long userId, long bookingId) {
        return this.get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookings(long userId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state.name());
        return this.get("?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(long userId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state.name());
        return this.get("/owner?state={state}", userId, parameters);
    }
}