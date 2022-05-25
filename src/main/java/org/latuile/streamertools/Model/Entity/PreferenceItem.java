package org.latuile.streamertools.Model.Entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class PreferenceItem {

    @Id
    String ItemId;
    String ItemValue;

    @Override
    public String toString() {
        return ItemValue;
    }
}
