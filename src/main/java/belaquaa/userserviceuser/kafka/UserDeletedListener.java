package belaquaa.userserviceuser.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDeletedListener {

    private final CacheManager cacheManager;

    @KafkaListener(
            topics = "user-deleted-topic",
            containerFactory = "userServiceKafkaListenerContainerFactory"
    )
    public void listenUserDeleted(Long userId) {
        Cache cache = cacheManager.getCache("users");
        if (cache != null) {
            cache.evict(userId);
            cache.evict(userId + "-includeDeleted");
        } else {
            log.warn("Кэш 'users' не найден при попытке удалить пользователя с ID {}", userId);
        }
    }
}