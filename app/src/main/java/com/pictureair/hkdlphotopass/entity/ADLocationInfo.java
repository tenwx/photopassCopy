package com.pictureair.hkdlphotopass.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by bauer_bao on 16/11/30.
 */

@Entity
public class ADLocationInfo {
    @Id
    private Long id;//自增长id
    private String locationId;
    private String descriptionCH;//广告中文
    private String descriptionEN;//广告英文
    @Generated(hash = 1401395519)
    public ADLocationInfo(Long id, String locationId, String descriptionCH,
            String descriptionEN) {
        this.id = id;
        this.locationId = locationId;
        this.descriptionCH = descriptionCH;
        this.descriptionEN = descriptionEN;
    }
    @Generated(hash = 209418185)
    public ADLocationInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getLocationId() {
        return this.locationId;
    }
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
    public String getDescriptionCH() {
        return this.descriptionCH;
    }
    public void setDescriptionCH(String descriptionCH) {
        this.descriptionCH = descriptionCH;
    }
    public String getDescriptionEN() {
        return this.descriptionEN;
    }
    public void setDescriptionEN(String descriptionEN) {
        this.descriptionEN = descriptionEN;
    }
}
