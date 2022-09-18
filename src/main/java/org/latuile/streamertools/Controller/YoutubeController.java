package org.latuile.streamertools.Controller;

import org.latuile.streamertools.Model.Entity.PreferenceItem;
import org.latuile.streamertools.Model.Preferences;
import org.latuile.streamertools.Model.Repository.PreferenceRepository;
import org.latuile.streamertools.Service.YoutubeNowPlayingService;
import org.latuile.streamertools.WebsocketMessages.NowPlayingInfos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Controller
@RequestMapping("youtube")
public class YoutubeController extends NowPlayingController {

    private final YoutubeNowPlayingService youtubeNowPlayingService;

    @Autowired
    public YoutubeController(PreferenceRepository preferenceRepository, YoutubeNowPlayingService youtubeNowPlayingService) {
        super(preferenceRepository);
        this.youtubeNowPlayingService = youtubeNowPlayingService;
    }

    @Override
    @GetMapping("/widget")
    public String getWidget(
            Model model,
            @RequestParam(name = "mode", defaultValue = "full") String mode
    ){

        model.addAttribute("metadataUrl", "https://localhost:8443/youtube/metadata");

        return super.getWidget(model, mode);
    }


    @Override
    @CrossOrigin(origins = "*")
    @GetMapping("/metadata")
    public @ResponseBody
    NowPlayingInfos getMetadata() {
        if (youtubeNowPlayingService.getCurrentlyPlaying() != null) {
            return new NowPlayingInfos(
                    youtubeNowPlayingService.getCurrentlyPlaying().getSnippet().getThumbnails().getMedium().getUrl(),
                    youtubeNowPlayingService.getCurrentlyPlaying().getSnippet().getTitle(),
                    youtubeNowPlayingService.getCurrentlyPlaying().getSnippet().getChannelTitle(),
                    true,
                    null,
                    null
            );
        } else {
            return new NowPlayingInfos();
        }
    }
}
