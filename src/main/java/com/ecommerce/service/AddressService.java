package com.ecommerce.service;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> getAddressesForCurrentUser();

    void deleteAddress(Long addressId);

    void saveAddress(AddressDto addressDto);

    boolean isNameTakenByUser(String name, Long id);
}
