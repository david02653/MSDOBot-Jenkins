package soselab.david.msdobot.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import soselab.david.msdobot.Entity.LongMessage.DiscordMessage;

@Repository
public interface LongMessageRepository extends MongoRepository<DiscordMessage, String> {
    DiscordMessage findDiscordMessageById(String id);
}
