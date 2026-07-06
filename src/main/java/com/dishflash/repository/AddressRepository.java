package com.dishflash.repository;

import com.dishflash.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    List<Address> findByRegionBlindIndex(String regionBlindIndex);

    List<Address> findByCityBlindIndex(String cityBlindIndex);

    List<Address> findByPostalCodeBlindIndex(String postalCodeBlindIndex);
}
