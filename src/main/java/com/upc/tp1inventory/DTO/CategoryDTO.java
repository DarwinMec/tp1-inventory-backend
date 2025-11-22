package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryDTO {

    private UUID id;
    private String name;
    private String description;
}
