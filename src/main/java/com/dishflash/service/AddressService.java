package com.dishflash.service;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.response.AddressResponse;
import com.dishflash.entity.Address;
import com.dishflash.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private static final String CONTEXT_REGION = "addresses:region";
    private static final String CONTEXT_CITY = "addresses:city";
    private static final String CONTEXT_STREET = "addresses:street";
    private static final String CONTEXT_HOUSE = "addresses:house_apartment";
    private static final String CONTEXT_POSTAL = "addresses:postal_code";

    private final AddressRepository addressRepository;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    public List<AddressResponse> findByUserId(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AddressResponse> findAll(int page, int size) {
        return addressRepository.findAll(PageRequest.of(page, size)).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> searchByRegion(String regionQuery) {
        if (regionQuery == null || regionQuery.isBlank()) return List.of();
        String blindIndex = cryptoService.blindIndex(regionQuery, CONTEXT_REGION);
        return addressRepository.findByRegionBlindIndex(blindIndex).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> searchByCity(String cityQuery) {
        if (cityQuery == null || cityQuery.isBlank()) return List.of();
        String blindIndex = cryptoService.blindIndex(cityQuery, CONTEXT_CITY);
        return addressRepository.findByCityBlindIndex(blindIndex).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse toResponse(Address address) {
        if (address == null) return null;
        byte[] regionBytes = cryptoService.decrypt(address.getRegionEncrypted(), CONTEXT_REGION);
        byte[] cityBytes = cryptoService.decrypt(address.getCityEncrypted(), CONTEXT_CITY);
        byte[] streetBytes = cryptoService.decrypt(address.getStreetEncrypted(), CONTEXT_STREET);
        byte[] houseBytes = cryptoService.decrypt(address.getHouseApartmentEncrypted(), CONTEXT_HOUSE);
        byte[] postalBytes = cryptoService.decrypt(address.getPostalCodeEncrypted(), CONTEXT_POSTAL);

        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUser() != null ? address.getUser().getId() : null)
                .region(regionBytes != null ? new String(regionBytes, StandardCharsets.UTF_8) : null)
                .city(cityBytes != null ? new String(cityBytes, StandardCharsets.UTF_8) : null)
                .street(streetBytes != null ? new String(streetBytes, StandardCharsets.UTF_8) : null)
                .houseApartment(houseBytes != null ? new String(houseBytes, StandardCharsets.UTF_8) : null)
                .postalCode(postalBytes != null ? new String(postalBytes, StandardCharsets.UTF_8) : null)
                .build();
    }
}
