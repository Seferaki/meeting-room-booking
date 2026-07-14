package com.example.meeting_room_booking.controller;

import com.example.meeting_room_booking.dto.BookingRequestDto;
import com.example.meeting_room_booking.model.Booking;
import com.example.meeting_room_booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // 1. Создание бронирования
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequestDto dto) {
        Booking booking = bookingService.createBooking(
                dto.getRoomId(),
                dto.getUserId(),
                dto.getStartTime(),
                dto.getEndTime()
        );
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    // 2. Получение всех бронирований
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // 3. Отмена бронирования
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}