package com.pictureair.photopass.util;

import com.pictureair.photopass.entity.PPinfo;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by talon on 15/12/26.
 */
public class PPinfoSortUtil implements Comparator<PPinfo>{

    @Override
    public int compare(PPinfo ppInfo1, PPinfo ppInfo2) {
        int ppInfo1_photoCount = ppInfo1.getPhotoCount();
        int ppInfo2_photoCount = ppInfo2.getPhotoCount();

        Date ppInfo1_date = AppUtil.getDateFromStr1(ppInfo1.getShootDate());
        Date ppInfo2_date = AppUtil.getDateFromStr1(ppInfo2.getShootDate());

        if (ppInfo1_photoCount == 0){
            return 1;
        }
        else{
            if (ppInfo1_date != null && ppInfo2_date != null){
                return ppInfo2_date.compareTo(ppInfo1_date);
            }
        }


        return -1;
    }
}
