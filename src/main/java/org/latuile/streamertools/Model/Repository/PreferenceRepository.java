package org.latuile.streamertools.Model.Repository;

import org.latuile.streamertools.Model.Entity.PreferenceItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public interface PreferenceRepository extends CrudRepository<PreferenceItem, String> {
    default Map<String, String> getPreferenceMap() {
        return StreamSupport.stream(findAll().spliterator(), true)
                .collect(Collectors.toMap(PreferenceItem::getItemId, PreferenceItem::getItemValue));
    }
}
