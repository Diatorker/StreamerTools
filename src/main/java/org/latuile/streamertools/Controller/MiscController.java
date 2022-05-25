package org.latuile.streamertools.Controller;

import org.latuile.streamertools.Model.Entity.PreferenceItem;
import org.latuile.streamertools.Model.Preferences;
import org.latuile.streamertools.Model.Repository.PreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MiscController {
    private final PreferenceRepository preferenceRepository;

    @Autowired
    public MiscController(PreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @GetMapping("/timer/widget")
    public String showTimerWidget(@RequestParam Integer time, Model model){
        model.addAttribute("time", time);
        model.addAttribute("color_accent1", preferenceRepository.findById(Preferences.Theme.COLOR_ACCENT1)
                                                                            .orElse(new PreferenceItem(Preferences.Theme.COLOR_ACCENT1, "green")));
        model.addAttribute("color_accent2", preferenceRepository.findById(Preferences.Theme.COLOR_ACCENT2)
                                                                            .orElse(new PreferenceItem(Preferences.Theme.COLOR_ACCENT2, "yellow")));
        return "misc/timer";
    }
}
