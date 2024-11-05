package belaquaa.userserviceuser.service;

import belaquaa.userserviceuser.exception.UserNotFoundException;
import belaquaa.userserviceuser.model.User;
import belaquaa.userserviceuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Long> kafkaTemplate;

    @Override
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUserById(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        user.setIsDeleted(true);
        userRepository.save(user);
        kafkaTemplate.send("user-deleted-topic", id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByIsDeletedFalse();
    }
}
