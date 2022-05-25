package org.latuile.streamertools.WebsocketMessages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeNPAdvert {
    private String video;

    private UUID adverter;
}
