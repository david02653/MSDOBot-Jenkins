package soselab.david.msdobot.Service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import soselab.david.msdobot.Entity.LongMessage.DiscordMessage;
import soselab.david.msdobot.Exception.MongoNotFoundException;
import soselab.david.msdobot.Repository.LongMessageRepository;

/**
 * Long message service do simple save and search with mongodb
 * this service is mainly for storing message with length over discord limit
 */
@Service
public class LongMessageService {

    private static LongMessageRepository repository;

    private final String SERVER_HOST;
    private final String SERVER_PORT;

    public LongMessageService(LongMessageRepository repo, Environment env){
        repository = repo;
        this.SERVER_HOST = env.getProperty("server.port");
        this.SERVER_PORT = env.getProperty("server.address");
    }

    /**
     * query previous message by id
     * @param id message id
     * @return target message object
     */
    public DiscordMessage getMessage(String id){
        return repository.findById(id).orElseThrow(() -> new MongoNotFoundException("message not found."));
    }

    /**
     * insert new message into database
     * @param content message content
     * @return message id
     */
    public String addMessage(String content){
        DiscordMessage msg = new DiscordMessage(content);
        // add new message into database
        return repository.insert(msg).getId();
    }

    /**
     * get url link of message
     * @param id message id
     * @return message url
     */
    public String getUrl(String id){
        // http://<your-host>:<your-port>/message/{id}
        return "http://" + SERVER_HOST + ":" + SERVER_PORT + "/message/" + id;
    }
}
