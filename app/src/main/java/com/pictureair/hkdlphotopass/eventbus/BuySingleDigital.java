package com.pictureair.hkdlphotopass.eventbus;

/**
 * Created by tech-beyondren on 17/8/24.
 */

public class BuySingleDigital implements BaseBusEvent {
    private int position;
    public BuySingleDigital(int pos) {
        this.position = pos;
    }
    public int getPosition() {
        return position;
    }
}
