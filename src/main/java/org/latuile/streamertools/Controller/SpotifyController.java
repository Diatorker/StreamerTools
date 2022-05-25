package org.latuile.streamertools.Controller;

import lombok.SneakyThrows;
import org.latuile.streamertools.Exception.SpotifyNotAuthenticatedException;
import org.latuile.streamertools.Model.Entity.PreferenceItem;
import org.latuile.streamertools.Model.Preferences;
import org.latuile.streamertools.Model.Repository.PreferenceRepository;
import org.latuile.streamertools.Service.SpotifyClientService;
import org.latuile.streamertools.WebsocketMessages.NowPlayingInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/spotify")
public class SpotifyController extends NowPlayingController {
    private final SpotifyClientService spotifyClientService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public SpotifyController(SpotifyClientService spotifyClientService, PreferenceRepository preferenceRepository) {
        super(preferenceRepository);
        this.spotifyClientService = spotifyClientService;
    }

    @Override
    @GetMapping("/widget")
    public String getWidget(
            Model model,
            @RequestParam(name = "mode", defaultValue = "full") String mode
    ){
        model.addAttribute("metadataUrl", "https://localhost:8443/spotify/metadata");

        return super.getWidget(model, mode);
    }

    @Override
    @CrossOrigin("*")
    @GetMapping("/metadata")
    public @ResponseBody NowPlayingInfos getMetadata()
    {
        try {
            CurrentlyPlayingContext playingContext = spotifyClientService.getCurrentPlayback();
            if(playingContext == null || !playingContext.getIs_playing()){
                return new NowPlayingInfos();
            }
            switch (playingContext.getCurrentlyPlayingType()){

                case TRACK:
                    Track track = (Track) playingContext.getItem();
                    return new NowPlayingInfos(
                            track.getAlbum().getImages()[0].getUrl(),
                            track.getName(),
                            Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(", ")),
                            true,
                            track.getId()
                    );
                case EPISODE: //Not supported right now, returns null when playing episodes...
//                    Episode episode = (Episode) playingContext.getItem();
//                    return new NowPlayingInfos(
//                            episode.getShow().getImages()[0].getUrl(),
//                            episode.getName(),
//                            episode.getShow().getName(),
//                            true
//                    );
                    break;
                case AD:
                    break;
                case UNKNOWN:
                    break;
            }

        } catch (SpotifyNotAuthenticatedException e) {
            logger.error("Spotify Authentication Error. Try to reauthorize the service, or check client id & secret");
        }
        return new NowPlayingInfos();
    }

    @SneakyThrows
    @GetMapping("/redirect-back")
    public String saveAuthorization(String code, String state){
        if(StringUtils.hasText(code)) {
            spotifyClientService.handleAuthorizationCode(code);
            return "redirect:/";
        }

        throw new SpotifyNotAuthenticatedException("No Authorization Code sent back");
    }

    @GetMapping("/clear-association")
    public String clearAssociation(){
        Optional<PreferenceItem> optionalAccessToken = preferenceRepository.findById(Preferences.Spotify.ACCESS_TOKEN);
        Optional<PreferenceItem> optionalRefreshToken = preferenceRepository.findById(Preferences.Spotify.REFRESH_TOKEN);
        Optional<PreferenceItem> optionalTokenExpires = preferenceRepository.findById(Preferences.Spotify.ACCESS_TOKEN_EXPIRES_AT);

        if(optionalAccessToken.isPresent()){
            preferenceRepository.delete(optionalAccessToken.get());
        }
        if(optionalRefreshToken.isPresent()){
            preferenceRepository.delete(optionalRefreshToken.get());
        }
        if(optionalTokenExpires.isPresent()){
            preferenceRepository.delete(optionalTokenExpires.get());
        }

        return "redirect:/";
    }

//    @GetMapping("/authorize")
//    public String startAuthorization(Model model){
//        model.addAttribute("authURI", spotifyClientService.getAuthorizationURI());
//        try {
//            model.addAttribute("user", spotifyClientService.getCurrentUser());
//        } catch (SpotifyNotAuthenticatedException e) {
//            model.addAttribute("user", null);
//        }
//
//        return "spotify/authorize";
//    }
}
