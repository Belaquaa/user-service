package belaquaa.userserviceuser.service;

import belaquaa.userserviceuser.exception.UserNotFoundException;
import belaquaa.userserviceuser.model.User;
import belaquaa.userserviceuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Long> kafkaTemplate;

    @Override
    @CachePut(value = "users", key = "#result.id")
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @CachePut(value = "users", key = "#id")
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setUsername(userDetails.getUsername());
        user.setAge(userDetails.getAge());

        return userRepository.save(user);
    }

    @Override
    @Cacheable(value = "users", key = "#id + '-' + #includeDeleted")
    public User getUserById(Long id, boolean includeDeleted) {
        if (includeDeleted) {
            return userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        } else {
            return userRepository.findByIdAndIsDeletedFalse(id)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        }
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void restoreUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        user.setIsDeleted(false);
        userRepository.save(user);
        kafkaTemplate.send("user-restored-topic", id);
    }

    @Override
    public List<User> getAllUsers(boolean includeDeleted) {
        if (includeDeleted) {
            return userRepository.findAll();
        } else {
            return userRepository.findAllByIsDeletedFalse();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUserById(Long id) {
        log.info("Удаление пользователя по ID {}", id);
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден для удаления", id);
                    return new UserNotFoundException("Пользователь не найден");
                });
        user.setIsDeleted(true);
        userRepository.save(user);
        log.info("Пользователь с ID {} помечен как удаленный", id);
        kafkaTemplate.send("user-deleted-topic", id);
        log.info("Отправлено сообщение в 'user-deleted-topic' для пользователя ID {}", id);
    }
}