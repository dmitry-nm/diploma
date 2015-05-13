package com.md.art.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import java.io.FileOutputStream;
import java.math.BigInteger;

/**
 * Created by MD on 07.05.2015.
 */
public class ImageUtils {
    private static final String TAG="ImageUtils";
    private static final int IMG_WIDTH=8;
    private static final int IMG_HEIGHT=8;
    public static String buildHash(Bitmap img)
    {
        String hash="";
        //step 1: scale image
        Bitmap scaledImg=img.createScaledBitmap(img,IMG_WIDTH,IMG_HEIGHT,false);
        //step 2: convert image to gray
        scaledImg=toGrayscale(scaledImg);
        //scaledImg=normalizeBrightness(scaledImg);
        //step 3: calc average value of color
        int avg=0;
        for (int x=0;x<IMG_WIDTH;x++)
        {
            for (int y=0;y<IMG_HEIGHT;y++)
            {
                avg+=scaledImg.getPixel(x,y);
            }
        }
        avg/=IMG_HEIGHT*IMG_WIDTH;
        //step 4: some magic ;)
        boolean[] bits_chain=new boolean[IMG_WIDTH*IMG_HEIGHT];
        for (int x=0;x<IMG_WIDTH;x++)
        {
            for (int y=0;y<IMG_HEIGHT;y++)
            {
                bits_chain[IMG_WIDTH*x+y]=scaledImg.getPixel(x,y)>avg;
            }
        }
        //step 5: build hash
        long n=0;
        for (int i = 0; i < bits_chain.length; ++i) {
            n = (n << 1) + (bits_chain[i] ? 1 : 0);
        }
       // Log.i(TAG,"bin is "+Long.toBinaryString(n));
       // Log.i(TAG, "hex is " + Long.toHexString(n));
        String h=Long.toHexString(n);
        while (h.length()<16) h="0"+h;
        return h;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

   /* public Bitmap normalizeBrightness(Bitmap bmp)
    {
        int maxpixel=Color.BLACK;
        for (int x=0;x<bmp.getWidth();x++)
        {
            for (int y=0;y<bmp.getHeight();y++)
            {
                if (bmp.getPixel(x,y)>maxpixel) maxpixel=bmp.getPixel(x,y);
            }
        }
        int delta=0;
        if (maxpixel<0xffffffff) delta=0xffffffff-maxpixel;
        if (delta!=0)
        for (int x=0;x<bmp.getWidth();x++) {
            for (int y = 0; y < bmp.getHeight(); y++) {
                bmp.setPixel(x, y, bmp.getPixel(x, y) + delta);
            }
        }
        return  bmp;
    }*/

    public static int calcHammingDistance(String hash1,String hash2)
    {
        //if (hash1.length()!=hash2.length()) return -1;
        //convert hex to bits
        BigInteger i1 = new BigInteger(hash1, 16);
        BigInteger i2 = new BigInteger(hash2, 16);
        String hb1=Long.toBinaryString(i1.longValue());
        String hb2=Long.toBinaryString(i2.longValue());
        while (hb1.length()<64) hb1="0"+hb1;
        while (hb2.length()<64) hb2="0"+hb2;
     //   Log.i(TAG,"hb1="+hb1);
     //   Log.i(TAG, "hb2=" + hb2);
        if (hb1.length()!=hb2.length()) return -1;
        int d=0;
        for (int i=0;i<hb1.length();i++)
        {
            if (hb1.charAt(i)!=hb2.charAt(i)) d++;
        }
        return d;
    }

    public static void saveToFile(String filename,Bitmap bmp) {
        try {
            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory()+"/"+filename+".png");
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch(Exception e) {}
    }
}
