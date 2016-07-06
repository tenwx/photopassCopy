package com.pictureworks.android.util;


import android.graphics.Bitmap;
import android.graphics.Color;

import com.pictureair.jni.ciphermanager.PWJniUtil;

/**
 * Created by talon on 16/1/10.
 */
public class OCRUtils {

    static String[] strList = {"2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    static String[] errorStrList = {"~","～","(","（",")","）","<","<<","《",">",">>","》"};
    static String rightStr = "23456789ABCDEFGHJKMNPQRSTUVWXYZ-";
    /**
     * 获取SD卡路径。
     * @return
     */
//    public static String getSDPath() {
//        File sdDir = null;
//        boolean sdCardExist = Environment.getExternalStorageState().equals(
//                android.os.Environment.MEDIA_MOUNTED);
//        if (sdCardExist) {
//            sdDir = Environment.getExternalStorageDirectory();
//        }
//        return sdDir.toString();
//    }

    /**
     * 将识别结果 规范化，返回处理过的结果。
     * @param codeStr
     * @return
     */
    public static String dealCode(String codeStr){
        if (codeStr.length() >= 19){
            //取出前19位或者后19位。因为扫描的可能包含前半部分，也可能包含后半部分多余。
            if (String.valueOf(codeStr.charAt(4)).equals("-")){
                codeStr = codeStr.substring(0,19);// 码在前面，或者是正确的情况下。
            }else{
                codeStr = codeStr.substring(codeStr.length() - 19, codeStr.length()); //码在后面的情况。特殊情况。
            }
            //由于前面 两位 字符印刷问题经常识别不准确。故去掉前两位的检验标准。  由于前两位有可能识别成三位字符。故从最后 开始取值。
//            codeStr ="Q6FV" + codeStr.substring(codeStr.length() - 15 , codeStr.length());
            return codeStr;
        }else{
            return "";
        }
    }

    /**
     * 检查 Code 是否正确。
     * @param codeStr
     * @return
     */
    public static boolean checkCode(String codeStr) {
        //长度必须等于19位
        if (codeStr.length() != 19){
            return false;
        }
        //如果含有不符合规则的字符。
        for (int i = 0; i< errorStrList.length; i++){
            if (codeStr.contains(errorStrList[i])) {
                return false;
            }
        }
        //判断识别的每个字符都在 正确的String中，符合规则。
        for (int i = 0; i < codeStr.length(); i++) {
            if (!(rightStr.contains(String.valueOf(codeStr.charAt(i))))){
                return false;
            }
        }
        // 检查PP+生成算法规则。 //这里应该检查是否有三个 “－” 号。
        if (String.valueOf(codeStr.charAt(4)).equals("-") && String.valueOf(codeStr.charAt(9)).equals("-") && String.valueOf(codeStr.charAt(14)).equals("-")) {
            String[] reStr = codeStr.split("-");
            if (codeStr.substring(0,4).equals(PWJniUtil.getOcrStr1())){
                codeStr = PWJniUtil.getOcrStr2() + reStr[1] + reStr[2] + reStr[3].substring(0, reStr[3].length() - 1);
            }else{
                codeStr = reStr[0] + reStr[1] + reStr[2] + reStr[3].substring(0, reStr[3].length() - 1);
            }
            if (strList[getCheckNum(codeStr)].equals(String.valueOf(reStr[3].charAt(3)))) {
                return true;
            }
        }
        return false;
    }


    /**
     * 检查PP+ code 是否合法的算法。
     * @param code
     * @return
     */
    public static int getCheckNum(String code){
        code = code + "x";
        int sum = 0;
        int num = 0;
        for (int i = 1; i<= code.length(); i++){
            num = code.charAt(code.length() - i);
            if (i % 2 == 0) {
                sum = sum + getSingleNum(num * 2);
            } else {
                //奇数位获取键盘值，直接转换成个位数
                if (i > 1) {
                    sum = sum + getSingleNum(num);
                }
            }
        }

        if (sum % 10 == 0) {
            return 0;
        } else {
            return 10 - (sum % 10);
        }
    }

    public static int getSingleNum(int num){
        int result = 0;
        String strNum = String.valueOf(num);
        for (int i = 0; i < strNum.length(); i++) {
            result += Integer.valueOf(String.valueOf(strNum.charAt(i)));
        }
        if (result >= 10) {
            return getSingleNum(result);
        } else {
            return result;
        }
    }

    /**
     * 图像二值化。
     * @param img
     */
    public static Bitmap binarization(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int area = width * height;
        int gray[][] = new int[width][height];
        int average = 0;// 灰度平均值
        int graysum = 0;
        int graymean = 0;
        int grayfrontmean = 0;
        int graybackmean = 0;
        int pixelGray;
        int front = 0;
        int back = 0;
        int[] pix = new int[width * height];
        img.getPixels(pix, 0, width, 0, 0, width, height);
        for (int i = 1; i < width; i++) { // 不算边界行和列，为避免越界
            for (int j = 1; j < height; j++) {
                int x = j * width + i;
                int r = (pix[x] >> 16) & 0xff;
                int g = (pix[x] >> 8) & 0xff;
                int b = pix[x] & 0xff;
                pixelGray = (int) (0.3 * r + 0.59 * g + 0.11 * b);// 计算每个坐标点的灰度
                gray[i][j] = (pixelGray << 16) + (pixelGray << 8) + (pixelGray);
                graysum += pixelGray;
            }
        }
        graymean = (int) (graysum / area);// 整个图的灰度平均值
        average = graymean;
        for (int i = 0; i < width; i++) // 计算整个图的二值化阈值
        {
            for (int j = 0; j < height; j++) {
                if (((gray[i][j]) & (0x0000ff)) < graymean) {
                    graybackmean += ((gray[i][j]) & (0x0000ff));
                    back++;
                } else {
                    grayfrontmean += ((gray[i][j]) & (0x0000ff));
                    front++;
                }
            }
        }
        int frontvalue = (int) (grayfrontmean / front);// 前景中心
        int backvalue = (int) (graybackmean / back);// 背景中心
        float G[] = new float[frontvalue - backvalue + 1];// 方差数组
        int s = 0;
        for (int i1 = backvalue; i1 < frontvalue + 1; i1++)// 以前景中心和背景中心为区间采用大津法算法（OTSU算法）
        {
            back = 0;
            front = 0;
            grayfrontmean = 0;
            graybackmean = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (((gray[i][j]) & (0x0000ff)) < (i1 + 1)) {
                        graybackmean += ((gray[i][j]) & (0x0000ff));
                        back++;
                    } else {
                        grayfrontmean += ((gray[i][j]) & (0x0000ff));
                        front++;
                    }
                }
            }
            grayfrontmean = (int) (grayfrontmean / front);
            graybackmean = (int) (graybackmean / back);
            G[s] = (((float) back / area) * (graybackmean - average)
                    * (graybackmean - average) + ((float) front / area)
                    * (grayfrontmean - average) * (grayfrontmean - average));
            s++;
        }
        float max = G[0];
        int index = 0;
        for (int i = 1; i < frontvalue - backvalue + 1; i++) {
            if (max < G[i]) {
                max = G[i];
                index = i;
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int in = j * width + i;
                if (((gray[i][j]) & (0x0000ff)) < (index + backvalue)) {
                    pix[in] = Color.rgb(0, 0, 0);
                } else {
                    pix[in] = Color.rgb(255, 255, 255);
                }
            }
        }

        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        temp.setPixels(pix, 0, width, 0, 0, width, height);
        return temp;
    }

    /**
     * HDR 算法  让图片更鲜明
     * @param image
     * @return
     */
    public static Bitmap transform(Bitmap image) {
        float contrast = 2.5f; // default value; 对比度
        float brightness = 2.0f; // default value; 亮度
        int width = image.getWidth();
        int height = image.getHeight();
        // src.getRGB( 0, 0, width, height, inPixels, 0, width );
        int dst[] = new int[width * height];
        image.getPixels(dst, 0, width, 0, 0, width, height);

        // calculate RED, GREEN, BLUE means of pixel
        int index = 0;
        int[] rgbmeans = new int[3];
        double redSum = 0, greenSum = 0, blueSum = 0;
        double total = height * width;
        for (int row = 0; row < height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for (int col = 0; col < width; col++) {
                index = row * width + col;
                ta = (dst[index] >> 24) & 0xff;
                tr = (dst[index] >> 16) & 0xff;
                tg = (dst[index] >> 8) & 0xff;
                tb = dst[index] & 0xff;
                redSum += tr;
                greenSum += tg;
                blueSum += tb;
            }
        }

        rgbmeans[0] = (int) (redSum / total);
        rgbmeans[1] = (int) (greenSum / total);
        rgbmeans[2] = (int) (blueSum / total);

        // adjust contrast and brightness algorithm, here
        for (int row = 0; row < height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for (int col = 0; col < width; col++) {
                index = row * width + col;
                ta = (dst[index] >> 24) & 0xff;
                tr = (dst[index] >> 16) & 0xff;
                tg = (dst[index] >> 8) & 0xff;
                tb = dst[index] & 0xff;

                // remove means
                tr -= rgbmeans[0];
                tg -= rgbmeans[1];
                tb -= rgbmeans[2];

                // adjust contrast now !!!
                tr = (int) (tr * contrast);
                tg = (int) (tg * contrast);
                tb = (int) (tb * contrast);

                // adjust brightness
                tr += (int) (rgbmeans[0] * brightness);
                tg += (int) (rgbmeans[1] * brightness);
                tb += (int) (rgbmeans[2] * brightness);
                dst[index] = (ta << 24) | (clamp(tr) << 16) | (clamp(tg) << 8)
                        | clamp(tb);
            }
        }
        Bitmap acrossFlushBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        acrossFlushBitmap.setPixels(dst, 0, width, 0, 0, width, height);
        return acrossFlushBitmap;
    }
    public static int clamp(int c) {
        return c > 255 ? 255 : ((c < 0) ? 0 : c);
    }

}
