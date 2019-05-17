package com.modori.kwonkiseokee.AUto.Service;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.modori.kwonkiseokee.AUto.R;
import com.modori.kwonkiseokee.AUto.Util.MakePreferences;
import com.modori.kwonkiseokee.AUto.data.DevicePhotoDTO;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;

public class SetWallpaperJob extends BroadcastReceiver {

    private final String[] okFileExtensions = new String[]{"jpg", "jpeg", "png", "gif"};


    public static final String PREFS_FILE = "PrefsFile";
    String SelectedPath;
    String TAG = "SetWallpaperJob";
    Boolean ShuffleMode;
    int GET_SETTING;
    static int FileNumber = 0;
    Realm realm;



    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        MakePreferences.getInstance().setSettings(context);
        Realm.init(context);
        realm = Realm.getDefaultInstance();

        //load preferences
        SelectedPath = MakePreferences.getInstance().getSettings().getString("SelectedPath", "/system");
        ShuffleMode = MakePreferences.getInstance().getSettings().getBoolean("ShuffleMode", false);

        Log.d("SetWallpaperJob", "신호 받음");
        GET_SETTING = MakePreferences.getInstance().getSettings().getInt("GetSetting", 0);
        if (GET_SETTING == 0) {
            // 사용자가 선택한 폴더에서 불러온다.
            try {


                String FileName;

                File file = new File(SelectedPath);

                File[] imageFiles = file.listFiles();

                for (int i = 0; i < imageFiles.length; i++) {

                    file = imageFiles[i];

                    Log.d("찾는 for 문 진입 ", String.valueOf(i));
                    //File file = files[i];

                    if (file.canRead()) {
                        for (int k = 0; k <= 3; k++) {
                            String checkFile = okFileExtensions[k];
                            if (file.getName().toLowerCase().endsWith(checkFile)) {
                                imageFiles[i] = new File(String.valueOf(file.getName()));
                                Log.d("찾은 파일", String.valueOf(imageFiles[i]));
                            }
                        }
                    }
                }

                //
                if (imageFiles.length > 0) {

                    if (ShuffleMode) {
                        final Random myRandom = new Random();
                        FileNumber = myRandom.nextInt(imageFiles.length);
                        FileName = imageFiles[FileNumber].getName();
                    } else {
                        FileName = imageFiles[FileNumber++].getName();
                    }
                    if (FileNumber == imageFiles.length)
                        FileNumber = 0;

                    final WallpaperManager wallpaperManager =
                            WallpaperManager.getInstance(context);


                    Bitmap myBitmap =
                            BitmapFactory.decodeFile(SelectedPath + "/" + FileName);

                    wallpaperManager.setBitmap(myBitmap);

                } else {
                    String alert = context.getResources().getString(R.string.noImageAlert);
                    throw new Exception(alert);
                }

            } catch (Exception ae) {
                Toast.makeText(context, ae.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if (GET_SETTING == 1) {
            // 사용자가 갤러리에서 선택한 사진을 불러온다.

            List<String> onlyPhotoUri = new ArrayList<>();
            RealmResults<DevicePhotoDTO> realmResults = realm.where(DevicePhotoDTO.class).findAll();
            for (int i = 0; i < realmResults.size(); i++) {
                onlyPhotoUri.add(realmResults.get(i).getPhotoUri_d());
            }

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(onlyPhotoUri.get(0)));
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }

            WallpaperManager wallpaperManager =
                    WallpaperManager.getInstance(context);

            try {
                wallpaperManager.setBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }
        }


        //End

    }//end onReceive


    private class OnlyExt implements FilenameFilter {
        String ext;

        public OnlyExt(String ext) {
            this.ext = "." + ext;
        }

        public boolean accept(File dir, String name) {
            return name.endsWith(ext);
        }
    }

}//end main class