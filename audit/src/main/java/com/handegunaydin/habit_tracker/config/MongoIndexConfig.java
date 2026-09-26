package com.handegunaydin.habit_tracker.config;

import com.handegunaydin.habit_tracker.entity.EventLog;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
@RequiredArgsConstructor
public class MongoIndexConfig {
    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        Index index = new Index()
                .on("createdAt", Sort.Direction.ASC).expire(60);

        mongoTemplate.indexOps(EventLog.class).createIndex(index);

    }
}
