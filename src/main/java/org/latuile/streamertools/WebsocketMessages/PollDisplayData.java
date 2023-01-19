package org.latuile.streamertools.WebsocketMessages;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PollDisplayData {
    private String question;
    private Long totalAnswers;
    private List<PollChoice> choices;

    public PollDisplayData() {
        choices = new ArrayList<>();
    }

    public void addPrompt(String prompt, Long answers, Boolean isFailed){
        choices.add(new PollChoice(prompt, answers, isFailed));
    }
}

@Data @AllArgsConstructor
class PollChoice {
    private String prompt;
    private Long answerCount;
    private Boolean isFailed;
}
