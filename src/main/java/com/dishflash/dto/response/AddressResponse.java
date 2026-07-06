package com.dishflash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private Long userId;
    private String region;
    private String city;
    private String street;
    private String houseApartment;
    private String postalCode;
}
