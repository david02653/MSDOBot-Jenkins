package soselab.david.msdobot.Controller.DiscordEvent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import soselab.david.msdobot.Service.AdditionalQAService;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Listener to JDA Ready event
 */
@Service
public class ReadyListener implements EventListener {

    private final AdditionalQAService aqaService;
    private final String serverId;

    public ReadyListener(AdditionalQAService additionalQAService, Environment env){
        this.aqaService = additionalQAService;
        this.serverId = env.getProperty("discord.server.id");
    }

    /**
     * load additional question list after JDA is ready
     * @param event ready event
     */
    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof ReadyEvent){
            System.out.println("> JDA API ready");
            checkManagerChannel(event);
            fireOnlineMsg(event);

            if(aqaService.loadFile()){
                System.out.println(">> yaml file load success !");
                System.out.println(aqaService.getMap());
            }else{
                System.out.println(">> yaml file load error");
            }
        }
    }

    private void checkManagerChannel(GenericEvent event){
        List<TextChannel> botOffice = event.getJDA().getTextChannelsByName("bot-office", true);
        if(botOffice.size() <= 0){
            // create channel to check bot status
            event.getJDA().getGuildById(serverId).createTextChannel("bot-office").queue(channel -> {
                System.out.println(channel.getId());
            });
        }
    }

    /**
     * send message to specific TextChannel when this bot is up
     */
    public void fireOnlineMsg(GenericEvent event){
        JDA currentJDA = event.getJDA();
//        TextChannel channel = event.getJDA().getTextChannelsByName();
        Guild manager = event.getJDA().getGuildById(serverId);
        System.out.println(manager);
        TextChannel target = currentJDA.getTextChannelsByName("bot-office", true).get(0);
//        System.out.println(currentJDA.getTextChannelsByName("text2", true).size());
//        System.out.println(target);
//        TextChannel target = currentJDA.getGuildById("737233839709225001").getTextChannelById("777776098552447027");
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
