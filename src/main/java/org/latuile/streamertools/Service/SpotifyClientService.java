package org.latuile.streamertools.Service;

import org.apache.hc.core5.http.ParseException;
import org.latuile.streamertools.Exception.SpotifyNotAuthenticatedException;
import org.latuile.streamertools.Model.Entity.PreferenceItem;
import org.latuile.streamertools.Model.Preferences;
import org.latuile.streamertools.Model.Repository.PreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.player.GetInformationAboutUsersCurrentPlaybackRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SpotifyClientService {
    //    @Value("${spotify.clientId}")
//    private String clientId;
//    @Value("${spotify.clientSecret}")
//    private String clientSecret;
    private final PreferenceRepository preferenceRepository;
    private final URI redirectUri = SpotifyHttpManager.makeUri("https://localhost:8443/spotify/redirect-back");
    private final SpotifyApi spotifyApi;
    private final AuthorizationCodeUriRequest authorizationCodeUriRequest;
    private Instant nowPlayingCacheExpires;
    private CurrentlyPlayingContext cachedPlayingContext;
    private final Logger logger = LoggerFactory.getLogger(SpotifyClientService.class);

    @Autowired
    public SpotifyClientService(
            PreferenceRepository preferenceRepository
//            , @Value("${spotify.clientId}") String clientId
//            , @Value("${spotify.clientSecret}") String clientSecret
    ) {
        this.preferenceRepository = preferenceRepository;
        spotifyApi = new SpotifyApi.Builder()
//            .setAccessToken("")
                .setClientId(preferenceRepository.findById(Preferences.Spotify.CLIENT_ID).orElse(new PreferenceItem(Preferences.Spotify.CLIENT_ID, "")).getItemValue())
                .setClientSecret(preferenceRepository.findById(Preferences.Spotify.CLIENT_SECRET).orElse(new PreferenceItem(Preferences.Spotify.CLIENT_SECRET, "")).getItemValue())
                .setRedirectUri(redirectUri)
                .build();
        authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
//          .state("x4xkmn9pu3j6ukrs8n")
                .scope("user-read-playback-state")
//          .show_dialog(true)
                .build();

        spotifyApi.setAccessToken(preferenceRepository.findById(Preferences.Spotify.ACCESS_TOKEN).orElse(new PreferenceItem()).getItemValue());
        spotifyApi.setRefreshToken(preferenceRepository.findById(Preferences.Spotify.REFRESH_TOKEN).orElse(new PreferenceItem()).getItemValue());
        nowPlayingCacheExpires = Instant.EPOCH;
    }

    public URI getAuthorizationURI() {
        return authorizationCodeUriRequest.execute();
    }

    public void handleAuthorizationCode(String code) {
        try {
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code)
                    .build();
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            PreferenceItem AccessTokenItem = preferenceRepository.findById(Preferences.Spotify.ACCESS_TOKEN).orElse(new PreferenceItem(Preferences.Spotify.ACCESS_TOKEN, null));
            PreferenceItem RefreshTokenItem = preferenceRepository.findById(Preferences.Spotify.REFRESH_TOKEN).orElse(new PreferenceItem(Preferences.Spotify.REFRESH_TOKEN, null));
            PreferenceItem TokenExpiresItem = preferenceRepository.findById(Preferences.Spotify.ACCESS_TOKEN_EXPIRES_AT).orElse(new PreferenceItem(Preferences.Spotify.ACCESS_TOKEN_EXPIRES_AT, null));

            AccessTokenItem.setItemValue(authorizationCodeCredentials.getAccessToken());
            RefreshTokenItem.setItemValue(authorizationCodeCredentials.getRefreshToken());
            TokenExpiresItem.setItemValue(Instant.now().plusSeconds(authorizationCodeCredentials.getExpiresIn() - 120).toString());

            preferenceRepository.save(AccessTokenItem);
            preferenceRepository.save(RefreshTokenItem);
            preferenceRepository.save(TokenExpiresItem);
            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

            logger.info("Token expires in: " + authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    public void checkAccessToken() throws SpotifyNotAuthenticatedException {
        String StrExpirationDate = preferenceRepository.findById(Preferences.Spotify.ACCESS_TOKEN_EXPIRES_AT).orElse(new PreferenceItem()).getItemValue();
        if (!StringUtils.hasText(StrExpirationDate) || Instant.now().isAfter(Instant.parse(StrExpirationDate))) {
            try {
                if (StringUtils.hasText(preferenceRepository.findById(Preferences.Spotify.REFRESH_TOKEN).orElse(new PreferenceItem()).getItemValue())) {
                    AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                            .build();
                    AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

                    PreferenceItem AccessTokenItem = preferenceRepository.findById(Preferences.Spotify.ACCESS_TOKEN).orElse(new PreferenceItem(Preferences.Spotify.ACCESS_TOKEN, null));
                    AccessTokenItem.setItemValue(authorizationCodeCredentials.getAccessToken());
                    preferenceRepository.save(AccessTokenItem);
                    spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

                    if (authorizationCodeCredentials.getRefreshToken() != null) {
                        PreferenceItem RefreshTokenItem = preferenceRepository.findById(Preferences.Spotify.REFRESH_TOKEN).orElse(new PreferenceItem(Preferences.Spotify.REFRESH_TOKEN, null));
                        RefreshTokenItem.setItemValue(authorizationCodeCredentials.getRefreshToken());
                        preferenceRepository.save(RefreshTokenItem);
                        spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
                    }

                    PreferenceItem TokenExpiresItem = preferenceRepository.findById(Preferences.Spotify.ACCESS_TOKEN_EXPIRES_AT).orElse(new PreferenceItem(Preferences.Spotify.ACCESS_TOKEN_EXPIRES_AT, null));
                    TokenExpiresItem.setItemValue(Instant.now().plusSeconds(authorizationCodeCredentials.getExpiresIn() - 120).toString());
                    preferenceRepository.save(TokenExpiresItem);

                    // Set access and refresh token for further "spotifyApi" object usage

                    logger.info("Token expires in: " + authorizationCodeCredentials.getExpiresIn());
                } else {
                    throw new SpotifyNotAuthenticatedException("No refresh token available");
                }
            } catch (IOException | ParseException e) {
                logger.error("Error: " + e.getMessage());
            } catch (SpotifyWebApiException e) {
                preferenceRepository.save(new PreferenceItem(Preferences.Spotify.ACCESS_TOKEN, null));
                preferenceRepository.save(new PreferenceItem(Preferences.Spotify.REFRESH_TOKEN, null));
                preferenceRepository.save(new PreferenceItem(Preferences.Spotify.ACCESS_TOKEN_EXPIRES_AT, Instant.EPOCH.toString()));
                throw new SpotifyNotAuthenticatedException("Error while refreshing the access token", e);
            }
        }
    }

    public CurrentlyPlayingContext getCurrentPlayback() throws SpotifyNotAuthenticatedException {
        if (nowPlayingCacheExpires.isBefore(Instant.now())) {
            logger.debug("Cache miss");
            checkAccessToken();
            try {
                GetInformationAboutUsersCurrentPlaybackRequest getInformationAboutUsersCurrentPlaybackRequest =
                        spotifyApi.getInformationAboutUsersCurrentPlayback()
                                .build();
                CurrentlyPlayingContext currentlyPlayingContext = getInformationAboutUsersCurrentPlaybackRequest.execute();
//            System.out.println("Timestamp: " + currentlyPlayingContext.getTimestamp());
                cachedPlayingContext = currentlyPlayingContext;
                nowPlayingCacheExpires = Instant.now().plus(2, ChronoUnit.SECONDS);
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                logger.error("Error: " + e.getMessage());
                return null;
            }
        }
        return cachedPlayingContext;
    }

    public User getCurrentUser() throws SpotifyNotAuthenticatedException {
        checkAccessToken();
        try {
            GetCurrentUsersProfileRequest getCurrentUsersProfileRequest =
                    spotifyApi.getCurrentUsersProfile()
                            .build();
            return getCurrentUsersProfileRequest.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error: " + e.getMessage());
        }

        return null;
    }
}
