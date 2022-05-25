package org.latuile.streamertools.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import lombok.SneakyThrows;
import org.latuile.streamertools.WebsocketMessages.YoutubeNPAdvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Service
public class YoutubeNowPlayingService {
    private final YouTube youtubeClient;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Video CurrentlyPlaying;
    private UUID CurrentlyPlayingAdverter;
    private Instant CurrentlyPlayingExpires;

    public YoutubeNowPlayingService(YouTube youtubeClient) {
        this.youtubeClient = youtubeClient;
        CurrentlyPlayingExpires = Instant.EPOCH;
    }

    @SneakyThrows
    public Video updateAndGetCurrentlyPlaying(YoutubeNPAdvert advert) {
        if (CurrentlyPlayingAdverter == null
                || Instant.now().isAfter(CurrentlyPlayingExpires)
                || CurrentlyPlayingAdverter.equals(advert.getAdverter())) {
            if (CurrentlyPlaying == null || !Objects.equals(CurrentlyPlaying.getId(), advert.getVideo())) {
                if (StringUtils.hasText(advert.getVideo())) {
                    try {
                        YouTube.Videos.List list = youtubeClient.videos().list(Collections.singletonList("id")).setPart(Collections.singletonList("snippet"));
                        list.setId(Collections.singletonList(advert.getVideo()));

                        CurrentlyPlaying = list.execute().getItems().get(0);
                        CurrentlyPlayingExpires = Instant.now().plusSeconds(5);
                        CurrentlyPlayingAdverter = advert.getAdverter();
                        logger.info("Fetched video info : " + CurrentlyPlaying.getSnippet().getTitle() + " by " + CurrentlyPlaying.getSnippet().getChannelTitle() + " from adverter " + advert.getAdverter());
                    } catch (GoogleJsonResponseException ex) {
                        if (ex.getDetails().getCode() == 403) {
                            logger.error("Youtube Authentication Error. Please check your Youtube API key");
                        } else {
                            logger.error("Youtube API error", ex);
                        }
                    }
                } else {
                    CurrentlyPlaying = null;
                }
            } else {
                CurrentlyPlayingExpires = Instant.now().plusSeconds(2);
            }

            return CurrentlyPlaying;
        } else {
            return null;
        }
    }

    public Video getCurrentlyPlaying() {
        if (Instant.now().isAfter(CurrentlyPlayingExpires)) {
            return null;
        } else {
            return CurrentlyPlaying;
        }
    }
}
