package com.example.meeting_room_booking.service;

import com.example.meeting_room_booking.model.Booking;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    // Метод для создания бронирования
    Booking createBooking(Long roomId, Long userId, LocalDateTime startTime, LocalDateTime endTime);

    // Метод для получения всех бронирований
    List<Booking> getAllBookings();

    // Метод для отмены (удаления) бронирования
    void cancelBooking(Long bookingId);
}