package com.pictureair.photopass.util;

import com.pictureair.photopass.entity.PhotoDownLoadInfo;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by pengwu on 16/8/10.
 */
public class PhotoDownLoadInfoSortUtil implements Comparator<PhotoDownLoadInfo> {
    @Override
    public int compare(PhotoDownLoadInfo info1, PhotoDownLoadInfo info2) {
        if (info1.getStatus().equalsIgnoreCase("true") && (info2.getStatus().equalsIgnoreCase("load") || info2.getStatus().equalsIgnoreCase("false"))){
            return -1;
        }else if (info1.getStatus().equalsIgnoreCase("load") && info2.getStatus().equalsIgnoreCase("false")){
            return -1;
        }else if (info1.getStatus().equalsIgnoreCase("load") && info2.getStatus().equalsIgnoreCase("true")){
            return 1;
        }else if (info1.getStatus().equalsIgnoreCase("false") && (info2.getStatus().equalsIgnoreCase("true") || info2.getStatus().equalsIgnoreCase("load"))){
            return 1;
        }else{
            if (info1.getStatus().equalsIgnoreCase("true") && info2.getStatus().equalsIgnoreCase("true")){
                Date date1 = AppUtil.getDateFromStr2(info1.getDownLoadTime());
                Date date2 = AppUtil.getDateFromStr2(info2.getDownLoadTime());
                return date1.compareTo(date2)*(-1);
            }else{
                return 0;
            }
        }
    }
}
