package com.example.meeting_room_booking.service;

import com.example.meeting_room_booking.exception.RoomAlreadyBookedException;
import com.example.meeting_room_booking.model.Booking;
import com.example.meeting_room_booking.model.Room;
import com.example.meeting_room_booking.model.User;
import com.example.meeting_room_booking.repository.BookingRepository;
import com.example.meeting_room_booking.repository.RoomRepository;
import com.example.meeting_room_booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User sampleUser;
    private Room sampleRoom;

    @BeforeEach
    void setUp() {
        // Забиваем чистые Java-объекты перед каждым тестом
        sampleUser = User.builder().id(1L).username("sefer").email("test@test.com").build();
        sampleRoom = Room.builder().id(1L).name("Альфа").capacity(10).build();
    }

    @Test
    @DisplayName("Успешное бронирование, когда выбранное время свободно")
    void shouldCreateBookingSuccessfully() {
        // Given — Данные и настройки моков
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        // Обучаем наших роботов-репозиториев, что отвечать сервису
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(sampleRoom));
        // Симулируем, что база пуста и наложений по времени нет (возвращаем пустой список)
        when(bookingRepository.findOverlappingBookings(1L, start, end)).thenReturn(Collections.emptyList());

        Booking savedBooking = Booking.builder().id(100L).user(sampleUser).room(sampleRoom).startTime(start).endTime(end).build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // When — Само действие (вызов метода сервиса)
        Booking result = bookingService.createBooking(1L, 1L, start, end);

        // Then — Утверждения и проверки
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Альфа", result.getRoom().getName());
        // Проверяем, что метод save() реально дернулся ровно 1 раз
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Ошибка бронирования, если комната на это время уже кем-то занята")
    void shouldThrowExceptionWhenRoomIsAlreadyBooked() {
        // Given — Настройки моков
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(sampleRoom));

        // ВАЖНО: Симулируем накладку! Говорим, что метод пересечений вернул список с существующей бронью
        List<Booking> overlapping = List.of(new Booking());
        when(bookingRepository.findOverlappingBookings(1L, start, end)).thenReturn(overlapping);

        // When & Then — Проверяем, что метод выбросит именно наше исключение
        assertThrows(RoomAlreadyBookedException.class, () ->
                bookingService.createBooking(1L, 1L, start, end)
        );

        // Железобетонная проверка: убеждаемся, что из-за ошибки метод save() вообще НЕ вызывался
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}