package org.latuile.streamertools.Model;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Preferences {
    public static class Spotify {
        public static final String CLIENT_ID = "spotify.client-id";
        public static final String CLIENT_SECRET = "spotify.client-secret";
        public static final String ACCESS_TOKEN = "spotify.access-token";
        public static final String REFRESH_TOKEN = "spotify.refresh-token";
        public static final String ACCESS_TOKEN_EXPIRES_AT = "spotify.access-token-expiration" ;
    }
    public static class Theme {
        public static final String COLOR_ACCENT1 = "theme.color-accent1";
        public static final String COLOR_ACCENT2 = "theme.color-accent2";
        public static final String COLOR_BACKGROUND = "theme.background";
        public static final String COLOR_TEXT = "theme.text";
        public static final String FONT = "theme.font";
    }
    public static class NowPlaying {
        public static final String BORDER_RADIUS = "now-playing.border-radius";
        public static final String BORDER_WIDTH = "now-playing.border-width";
        public static final String BORDER_ENABLE = "now-playing.border-enable";
        public static final String NOTHING_PLAYING_BEHAVIOR = "now-playing.nothing-playing-behavior";
        public static final String PROGRESS_ENABLE = "now-playing.progress-enable";
        public static final String PROGRESS_COLOR = "now-playing.progress-color";
    }
    public static class Youtube {
        public static final String API_KEY = "youtube.api-key";
    }

    public static class Twitch {
        public static final String CHANNEL_ID = "twitch.channel-id";
        public static final String WIDGET_TIMEOUT = "twitch.widget-timeout";
        public static final String SHOUTOUT_DURATION = "twitch.shoutout-duration";
        public static final String CHOICE_TEXT_COLOR = "twitch.choice-text-color";
        public static final String CHOICE_BACK_COLOR = "twitch.choice-back-color";
        public static final String PREDI_TWITCH_COLOR = "twitch.predi-twitch-color";
    }

    @SneakyThrows
    public static List<String> getKeys(){
        List<Class> PrefClasses = List.of(Preferences.class.getClasses());
        List<String> prefKeys = new ArrayList<>();
        for (Class c : PrefClasses){
            for (Field f : c.getDeclaredFields()){
                prefKeys.add((String) f.get(null));
            }
        }


        return prefKeys;
    }
}
