package belaquaa.userserviceuser.kafka;

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
        Cache cache = cacheManager.getCache("users");
        if (cache != null) {
            cache.evict(userId);
            userRepository.findByIdAndIsDeletedFalse(userId)
                    .ifPresent(user -> Objects
                            .requireNonNull(cacheManager.getCache("users"))
                            .put(userId, user));
        } else {
            log.warn("Кэш 'users' не найден!");
        }
    }
}