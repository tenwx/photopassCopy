package com.pictureair.photopassCopy.util;

import android.text.TextUtils;

import com.pictureair.photopassCopy.entity.PPinfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by pengwu on 16/12/15.
 */

public class PPInfoSortUtil implements Comparator<PPinfo> {
    @Override
    public int compare(PPinfo info1, PPinfo info2) {

        Date date1 = null;
        Date date2 = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        if (TextUtils.isEmpty(info1.getShootDate()) && TextUtils.isEmpty(info2.getShootDate())) {
            return 0;
        } else if (TextUtils.isEmpty(info1.getShootDate()) && !TextUtils.isEmpty(info2.getShootDate())) {
            return -1;
        } else if (!TextUtils.isEmpty(info1.getShootDate()) && TextUtils.isEmpty(info2.getShootDate())) {
            return 1;
        } else {
            try {
                date1 = df.parse(info1.getShootDate());
            } catch (ParseException e) {
                e.printStackTrace();
                date1 = null;
            }

            try {
                date2 = df.parse(info2.getShootDate());
            } catch (ParseException e) {
                e.printStackTrace();
                date2 = null;
            }

            if (date1 != null && date2 != null) {
                return date1.compareTo(date2) * (-1);
            } else if (date1 == null && date2 != null){
                return 1;
            } else if (date1 != null && date2 == null){
                return -1;
            } else {
                return 0;
            }
        }
    }
}
