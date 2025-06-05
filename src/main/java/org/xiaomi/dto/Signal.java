package org.xiaomi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public  class Signal {
    @JsonProperty("Ix")
    private Double Ix;

    @JsonProperty("Ii")
    private Double Ii;

    @JsonProperty("Mi")
    private Double Mi;

    @JsonProperty("Mx")
    private Double Mx;
}