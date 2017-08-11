package ru.otus_matveev_anton.genaral;

import java.util.Arrays;
import java.util.Objects;

public class AddresseeImpl implements Addressee{
    private final Address address;
    private final String groupName;

    public AddresseeImpl(String strAddress, String groupName) {
        Objects.requireNonNull(strAddress, "strAddress must not be null");

        try {
            if (strAddress.chars().allMatch(Character::isDigit)) {
                this.address = new ClientAddress(Integer.valueOf(strAddress));
            } else {
                this.address = SpecialAddress.valueOf(strAddress);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("strAddress must be a number or one of " + Arrays.toString(SpecialAddress.values()));
        }
        this.groupName = groupName;
    }

    public AddresseeImpl(Address address, String groupName) {
        Objects.requireNonNull(address, "strAddress must not be null");
        this.address = address;
        this.groupName = groupName;
    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return "AddresseeImpl{" +
                "address=" + address +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
