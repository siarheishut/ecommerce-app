package com.ecommerce.service;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.exception.AccessDeniedException;
import com.ecommerce.exception.AddressLimitExceededException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
  private final AddressRepository addressRepository;
  private final UserService userService;

  private void OwnerCheck(Address address, User currentUser) {
    if (!address.getUser().getId().equals(currentUser.getId())) {
      throw new AccessDeniedException("You are not authorized to edit this address.");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Address> getAddressesForCurrentUser() {
    User currentUser = userService.getCurrentUser();
    return addressRepository.findByUser(currentUser);
  }

  @Override
  @Transactional
  public void saveAddress(AddressDto addressDto) {
    User currentUser = userService.getCurrentUser();
    Address address;

    if (addressDto.getId() != null) {
      address = addressRepository.findById(addressDto.getId())
          .orElseThrow(() -> new ResourceNotFoundException("Address with ID " + addressDto.getId() +
              " not found."));
      OwnerCheck(address, currentUser);
    } else {
      if (currentUser.getAddresses().size() >= 5) {
        throw new AddressLimitExceededException("User " + currentUser.getUsername() + " with ID "
            + currentUser.getId() + " cannot have more than 5 addresses.");
      }
      address = new Address();
      currentUser.addAddress(address);
    }

    address.setName(addressDto.getName());
    address.setAddressLine(addressDto.getAddressLine());
    address.setCity(addressDto.getCity());
    address.setPostalCode(addressDto.getPostalCode());
    address.setCountry(addressDto.getCountry());
    addressRepository.save(address);
  }

  @Override
  @Transactional
  public void deleteAddress(Long addressId) {
    User currentUser = userService.getCurrentUser();
    Address address = addressRepository.findById(addressId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Address with ID " + addressId + " not found."));
    OwnerCheck(address, currentUser);
    currentUser.removeAddress(address);
  }

  @Override
  public boolean isNameTakenByUser(String name, Long id) {
    return userService.getCurrentUser().getAddresses()
        .stream().filter(a -> !a.getId().equals(id))
        .anyMatch(a -> a.getName().equals(name));
  }
}
