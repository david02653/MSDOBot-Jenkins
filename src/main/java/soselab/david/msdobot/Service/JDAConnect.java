package soselab.david.msdobot.Service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import soselab.david.msdobot.Controller.DiscordEvent.DiscordMessageEvent;
import soselab.david.msdobot.Controller.DiscordEvent.ReadyListener;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;
import java.util.List;

/**
 * main control of discord bot
 */
@Service
public class JDAConnect {
    private JDA jda;
    private ReadyListener readyListener;
    private DiscordMessageEvent discordMessageEventListener;
    private final String DISCORD_BOT_TOKEN;

    @Autowired
    public JDAConnect(ReadyListener readyListener, DiscordMessageEvent discordMessageEvent, Environment env){
        this.readyListener = readyListener;
        this.discordMessageEventListener = discordMessageEvent;
        this.DISCORD_BOT_TOKEN = env.getProperty("discord.application.token");
    }

    /**
     * connect to discord when this class instance is created
     * this should be triggered by spring itself when this application startupConsumer
     */
    @PostConstruct
    private void init(){
        try{
            createJDAConnect(DISCORD_BOT_TOKEN);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("[JDA] initialize failed !");
        }
    }

    public JDA getJda(){
        return this.jda;
    }

    /**
     * create connect to discord by using server token
     * @param token server token
     * @throws LoginException if discord login failed
     */
    public void createJDAConnect(String token) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(token);

        configure(builder);
        // add customized ReadyListener
        builder.addEventListeners(readyListener);
        // add customized MessageListener
        builder.addEventListeners(discordMessageEventListener);
        jda = builder.build();
    }

    /**
     * discord bot setup
     * @param builder discord bot builder
     */
    public void configure(JDABuilder builder){
        // disable member activities (streaming / games / spotify)
        builder.disableCache(CacheFlag.ACTIVITY);
        // disable member chunking on startup
        builder.setChunkingFilter(ChunkingFilter.NONE);
    }

    /**
     * send message to target discord channel
     * @param channel channel name
     * @param msg message content
     */
    public void send(String channel, String msg){
        List<TextChannel> channels = jda.getTextChannelsByName(channel, true);
        for(TextChannel ch: channels){
            ch.sendMessage(msg).queue();
        }
    }
}
