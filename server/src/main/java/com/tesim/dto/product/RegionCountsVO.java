package com.tesim.dto.product;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * Serializes as a flat region→count object for frontend compatibility.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionCountsVO {

    @JsonIgnore
    private Map<String, Long> counts = Collections.emptyMap();

    @JsonAnyGetter
    public Map<String, Long> any() {
        return counts == null ? Collections.emptyMap() : counts;
    }
}
