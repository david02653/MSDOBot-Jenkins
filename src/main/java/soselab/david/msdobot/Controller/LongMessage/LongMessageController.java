package soselab.david.msdobot.Controller.LongMessage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import soselab.david.msdobot.Service.LongMessageService;

/**
 * simple frontend for message display
 */
@RestController
@RequestMapping(value = "/message")
public class LongMessageController {

    private final LongMessageService longMessageService;

    public LongMessageController(LongMessageService longMessageService){
        this.longMessageService = longMessageService;
    }

    /**
     * return message of id
     * @param id target id
     * @return message content
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<String> getMessage(@PathVariable String id){
        System.out.println("[DEBUG][LongMessage Controller] GET method: request message triggered.");
        return ResponseEntity.ok(longMessageService.getMessage(id).getMessage());
    }
}