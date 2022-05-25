package org.latuile.streamertools.Controller;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import org.latuile.streamertools.Service.YoutubeNowPlayingService;
import org.latuile.streamertools.WebsocketMessages.YoutubeNPAdvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebsocketsController {
    private final Logger logger;
    private final YoutubeNowPlayingService ytService;

    @Autowired
    public WebsocketsController(YoutubeNowPlayingService ytService) {
        this.ytService = ytService;
        logger = LoggerFactory.getLogger(WebsocketsController.class);
    }

    @MessageMapping("/youtube/now-watching")
    @CrossOrigin(origins = "https://www.youtube.com", allowCredentials = "true")
    public void updateYoutubeNowWatching(YoutubeNPAdvert advert) {
        logger.debug("Now Playing Received for vidéo : " + advert.getVideo());

        Video v = ytService.updateAndGetCurrentlyPlaying(advert);
    }
}
