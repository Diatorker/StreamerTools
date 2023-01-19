package org.latuile.streamertools.WebsocketMessages;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PrediDisplayData {
    private String question;
    private Long totalAmount;
    private List<PrediChoice> choices;

    public PrediDisplayData() {
        choices = new ArrayList<>();
    }

    public void addPrompt(String prompt, Long count, Long amount, Boolean isFailed){
        choices.add(new PrediChoice(prompt, count, amount, isFailed));
    }
}

@Data @AllArgsConstructor
class PrediChoice {
    private String prompt;
    private Long predictionCount;
    private Long predictionAmount;
    private Boolean isFailed;
}
