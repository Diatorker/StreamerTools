package org.latuile.streamertools.Controller;

import org.latuile.streamertools.Exception.SpotifyNotAuthenticatedException;
import org.latuile.streamertools.Model.Entity.PreferenceItem;
import org.latuile.streamertools.Model.Preferences;
import org.latuile.streamertools.Model.Repository.PreferenceRepository;
import org.latuile.streamertools.Service.SpotifyClientService;
import org.latuile.streamertools.Service.YoutubeNowPlayingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequestMapping("/")
public class MainController {
    private final PreferenceRepository preferenceRepository;
    private final SpotifyClientService spotifyClientService;

    @Autowired
    public MainController(PreferenceRepository preferenceRepository, SpotifyClientService spotifyClientService) {
        this.preferenceRepository = preferenceRepository;
        this.spotifyClientService = spotifyClientService;
    }

    @GetMapping("")
    public String HomeView(Model model) {
        List<PreferenceItem> dbPrefs = StreamSupport.stream(preferenceRepository.findAll().spliterator(), true)
                .filter(i -> Objects.nonNull(i.getItemValue()))
                .collect(Collectors.toList());

        List<String> dbPrefsKeys = dbPrefs.parallelStream()
                .map(PreferenceItem::getItemId)
                .collect(Collectors.toList());

        List<PreferenceItem> finalPrefs = new ArrayList<>();
        List<String> classPrefKeys = Preferences.getKeys();
        classPrefKeys.stream()
                .filter(k -> !dbPrefsKeys.contains(k))
                .forEach(k -> finalPrefs.add(new PreferenceItem(k, "")));
        finalPrefs.addAll(dbPrefs);

        finalPrefs.sort(Comparator.comparing(PreferenceItem::getItemValue));

        model.addAttribute("authURI", spotifyClientService.getAuthorizationURI());
        try {
            model.addAttribute("user", spotifyClientService.getCurrentUser());
        } catch (SpotifyNotAuthenticatedException e) {
            model.addAttribute("user", null);
        }

        model.addAttribute("preferences", finalPrefs);
        model.addAttribute("preferencesDict", finalPrefs.stream().collect(Collectors.toMap(PreferenceItem::getItemId, PreferenceItem::getItemValue)));

        return "main/home";
    }

    @PostMapping(value="config", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String SaveConfiguration(@RequestBody MultiValueMap<String, String> formData) {
        Map<String, PreferenceItem> dbPrefs = StreamSupport.stream(preferenceRepository.findAll().spliterator(), true)
                .collect(Collectors.toMap(PreferenceItem::getItemId, i -> i));
        formData.forEach((k, v) -> {
                    if (dbPrefs.containsKey(k)) {
                        dbPrefs.get(k).setItemValue(v.get(0));
                    } else {
                        dbPrefs.put(k, new PreferenceItem(k, v.get(0)));
                    }
                }
        );

        preferenceRepository.saveAll(dbPrefs.values());
        return "redirect:/";
    }
}
