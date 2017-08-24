package com.pictureair.hkdlphotopass.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * zip压缩解压工具类
 */
public class ZipUtil {
    /**
     * 功能：压缩成zip
     *
     * @param zipFilep 压缩后的zip文件名
     * @param path     压缩路径
     * @throws Exception
     */
    public static boolean zip(String zipFilep, String path) {
        File zipFile = new File(zipFilep);
        try {
            if (!zipFile.exists()) {
                zipFile.createNewFile();
            }
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
            write2zip(out, path, "");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 功能：压缩成zip
     *
     * @param zipName 压缩后的zip文件名
     * @param paths     压缩路径
     * @throws Exception
     */
    public static boolean zip(String zipName, ArrayList<String> paths) {
        File zipFile = new File(zipName);
        try {
            if (!zipFile.exists()) {
                zipFile.createNewFile();
            }
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
            write2zip(out, paths, "");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 功能：写压缩流
     *
     * @param out  压缩输出流
     * @param path 压缩路径
     * @param base 压缩式的基础目录
     * @throws Exception
     */
    private static void write2zip(ZipOutputStream out, String path, String base) throws Exception {
        File file = new File(path);
        if (file.isDirectory()) {// 文件夹，递归
            base = base.length() == 0 ? "" : base + File.separator;
            File[] tempFiles = file.listFiles();
            for (int i = 0; i < tempFiles.length; i++) {
                write2zip(out, tempFiles[i].getPath(), base + tempFiles[i].getName());
            }
        } else {// 文件，压缩
            byte[] buff = new byte[2048];
            int bytesRead = -1;
            ZipEntry entry = new ZipEntry(base);
            out.putNextEntry(entry);
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            while (-1 != (bytesRead = in.read(buff, 0, buff.length))) {
                out.write(buff, 0, bytesRead);
            }
            in.close();
            out.flush();
        }
    }

    /**
     * 功能：写压缩流
     *
     * @param out  压缩输出流
     * @param paths 压缩路径
     * @param base 压缩式的基础目录
     * @throws Exception
     */
    private static void write2zip(ZipOutputStream out, ArrayList<String> paths, String base) throws Exception {
        for (String pathStr : paths) {
            File file = new File(pathStr);
            if (file.isDirectory()) {// 文件夹，递归
                base = base.length() == 0 ? "" : base + File.separator;
                File[] tempFiles = file.listFiles();
                for (int i = 0; i < tempFiles.length; i++) {
                    write2zip(out, tempFiles[i].getPath(), base + tempFiles[i].getName());
                }
            } else {// 文件，压缩
                byte[] buff = new byte[2048];
                int bytesRead = -1;
                ZipEntry entry = new ZipEntry(base);
                out.putNextEntry(entry);
                InputStream in = new BufferedInputStream(new FileInputStream(file));
                while (-1 != (bytesRead = in.read(buff, 0, buff.length))) {
                    out.write(buff, 0, bytesRead);
                }
                in.close();
                out.flush();
            }
        }
    }

    /**
     * 功能：解压缩
     *
     * @param zipFilePath zip文件路径
     * @param destPath    解压缩路径
     * @throws Exception
     */
    public static void unZip(String zipFilePath, String destPath) throws Exception {
        File zipFile = new File(zipFilePath);
        ZipFile zip = new ZipFile(zipFile, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE);
        Enumeration<?> enu = zip.entries();// 得到压缩文件夹中的所有文件
        while (enu.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) enu.nextElement();
            String file = destPath + entry.getName();
            write2file(zip, entry, file);
        }
    }

    /**
     * 功能：解压缩
     *
     * @param zipFile  zip文件
     * @param destPath 解压缩路径
     * @throws Exception
     */
    public static void unZip(File zipFile, String destPath) throws Exception {
        ZipFile zip = new ZipFile(zipFile, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE);
        Enumeration<?> enu = zip.entries();// 得到压缩文件夹中的所有文件
        while (enu.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) enu.nextElement();
            String file = destPath + entry.getName();
            write2file(zip, entry, file);
        }
    }

    /**
     * 功能：将解压缩之后的文件写入目录
     *
     * @param zip   压缩文件
     * @param entry 压缩文件实体——压缩文件中的文件
     * @param file  解压缩之后的文件路径
     * @throws Exception
     */
    private static void write2file(ZipFile zip, ZipEntry entry, String file) throws Exception {
        if (entry.isDirectory()) {
            File f = new File(file);
            f.mkdirs();
        } else {
            File f = new File(file);
            createDir(f);
            FileOutputStream fos = new FileOutputStream(f);
            byte[] buffer = new byte[8196];//8K
            if (entry != null) {
                InputStream is = zip.getInputStream(entry);
                if (is != null) {
                    for (int len = is.read(buffer, 0, buffer.length); len != -1; len = is.read(buffer, 0, 8196)) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            fos.close();
        }
    }

    /**
     * 功能：创建目录
     *
     * @param file 文件或目录
     */
    private static void createDir(File file) {
        if (file.isDirectory() && !file.exists()) {
            file.mkdirs();
        } else {
            String path = file.getPath();
            int i = 0;
            if (path.contains("\\")) {//window下的文件系统
                i = path.lastIndexOf("\\");
            } else {
                i = path.lastIndexOf(File.separator);//linux下的文件系统
            }
            path = path.substring(0, i);
            new File(path).mkdirs();
        }
    }

    /**
     * 读文件在./data/data/com.srcb.mbank/files/下面
     *
     * @param zipPath：.zip文件路径
     * @param fileName：文件名
     * @return
     */
    public static Bitmap readFileData(String zipPath, String fileName) {
        try {
            File file = new File(zipPath);
            if (!file.exists()) {
                return null;
            }
            ZipFile zip = new ZipFile(file);
            Enumeration<?> enu = zip.entries();// 得到压缩文件夹中的所有文件
            while (enu.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) enu.nextElement();
                String name = entry.getName();
                if (entry.getName().contains(fileName.concat(".png"))) {
                    return BitmapFactory.decodeStream(zip.getInputStream(entry));
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        } finally {
        }
    }
}
