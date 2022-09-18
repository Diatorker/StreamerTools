package org.latuile.streamertools.Controller;

import org.latuile.streamertools.Model.Entity.PreferenceItem;
import org.latuile.streamertools.Model.Preferences;
import org.latuile.streamertools.Model.Repository.PreferenceRepository;
import org.latuile.streamertools.WebsocketMessages.NowPlayingInfos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Locale;

public abstract class NowPlayingController {
    protected PreferenceRepository preferenceRepository;

    @Autowired
    protected NowPlayingController(PreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @GetMapping("/widget")
    public String getWidget(
            Model model,
            @RequestParam(name = "mode", defaultValue = "full") String mode
    )
    {
        String Accent1 = preferenceRepository.findById(Preferences.Theme.COLOR_ACCENT1)
                .orElse(new PreferenceItem(Preferences.Theme.COLOR_ACCENT1, "green")).getItemValue();
        String Accent2 = preferenceRepository.findById(Preferences.Theme.COLOR_ACCENT2)
                .orElse(new PreferenceItem(Preferences.Theme.COLOR_ACCENT2, "yellow")).getItemValue();
        boolean EnableBorder = Boolean.parseBoolean(preferenceRepository.findById(Preferences.NowPlaying.BORDER_ENABLE)
                .orElse(new PreferenceItem(Preferences.NowPlaying.BORDER_ENABLE, "true")).getItemValue());

        model.addAttribute("color_text", preferenceRepository.findById(Preferences.Theme.COLOR_TEXT)
                .orElse(new PreferenceItem(Preferences.Theme.COLOR_TEXT, "white")));
        if (EnableBorder) {
            model.addAttribute("color_border", "linear-gradient(45deg, " + Accent1 + " 0%, " + Accent1 + " 40%, " + Accent2 + " 60%, " + Accent2 + " 100%)");
            model.addAttribute("color_background", preferenceRepository.findById(Preferences.Theme.COLOR_BACKGROUND)
                    .orElse(new PreferenceItem(Preferences.Theme.COLOR_BACKGROUND, "black")));
        }
        else {
            model.addAttribute("color_border", "transparent");
            model.addAttribute("color_background",  "transparent");
        }

        String prefFont = preferenceRepository.findById(Preferences.Theme.FONT)
                .orElse(new PreferenceItem(Preferences.Theme.FONT, "")).getItemValue();
        if(StringUtils.hasText(prefFont)) {
            model.addAttribute("font_family", "'" + prefFont + "', Roboto, Arial, sans-serif");
        }
        else {
            model.addAttribute("font_family", "Roboto, Arial, sans-serif");

        }

        model.addAttribute("border_radius", preferenceRepository.findById(Preferences.NowPlaying.BORDER_RADIUS)
                .orElse(new PreferenceItem(Preferences.NowPlaying.BORDER_RADIUS, "6px")));
        model.addAttribute("border_width", preferenceRepository.findById(Preferences.NowPlaying.BORDER_WIDTH)
                .orElse(new PreferenceItem(Preferences.NowPlaying.BORDER_WIDTH, "6px")));
        model.addAttribute("nothing_playing_behavior", preferenceRepository.findById(Preferences.NowPlaying.NOTHING_PLAYING_BEHAVIOR)
                .orElse(new PreferenceItem(Preferences.NowPlaying.NOTHING_PLAYING_BEHAVIOR, "placeholder")));

        String progressEnable = preferenceRepository.findById(Preferences.NowPlaying.PROGRESS_ENABLE)
                .orElse(new PreferenceItem(Preferences.NowPlaying.PROGRESS_ENABLE, "true")).getItemValue();
        model.addAttribute("progress_enable", progressEnable);

        if(progressEnable.equals("true")) {
            model.addAttribute("progress_color", preferenceRepository.findById(Preferences.NowPlaying.PROGRESS_COLOR)
                    .orElse(new PreferenceItem(Preferences.NowPlaying.PROGRESS_COLOR, "rgba(1, 1, 1, 0)")));
        }
        else {
            model.addAttribute("progress_color", preferenceRepository.findById(Preferences.Theme.COLOR_BACKGROUND)
                    .orElse(new PreferenceItem(Preferences.Theme.COLOR_BACKGROUND, "black")));
        }

        switch (mode.toLowerCase(Locale.ROOT)) {
            case "image":
            case "img":
                model.addAttribute("mode", "img");
//                return  "now-playing/img-widget";
                break;
            case "text":
            case "txt":
                model.addAttribute("mode", "txt");
//                return  "now-playing/txt-widget";
                break;
            default:
                model.addAttribute("mode", "full");
                break;
        }
        return "now-playing/widget";
    }

    @CrossOrigin("*")
    @GetMapping("/metadata")
    public abstract @ResponseBody NowPlayingInfos getMetadata();
}
