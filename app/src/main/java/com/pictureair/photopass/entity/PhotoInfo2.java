package com.pictureair.photopass.entity;

import org.greenrobot.greendao.annotation.Entity;

import java.util.List;

/**
 * 图片封装类
 *
 * @author bauer_bao
 */
@Entity
public class PhotoInfo2 {
    /**
     * url : media/fff048f44d6deda7710f7bbb371b875a6b45f25c55b722d989880fa987b8a7fbc3a9555b1adb6166b83e0f43b075d3137d858624717a1d81ada10e5e3edb5779
     */

    private OriginalInfoBean originalInfo;
    /**
     * x512 : {"url":"media/7e1fbf09e23ac50aae1421c3fadd101ab3fcd88949b2b7d2ebc19837f6ac262ef53d312562a3c0eaacb714c9ccf09e943d72cef0aead420b80ccacd3b84678c1ef66cb886012940e12271a30e12e858b"}
     * x1024 : {"url":"media/7e1fbf09e23ac50aae1421c3fadd101a10a0a9e50d6c3739c32ad92e160bc657ddeb7ad12971fbc44f088d189cecc27f4be03bae8d0363d94bb0d52008585fc22ee119776a76ed1fccb8307afd322e4f","height":683,"width":1024}
     * x128 : {"url":"media/7e1fbf09e23ac50aae1421c3fadd101ab3fcd88949b2b7d2ebc19837f6ac262ef21efd76ce649fb7ec87379aba345cc3cfdb0b26af103f4e2b43b15fcbccc7aa01bf9c318e5afab44634c70203eece96"}
     */

    private ThumbnailBean thumbnail;
    /**
     * originalInfo : {"url":"media/fff048f44d6deda7710f7bbb371b875a6b45f25c55b722d989880fa987b8a7fbc3a9555b1adb6166b83e0f43b075d3137d858624717a1d81ada10e5e3edb5779"}
     * thumbnail : {"x512":{"url":"media/7e1fbf09e23ac50aae1421c3fadd101ab3fcd88949b2b7d2ebc19837f6ac262ef53d312562a3c0eaacb714c9ccf09e943d72cef0aead420b80ccacd3b84678c1ef66cb886012940e12271a30e12e858b"},"x1024":{"url":"media/7e1fbf09e23ac50aae1421c3fadd101a10a0a9e50d6c3739c32ad92e160bc657ddeb7ad12971fbc44f088d189cecc27f4be03bae8d0363d94bb0d52008585fc22ee119776a76ed1fccb8307afd322e4f","height":683,"width":1024},"x128":{"url":"media/7e1fbf09e23ac50aae1421c3fadd101ab3fcd88949b2b7d2ebc19837f6ac262ef21efd76ce649fb7ec87379aba345cc3cfdb0b26af103f4e2b43b15fcbccc7aa01bf9c318e5afab44634c70203eece96"}}
     * shootDate : 2016-10-20
     * presetId : 000000000000000000000000
     * strShootOn : 2016-10-20 10:06:57
     * enImage : false
     * customerIds : [{"code":"SHDRC22AMWK5H4NB"}]
     * _id : 580827e8a482ecfc510000f2
     * receivedOn : 2016-10-20T02:06:57.000Z
     * mimeType : jpg
     * locationId : lk2
     * modifiedOn : 2016-10-20T06:46:42.558Z
     * shootOn : 2016-10-20T02:06:57.000Z
     * isPaid : true
     */

    private String shootDate;
    private String presetId;
    private String strShootOn;
    private boolean enImage;
    private String _id;
    private String receivedOn;
    private String mimeType;
    private String locationId;
    private String modifiedOn;
    private String shootOn;
    private boolean isPaid;
    /**
     * code : SHDRC22AMWK5H4NB
     */

    private List<CustomerIdsBean> customerIds;

    public OriginalInfoBean getOriginalInfo() {
        return originalInfo;
    }

    public void setOriginalInfo(OriginalInfoBean originalInfo) {
        this.originalInfo = originalInfo;
    }

    public ThumbnailBean getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(ThumbnailBean thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getShootDate() {
        return shootDate;
    }

    public void setShootDate(String shootDate) {
        this.shootDate = shootDate;
    }

    public String getPresetId() {
        return presetId;
    }

    public void setPresetId(String presetId) {
        this.presetId = presetId;
    }

    public String getStrShootOn() {
        return strShootOn;
    }

    public void setStrShootOn(String strShootOn) {
        this.strShootOn = strShootOn;
    }

    public boolean isEnImage() {
        return enImage;
    }

    public void setEnImage(boolean enImage) {
        this.enImage = enImage;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getReceivedOn() {
        return receivedOn;
    }

    public void setReceivedOn(String receivedOn) {
        this.receivedOn = receivedOn;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(String modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public String getShootOn() {
        return shootOn;
    }

    public void setShootOn(String shootOn) {
        this.shootOn = shootOn;
    }

    public boolean isIsPaid() {
        return isPaid;
    }

    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    public List<CustomerIdsBean> getCustomerIds() {
        return customerIds;
    }

    public void setCustomerIds(List<CustomerIdsBean> customerIds) {
        this.customerIds = customerIds;
    }

    public static class OriginalInfoBean {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class ThumbnailBean {
        /**
         * url : media/7e1fbf09e23ac50aae1421c3fadd101ab3fcd88949b2b7d2ebc19837f6ac262ef53d312562a3c0eaacb714c9ccf09e943d72cef0aead420b80ccacd3b84678c1ef66cb886012940e12271a30e12e858b
         */

        private X512Bean x512;
        /**
         * url : media/7e1fbf09e23ac50aae1421c3fadd101a10a0a9e50d6c3739c32ad92e160bc657ddeb7ad12971fbc44f088d189cecc27f4be03bae8d0363d94bb0d52008585fc22ee119776a76ed1fccb8307afd322e4f
         * height : 683
         * width : 1024
         */

        private X1024Bean x1024;
        /**
         * url : media/7e1fbf09e23ac50aae1421c3fadd101ab3fcd88949b2b7d2ebc19837f6ac262ef21efd76ce649fb7ec87379aba345cc3cfdb0b26af103f4e2b43b15fcbccc7aa01bf9c318e5afab44634c70203eece96
         */

        private X128Bean x128;

        public X512Bean getX512() {
            return x512;
        }

        public void setX512(X512Bean x512) {
            this.x512 = x512;
        }

        public X1024Bean getX1024() {
            return x1024;
        }

        public void setX1024(X1024Bean x1024) {
            this.x1024 = x1024;
        }

        public X128Bean getX128() {
            return x128;
        }

        public void setX128(X128Bean x128) {
            this.x128 = x128;
        }

        public static class X512Bean {
            private String url;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }

        public static class X1024Bean {
            private String url;
            private int height;
            private int width;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }
        }

        public static class X128Bean {
            private String url;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }

    public static class CustomerIdsBean {
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
