package com.dishflash.controller;

import com.dishflash.dto.response.AddressResponse;
import com.dishflash.dto.response.PagedResponse;
import com.dishflash.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.findByUserId(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<AddressResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var addrPage = addressService.findAll(page, size);
        return ResponseEntity.ok(new PagedResponse<>(
                addrPage.getContent(), addrPage.getNumber(), addrPage.getSize(),
                addrPage.getTotalElements(), addrPage.getTotalPages(), addrPage.isLast()));
    }

    @GetMapping("/search/region")
    public ResponseEntity<List<AddressResponse>> searchByRegion(@RequestParam String q) {
        return ResponseEntity.ok(addressService.searchByRegion(q));
    }

    @GetMapping("/search/city")
    public ResponseEntity<List<AddressResponse>> searchByCity(@RequestParam String q) {
        return ResponseEntity.ok(addressService.searchByCity(q));
    }
}
