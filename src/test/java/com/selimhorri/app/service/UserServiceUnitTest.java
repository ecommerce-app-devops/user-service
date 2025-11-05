package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Credential testCredential;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testCredential = Credential.builder()
                .credentialId(1)
                .username("testuser")
                .password("password123")
                .build();

        testUser = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .credential(testCredential)
                .build();

        testUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();
    }

    @Test
    @DisplayName("Should find user by ID when user exists with credentials")
    void testFindById_Success() {
        // Given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void testFindById_UserNotFound() {
        // Given
        Integer userId = 999;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserObjectNotFoundException.class, () -> userService.findById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user exists but has no credentials")
    void testFindById_UserWithoutCredentials() {
        // Given
        Integer userId = 1;
        User userWithoutCredential = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .credential(null)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithoutCredential));

        // When & Then
        assertThrows(UserObjectNotFoundException.class, () -> userService.findById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should find user by username when user exists")
    void testFindByUsername_Success() {
        // Given
        String username = "testuser";
        when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.findByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getCredentialDto().getUsername());
        verify(userRepository, times(1)).findByCredentialUsername(username);
    }

    @Test
    @DisplayName("Should throw exception when username not found")
    void testFindByUsername_NotFound() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserObjectNotFoundException.class, () -> userService.findByUsername(username));
        verify(userRepository, times(1)).findByCredentialUsername(username);
    }

    @Test
    @DisplayName("Should find all users with credentials")
    void testFindAll_Success() {
        // Given
        List<User> users = new ArrayList<>();
        users.add(testUser);
        
        User anotherUser = User.builder()
                .userId(2)
                .firstName("Jane")
                .lastName("Smith")
                .credential(Credential.builder().credentialId(2).username("janesmith").build())
                .build();
        users.add(anotherUser);

        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserDto> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should filter out users without credentials when finding all")
    void testFindAll_FiltersUsersWithoutCredentials() {
        // Given
        List<User> users = new ArrayList<>();
        users.add(testUser);
        
        User userWithoutCredential = User.builder()
                .userId(2)
                .firstName("Jane")
                .lastName("Smith")
                .credential(null)
                .build();
        users.add(userWithoutCredential);

        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserDto> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only user with credentials
        assertEquals(1, result.get(0).getUserId());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should save new user successfully")
    void testSave_Success() {
        // Given
        UserDto newUserDto = UserDto.builder()
                .firstName("New")
                .lastName("User")
                .email("newuser@example.com")
                .phone("9876543210")
                .build();

        User savedUser = User.builder()
                .userId(2)
                .firstName("New")
                .lastName("User")
                .email("newuser@example.com")
                .phone("9876543210")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDto result = userService.save(newUserDto);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user successfully")
    void testUpdate_Success() {
        // Given
        UserDto updatedUserDto = UserDto.builder()
                .userId(1)
                .firstName("Updated")
                .lastName("Name")
                .email("updated@example.com")
                .phone("1111111111")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.update(updatedUserDto);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user and credentials successfully")
    void testDeleteById_Success() {
        // Given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(credentialRepository).deleteByCredentialId(anyInt());

        // When
        userService.deleteById(userId);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
        verify(credentialRepository, times(1)).deleteByCredentialId(1);
    }

    @Test
    @DisplayName("Should throw exception when deleting user without credentials")
    void testDeleteById_UserWithoutCredentials() {
        // Given
        Integer userId = 1;
        User userWithoutCredential = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .credential(null)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithoutCredential));

        // When & Then
        assertThrows(UserObjectNotFoundException.class, () -> userService.deleteById(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(credentialRepository, never()).deleteByCredentialId(anyInt());
    }
}

