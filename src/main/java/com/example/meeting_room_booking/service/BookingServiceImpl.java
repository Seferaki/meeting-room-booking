package com.example.meeting_room_booking.service;

import com.example.meeting_room_booking.exception.ResourceNotFoundException;
import com.example.meeting_room_booking.exception.RoomAlreadyBookedException;
import com.example.meeting_room_booking.model.Booking;
import com.example.meeting_room_booking.model.Room;
import com.example.meeting_room_booking.model.User;
import com.example.meeting_room_booking.repository.BookingRepository;
import com.example.meeting_room_booking.repository.RoomRepository;
import com.example.meeting_room_booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public Booking createBooking(Long roomId, Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. Проверяем валидность времени (бизнес-валидация)
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Время начала должно быть строго раньше времени окончания!");
        }

        // 2. Ищем пользователя в базе. Если нет — кидаем нашу ошибку
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не найден"));

        // 3. Ищем комнату в базе. Если нет — кидаем ошибку
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Комната с id " + roomId + " не найден"));

        // 4. Проверяем пересечения по времени с помощью нашего JPQL-запроса
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(roomId, startTime, endTime);
        if (!overlappingBookings.isEmpty()) {
            throw new RoomAlreadyBookedException("Эта комната уже занята на выбранный промежуток времени!");
        }

        // 5. Если всё ок — собираем объект Booking через Builder и сохраняем
        Booking booking = Booking.builder()
                .room(room)
                .user(user)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }   

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Бронирование с id " + bookingId + " не найдено");
        }
        bookingRepository.deleteById(bookingId);
    }
}