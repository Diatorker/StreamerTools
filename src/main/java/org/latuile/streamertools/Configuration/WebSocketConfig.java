package org.latuile.streamertools.Configuration;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import org.latuile.streamertools.Model.Entity.PreferenceItem;
import org.latuile.streamertools.Model.Preferences;
import org.latuile.streamertools.Model.Repository.PreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final Environment env;
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    @Autowired
    public WebSocketConfig(Environment env) {
        this.env = env;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/youtube").setAllowedOrigins("*");
        registry.addEndpoint("/youtube").setAllowedOrigins("https://www.youtube.com").withSockJS();
    }

    @Bean
    public YouTube buildYoutubeClient(PreferenceRepository preferenceRepository) {
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, request -> {
        }).setApplicationName("YoutubeVideoInfo")
                .setYouTubeRequestInitializer(new YouTubeRequestInitializer(
                        preferenceRepository
                                .findById(Preferences.Youtube.API_KEY)
                                .orElse(new PreferenceItem(Preferences.Youtube.API_KEY, "")).getItemValue())
                ).build();
    }
}