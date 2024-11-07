package belaquaa.userserviceuser.controller;

import belaquaa.userserviceuser.exception.UserNotFoundException;
import belaquaa.userserviceuser.model.User;
import belaquaa.userserviceuser.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @PathVariable Long id,
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        User user = userService.getUserById(id, includeDeleted);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreUser(@PathVariable Long id) {
        userService.restoreUserById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        List<User> users = userService.getAllUsers(includeDeleted);
           return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Получен запрос на удаление пользователя с ID {}", id);
        try {
            userService.deleteUserById(id);
            log.info("Пользователь с ID {} удален", id);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            log.warn("Пользователь с ID {} не найден для удаления", id);
            return ResponseEntity.notFound().build();
        }
    }
}