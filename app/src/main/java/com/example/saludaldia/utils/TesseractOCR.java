package com.example.saludaldia.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TesseractOCR {
    private TessBaseAPI tessBaseAPI;

    public TesseractOCR(Context context) {
        tessBaseAPI = new TessBaseAPI();
        String dataPath = context.getFilesDir() + "/tesseract/";
        checkFile(new File(dataPath + "tessdata/"), context);
        tessBaseAPI.init(dataPath, "spa");
    }

    private void checkFile(File dir, Context context) {
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(context);
        }
        if (dir.exists()) {
            String datafilepath = dir + "/spa.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles(context);
            }
        }
    }

    private void copyFiles(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream in = assetManager.open("tessdata/spa.traineddata");
            String outPath = context.getFilesDir() + "/tesseract/tessdata/spa.traineddata";
            OutputStream out = new FileOutputStream(outPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOCRResult(Bitmap bitmap) {
        tessBaseAPI.setImage(bitmap);
        return tessBaseAPI.getUTF8Text();
    }

    public void release() {
        if (tessBaseAPI != null) {
            tessBaseAPI.end();
        }
    }
}
