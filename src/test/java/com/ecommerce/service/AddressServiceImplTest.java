package com.ecommerce.service;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.exception.AccessDeniedException;
import com.ecommerce.exception.AddressLimitExceededException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceImplTest {
  @Mock
  private AddressRepository addressRepository;

  @Mock
  private UserService userService;

  @InjectMocks
  AddressServiceImpl addressService;

  @Test
  public void whenGetAddresses_returnAddresses() {
    User currentUser = new User();
    List<Address> addresses = List.of(new Address());
    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(addressRepository.findByUser(currentUser)).thenReturn(addresses);

    List<Address> returned_addresses = addressService.getAddressesForCurrentUser();

    verify(addressRepository).findByUser(currentUser);
    assertThat(returned_addresses).containsExactlyInAnyOrderElementsOf(addresses);
  }

  @Test
  public void whenSaveAddress_withNotFoundId_throwResourceNotFoundException() {
    AddressDto addressDto = new AddressDto();
    addressDto.setId(1L);

    when(userService.getCurrentUser()).thenReturn(new User());
    when(addressRepository.findById(addressDto.getId())).thenReturn(java.util.Optional.empty());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> addressService.saveAddress(addressDto)
    );

    verify(addressRepository, never()).save(any(Address.class));
    assertThat(exception.getMessage()).isEqualTo("Address with ID 1 not found.");
  }

  @Test
  public void whenSaveAddress_withIdAndMismatchingUserId_throwAccessDeniedException() {
    AddressDto addressDto = new AddressDto();
    addressDto.setId(1L);

    User currentUser = mock(User.class);
    User addressOwner = mock(User.class);
    Address existingAddress = new Address();
    existingAddress.setUser(addressOwner);

    when(currentUser.getId()).thenReturn(5L);
    when(addressOwner.getId()).thenReturn(10L);
    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(addressRepository.findById(1L)).thenReturn(Optional.of(existingAddress));

    AccessDeniedException exception = assertThrows(
        AccessDeniedException.class,
        () -> addressService.saveAddress(addressDto)
    );

    verify(addressRepository, never()).save(any(Address.class));
    assertThat(exception.getMessage()).isEqualTo("You are not authorized to edit this address.");
  }

  @Test
  public void whenSaveAddress_withIdAndMatchingUserId_saveSuccessfully() {
    AddressDto addressDto = new AddressDto();
    addressDto.setId(1L);
    addressDto.setAddressLine("New Address Line");
    addressDto.setCity("New City");
    addressDto.setCountry("New Country");
    addressDto.setName("New Name");
    addressDto.setPostalCode("54321");

    User currentUser = mock(User.class);
    Address existingAddress = new Address();
    existingAddress.setAddressLine("Old Address Line");
    existingAddress.setCity("Old City");
    existingAddress.setCountry("Old Country");
    existingAddress.setName("Old Name");
    existingAddress.setPostalCode("12345");
    existingAddress.setUser(currentUser);

    when(currentUser.getId()).thenReturn(5L);
    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(addressRepository.findById(1L)).thenReturn(Optional.of(existingAddress));

    addressService.saveAddress(addressDto);

    ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
    verify(addressRepository).save(addressCaptor.capture());
    Address savedAddress = addressCaptor.getValue();

    assertThat(savedAddress.getAddressLine()).isEqualTo("New Address Line");
    assertThat(savedAddress.getCity()).isEqualTo("New City");
    assertThat(savedAddress.getCountry()).isEqualTo("New Country");
    assertThat(savedAddress.getName()).isEqualTo("New Name");
    assertThat(savedAddress.getPostalCode()).isEqualTo("54321");
    assertThat(savedAddress.getUser()).isSameAs(currentUser);
  }

  @Test
  public void whenSaveAddress_withUserAddressesLimit_throwAddressLimitExceededException() {
    AddressDto addressDto = new AddressDto();

    User currentUser = mock(User.class);

    when(currentUser.getId()).thenReturn(5L);
    when(currentUser.getUsername()).thenReturn("Tom");
    when(currentUser.getAddresses()).thenReturn(
        List.of(new Address(), new Address(), new Address(), new Address(), new Address()));
    when(userService.getCurrentUser()).thenReturn(currentUser);

    AddressLimitExceededException exception = assertThrows(
        AddressLimitExceededException.class,
        () -> addressService.saveAddress(addressDto)
    );

    verify(addressRepository, never()).save(any(Address.class));
    assertThat(exception.getMessage()).isEqualTo(
        "User Tom with ID 5 cannot have more than 5 addresses.");
  }

  @Test
  public void whenSaveAddress_withoutId_saveSuccessfully() {
    AddressDto addressDto = new AddressDto();
    addressDto.setAddressLine("Address Line");
    addressDto.setCity("City");
    addressDto.setCountry("Country");
    addressDto.setName("Name");
    addressDto.setPostalCode("12345");

    User currentUser = new User();

    when(userService.getCurrentUser()).thenReturn(currentUser);

    addressService.saveAddress(addressDto);

    ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
    verify(addressRepository).save(addressCaptor.capture());
    Address savedAddress = addressCaptor.getValue();

    assertThat(savedAddress.getAddressLine()).isEqualTo("Address Line");
    assertThat(savedAddress.getCity()).isEqualTo("City");
    assertThat(savedAddress.getCountry()).isEqualTo("Country");
    assertThat(savedAddress.getName()).isEqualTo("Name");
    assertThat(savedAddress.getPostalCode()).isEqualTo("12345");
    assertThat(savedAddress.getUser()).isSameAs(currentUser);
  }

  @Test
  public void whenDeleteAddress_withNotFoundId_throwResourceNotFoundException() {
    Long addressId = 1L;

    when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> addressService.deleteAddress(addressId)
    );

    assertThat(exception.getMessage()).isEqualTo("Address with ID 1 not found.");
    verify(addressRepository, never()).delete(any(Address.class));
  }

  @Test
  public void whenDeleteAddress_withMismatchingUserId_throwAccessDeniedException() {
    Long addressId = 1L;
    User currentUser = mock(User.class);
    User addressOwner = mock(User.class);
    Address existingAddress = new Address();
    existingAddress.setUser(addressOwner);

    when(currentUser.getId()).thenReturn(5L);
    when(addressOwner.getId()).thenReturn(10L);
    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(addressRepository.findById(addressId)).thenReturn(Optional.of(existingAddress));

    AccessDeniedException exception = assertThrows(
        AccessDeniedException.class,
        () -> addressService.deleteAddress(addressId)
    );

    assertThat(exception.getMessage()).isEqualTo("You are not authorized to edit this address.");
    verify(currentUser, never()).removeAddress(any(Address.class));
  }

  @Test
  public void whenDeleteAddress_withValidData_removeSuccessfully() {
    Long addressId = 1L;
    User currentUser = mock(User.class);
    Address existingAddress = new Address();
    existingAddress.setUser(currentUser);

    when(currentUser.getId()).thenReturn(5L);
    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(addressRepository.findById(addressId)).thenReturn(Optional.of(existingAddress));

    addressService.deleteAddress(addressId);

    ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
    verify(currentUser).removeAddress(addressCaptor.capture());
    assertThat(addressCaptor.getValue()).isSameAs(existingAddress);
  }

  @Test
  public void whenIsNameTakenByUser_returnTrue() {
    User currentUser = mock(User.class);
    Address address = mock(Address.class);

    when(address.getId()).thenReturn(1L);
    when(address.getName()).thenReturn("Name");
    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(currentUser.getAddresses()).thenReturn(List.of(address));

    assertThat(addressService.isNameTakenByUser("Name", 2L)).isTrue();
  }

  @Test
  public void whenIsNameTakenByUser_returnFalse() {
    User currentUser = mock(User.class);
    Address address = mock(Address.class);

    when(address.getId()).thenReturn(1L);
    when(address.getName()).thenReturn("Name");
    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(currentUser.getAddresses()).thenReturn(List.of(address));

    assertThat(addressService.isNameTakenByUser("Name 2", 2L)).isFalse();
  }

  @Test
  public void whenIsNameTakenByUser_withSameAddressId_returnFalse() {
    User currentUser = mock(User.class);
    Address address = mock(Address.class);

    when(address.getId()).thenReturn(1L);
    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(currentUser.getAddresses()).thenReturn(List.of(address));

    assertThat(addressService.isNameTakenByUser("Home", 1L)).isFalse();
  }
}
