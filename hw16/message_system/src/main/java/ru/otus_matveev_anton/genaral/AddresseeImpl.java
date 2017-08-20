package ru.otus_matveev_anton.genaral;

import java.util.Arrays;
import java.util.Objects;

public class AddresseeImpl implements Addressee{
    private final String groupName;
    private final Address address;

    public AddresseeImpl(String strAddress, String groupName) {
        Objects.requireNonNull(strAddress, "strAddress must not be null");

        if (strAddress.isEmpty()){
            throw new IllegalArgumentException("strAddress must not be empty");
        }

        if (Arrays.stream(SpecialAddress.values()).map(Enum::toString).anyMatch(strAddress::equals)) {
            this.address = SpecialAddress.valueOf(strAddress);
        } else {
            this.address = new ClientAddress(strAddress);
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
        return "{" +
                "groupName='" + groupName + '\'' +
                ", address=" + address +
                '}';
    }
}
