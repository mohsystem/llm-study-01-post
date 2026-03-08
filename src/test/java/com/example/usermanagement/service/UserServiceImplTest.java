package com.example.usermanagement.service;

import com.example.usermanagement.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(UserServiceImpl.class)
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    void createAndFindUserByEmail() {
        User user = new User();
        user.setFullName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setActive(true);

        User created = userService.createUser(user);

        assertThat(created.getId()).isNotNull();
        assertThat(userService.getUserByEmail("jane@example.com")).isPresent();
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {
        User user = new User();
        user.setFullName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setActive(true);
        userService.createUser(user);

        User duplicate = new User();
        duplicate.setFullName("John Doe");
        duplicate.setEmail("jane@example.com");
        duplicate.setActive(true);

        assertThatThrownBy(() -> userService.createUser(duplicate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void deleteMissingUserShouldFail() {
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
