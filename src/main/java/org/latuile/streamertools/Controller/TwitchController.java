package org.latuile.streamertools.Controller;

import com.github.twitch4j.pubsub.domain.PollData;
import com.github.twitch4j.pubsub.domain.PredictionEvent;
import org.latuile.streamertools.Model.Entity.PreferenceItem;
import org.latuile.streamertools.Model.Preferences;
import org.latuile.streamertools.Model.Repository.PreferenceRepository;
import org.latuile.streamertools.Service.TwitchClientService;
import org.latuile.streamertools.WebsocketMessages.PollDisplayData;
import org.latuile.streamertools.WebsocketMessages.PrediDisplayData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/twitch")
public class TwitchController {
    private final TwitchClientService twitchClientService;
    private final Logger logger = LoggerFactory.getLogger(TwitchController.class);
    private final PreferenceRepository preferenceRepository;

    @Autowired
    public TwitchController(TwitchClientService twitchClientService, PreferenceRepository preferenceRepository) {
        this.twitchClientService = twitchClientService;
        this.preferenceRepository = preferenceRepository;
    }


    // ========================
    // WIDGETS DISPLAY METHODS
    // ========================
    private void prepareTwitchWidgetModel(Model model, String pos, Boolean frame, Boolean debug) {
        String Accent1 = preferenceRepository.findById(Preferences.Theme.COLOR_ACCENT1)
                .orElse(new PreferenceItem(Preferences.Theme.COLOR_ACCENT1, "green")).getItemValue();
        String Accent2 = preferenceRepository.findById(Preferences.Theme.COLOR_ACCENT2)
                .orElse(new PreferenceItem(Preferences.Theme.COLOR_ACCENT2, "yellow")).getItemValue();

        model.addAttribute("color_text", preferenceRepository.findById(Preferences.Theme.COLOR_TEXT)
                .orElse(new PreferenceItem(Preferences.Theme.COLOR_TEXT, "white")));
        if (frame) {
            model.addAttribute("color_background", preferenceRepository.findById(Preferences.Theme.COLOR_BACKGROUND)
                    .orElse(new PreferenceItem(Preferences.Theme.COLOR_BACKGROUND, "black")));
            model.addAttribute("color_border", "linear-gradient(45deg, " + Accent1 + " 0%, " + Accent1 + " 40%, " + Accent2 + " 60%, " + Accent2 + " 100%)");
        }
        else {
            model.addAttribute("color_background",  "transparent");
            model.addAttribute("color_border", "transparent");
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

        model.addAttribute("choice_text_color", preferenceRepository.findById(Preferences.Twitch.CHOICE_TEXT_COLOR)
                .orElse(new PreferenceItem(Preferences.Twitch.CHOICE_TEXT_COLOR, "white")));
        model.addAttribute("choice_back_color", preferenceRepository.findById(Preferences.Twitch.CHOICE_BACK_COLOR)
                .orElse(new PreferenceItem(Preferences.Twitch.CHOICE_BACK_COLOR, "gray")));

        model.addAttribute("timeout", Integer.valueOf(preferenceRepository.findById(Preferences.Twitch.WIDGET_TIMEOUT)
                .orElse(new PreferenceItem(Preferences.Twitch.WIDGET_TIMEOUT, "10")).getItemValue()));
        model.addAttribute("metadataUrl", "https://localhost:8443/twitch/metadata/poll/");
        model.addAttribute("debugMode", debug);

        switch (pos){
            case "top":
                model.addAttribute("align", "start");
                model.addAttribute("justify", "center");
                break;
            case "left":
                model.addAttribute("align", "center");
                model.addAttribute("justify", "start");
                break;
            case "bottom":
                model.addAttribute("align", "end");
                model.addAttribute("justify", "center");
                break;
            case "right":
                model.addAttribute("align", "center");
                model.addAttribute("justify", "end");
                break;
            case "center":
            default:
                model.addAttribute("align", "center");
                model.addAttribute("justify", "center");
                break;
        }
    }


    @GetMapping("/poll")
    public String getPollWidget(
            Model model,
            @RequestParam(name = "pos", defaultValue = "center") String pos,
            @RequestParam(name="frame", defaultValue = "true") Boolean frame,
            @RequestParam(name="debug", defaultValue = "false") Boolean debug
    ){
        prepareTwitchWidgetModel(model, pos, frame, debug);

        return "twitch/poll";
    }

    @GetMapping("/prediction")
    public String getPrediWidget(
            Model model,
            @RequestParam(name = "pos", defaultValue = "center") String pos,
            @RequestParam(name="frame", defaultValue = "true") Boolean frame,
            @RequestParam(name="debug", defaultValue = "false") Boolean debug
    ){
        prepareTwitchWidgetModel(model, pos, frame, debug);

        return "twitch/prediction";
    }

    // ========================
    // WIDGETS DATA API METHODS
    // ========================
    @GetMapping("/metadata/poll")
    public @ResponseBody PollDisplayData getPollMetadata(){
        PollData pollData = twitchClientService.getPollData();
        PollDisplayData result = new PollDisplayData();

        if(pollData != null) {
            result.setQuestion(pollData.getTitle());
            Long maxVoteCnt = pollData.getChoices().stream()
                            .mapToLong(c -> c.getVotes().getTotal())
                            .max()
                            .orElse(0);
            result.setTotalAnswers(
                    pollData.getChoices().stream()
                            .mapToLong(choice -> {
                                result.addPrompt(
                                        choice.getTitle(),
                                        choice.getVotes().getTotal(),
                                        !choice.getVotes().getTotal().equals(maxVoteCnt) && pollData.getEndedAt() != null
                                );
                                return choice.getVotes().getTotal();
                            })
                            .sum()
            );
        }

        return result;
    }

    @GetMapping("/metadata/prediction")
    public @ResponseBody PrediDisplayData getPrediMetadata(){
        PredictionEvent prediData = twitchClientService.getPredictionEvent();
        PrediDisplayData result = new PrediDisplayData();

        if(prediData != null) {
            result.setQuestion(prediData.getTitle());
            result.setTotalAmount(
                    prediData.getOutcomes().stream()
                            .mapToLong(outcome -> {
                                result.addPrompt(
                                        outcome.getTitle(),
                                        outcome.getTotalUsers().longValue(),
                                        outcome.getTotalPoints().longValue(),
                                        !outcome.getId().equals(prediData.getWinningOutcomeId()) && prediData.getEndedAt() != null
                                );
                                return outcome.getTotalPoints().longValue();
                            })
                            .sum()
            );
        }

        return result;
    }
}
