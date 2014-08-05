package com.bhj.setclip;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PutClipService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getTask() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
        return foregroundTaskInfo .topActivity.getPackageName();
    }

    @Override
    public int onStartCommand(Intent intent,  int flags,  int startId)  {
        try {
            String picName = intent.getStringExtra("picture");
            if (picName != null) {
                picName = picName.replaceFirst("^/sdcard/", "");
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), picName))));
            } else if (intent.getIntExtra("gettask", 0) == 1) {
                String foregroundTaskPackageName = getTask();
                FileWriter f = new FileWriter(new File(Environment.getExternalStorageDirectory(), "putclip.txt.1"));
                f.write(foregroundTaskPackageName);
                f.close();
                File txt = new File(Environment.getExternalStorageDirectory(), "putclip.txt.1");
                txt.renameTo(new File(Environment.getExternalStorageDirectory(), "putclip.txt"));
            } else if (intent.getIntExtra("getclip", 0) == 1) {
                FileWriter f = new FileWriter(new File(Environment.getExternalStorageDirectory(), "putclip.txt.1"));
                ClipboardManager mClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                String str = mClipboard.getPrimaryClip().getItemAt(0).getText().toString();
                f.write(str);
                f.close();
                File txt = new File(Environment.getExternalStorageDirectory(), "putclip.txt.1");
                txt.renameTo(new File(Environment.getExternalStorageDirectory(), "putclip.txt"));
            } else {
                FileReader f = new FileReader(new File(Environment.getExternalStorageDirectory(), "putclip.txt"));
                if (getTask().equals("com.tencent.mm")) {
                    f.close();
                    f = new FileReader(new File(Environment.getExternalStorageDirectory(), "putclip-wx.txt"));
                }
                char[] buffer = new char[1024 * 1024];
                int n = f.read(buffer);
                String str = new String(buffer, 0, n);
                Log.e("bhj", String.format("%s:%d: str is %s", "PutClipService.java", 36, str));
                ClipboardManager mClipboard;
                mClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                mClipboard.setPrimaryClip(ClipData.newPlainText("Styled Text", str));
                File putclip = new File(Environment.getExternalStorageDirectory(), "putclip.txt");
                putclip.delete();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong in putclip: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    }
}