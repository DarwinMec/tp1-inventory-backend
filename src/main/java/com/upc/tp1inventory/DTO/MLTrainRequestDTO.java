package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MLTrainRequestDTO {

    private String startDate;
    private String endDate;

    private Boolean fastMode = true;
    private Boolean registerInDb = true;

    private String createdBy;

    private Boolean asyncMode = false;
}