package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SupplierDTO {

    private UUID id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String region;
    private Boolean isActive;
}
