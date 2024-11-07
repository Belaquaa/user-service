package belaquaa.userserviceuser.kafka;

import belaquaa.userserviceuser.model.User;
import belaquaa.userserviceuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRestoredListener {

    private final CacheManager cacheManager;
    private final UserRepository userRepository;

    @KafkaListener(
            topics = "user-restored-topic",
            containerFactory = "userServiceAdminKafkaListenerContainerFactory"
    )
    public void listenUserRestored(Long userId) {
        log.info("Получено сообщение из 'user-restored-topic' для пользователя ID {}", userId);
        Cache cache = cacheManager.getCache("users");
        if (cache != null) {
            cache.evict(userId);
            User user = userRepository.findByIdAndIsDeletedFalse(userId).orElse(null);
            if (user != null) {
                cache.put(userId, user);
                log.info("Кэш обновлен для пользователя ID {}", userId);
            } else {
                log.warn("Пользователь с ID {} не найден в репозитории при восстановлении", userId);
            }
        } else {
            log.warn("Кэш 'users' не найден!");
        }
    }
}