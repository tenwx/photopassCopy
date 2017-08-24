package com.pictureair.hkdlphotopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/20.
 */
public class AddressJson implements Serializable{
    List<Address> outlets = null;

    public AddressJson() {
    }

    public AddressJson(List<Address> outlets) {
        this.outlets = outlets;
    }

    public List<Address> getOutlets() {
        return outlets;
    }

    public void setOutlets(List<Address> outlets) {
        this.outlets = outlets;
    }
}
