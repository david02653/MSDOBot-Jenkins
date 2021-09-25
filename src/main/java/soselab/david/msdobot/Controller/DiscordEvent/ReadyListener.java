package soselab.david.msdobot.Controller.DiscordEvent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.entities.CategoryImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import soselab.david.msdobot.Service.AdditionalQAService;

import java.awt.*;
import java.time.Instant;
import java.util.List;

/**
 * Listener to JDA Ready event
 */
@Service
public class ReadyListener implements EventListener {

    private final AdditionalQAService aqaService;
    private final String serverId;
    private final String managerChannelName;
    private final String rabbitmqChannelName;

    public ReadyListener(AdditionalQAService additionalQAService, Environment env){
        this.aqaService = additionalQAService;
        this.serverId = env.getProperty("discord.server.id");
        this.managerChannelName = env.getProperty("discord.channel.system");
        this.rabbitmqChannelName = env.getProperty("discord.channel.rabbitmq");
    }

    /**
     * load additional question list after JDA is ready
     * @param event ready event
     */
    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof ReadyEvent){
            System.out.println("> JDA API ready");
            checkBotChannel(event);
            fireOnlineMsg(event);

            if(aqaService.loadFile()){
                System.out.println(">> yaml file load success !");
                System.out.println(aqaService.getMap());
            }else{
                System.out.println(">> yaml file load error");
            }
        }
    }

    /**
     * check if specific channel exist
     * botOffice is for system message
     * rabbitMsg is for message from rabbitmq
     * create channel if no correspond channel found
     * @param event ready event
     */
    private void checkBotChannel(GenericEvent event){
        List<TextChannel> botOffice = event.getJDA().getTextChannelsByName(managerChannelName, true);
        if(botOffice.size() <= 0){
            // create channel to check bot status
            event.getJDA().getGuildById(serverId).createTextChannel(managerChannelName).queue(channel -> {
                System.out.println("[DEBUG][system channel] id: " + channel.getId());
            });
        }
        List<TextChannel> rabbitMsg = event.getJDA().getTextChannelsByName(rabbitmqChannelName, true);
        if(rabbitMsg.size() <= 0){
            event.getJDA().getGuildById(serverId).createTextChannel(rabbitmqChannelName).queue(channel -> {
                System.out.println("[DEBUG][rabbitmq channel] id: " + channel.getId());
            });
        }
    }

    /**
     * send message to specific TextChannel when this bot is up
     */
    public void fireOnlineMsg(GenericEvent event){
        JDA currentJDA = event.getJDA();
        TextChannel target = currentJDA.getGuildById(serverId).getTextChannelsByName(managerChannelName, true).get(0);
//        TextChannel target = currentJDA.getTextChannelsByName("bot-office", true).get(0);
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
