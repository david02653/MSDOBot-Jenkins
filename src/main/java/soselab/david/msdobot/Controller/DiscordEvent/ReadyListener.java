package soselab.david.msdobot.Controller.DiscordEvent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import soselab.david.msdobot.Service.AdditionalQAService;

import java.awt.*;
import java.time.Instant;

/**
 * Listener to JDA Ready event
 */
@Service
public class ReadyListener implements EventListener {

    private final AdditionalQAService aqaService;

    public ReadyListener(AdditionalQAService additionalQAService){
        this.aqaService = additionalQAService;
    }

    /**
     * load additional question list after JDA is ready
     * @param event ready event
     */
    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof ReadyEvent){
            System.out.println("> JDA API ready");
//            fireOnlineMsg(event);

            if(aqaService.loadFile()){
                System.out.println(">> yaml file load success !");
                System.out.println(aqaService.getMap());
            }else{
                System.out.println(">> yaml file load error");
            }
        }
    }

    /**
     * send message to specific TextChannel when this bot is up
     */
    public void fireOnlineMsg(GenericEvent event){
        JDA currentJDA = event.getJDA();
        TextChannel target = currentJDA.getTextChannelsByName("text2", true).get(0);
        System.out.println("[JDA][ReadyListener] send msg to discord !");
        if(target != null) {
//            target.sendMessage("Bot service has launched !").queue();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setAuthor("Master Bot");
            embedBuilder.setColor(Color.CYAN);
            embedBuilder.setDescription("Seems like this bot is starting to work.\ncheck it out");
            embedBuilder.setFooter("nice try", "https://www.freepngimg.com/download/mouth/92712-ear-head-twitch-pogchamp-emote-free-download-png-hq.png");
            embedBuilder.setTitle("Let you know what's going on.");
            embedBuilder.setTimestamp(Instant.now());
            target.sendMessage(embedBuilder.build()).queue();
        }else
            System.out.println("[JDA][ReadyListener] unable to get channel by id");
    }
}
