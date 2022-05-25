package org.latuile.streamertools.WebsocketMessages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NowPlayingInfos {
    private String Thumbnail;
    private String Title;
    private String Channel;
    private boolean SourceConnected;
    private String ContentId;
}
