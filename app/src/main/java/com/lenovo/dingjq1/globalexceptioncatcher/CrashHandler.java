package com.lenovo.dingjq1.globalexceptioncatcher;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dingjq on 2017/10/23.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "dingjq";

    private Context mContext;
    private static CrashHandler mInstance;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //用来存储文件信息和异常信息
    private Map<String, String> mInfo = new HashMap<>();
    //文件日期格式
    private DateFormat dataFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        if (mInstance == null) {
            synchronized (CrashHandler.class) {
                if (mInstance == null) {
                    mInstance = new CrashHandler();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.d(TAG, "uncaughtException() called with: t = [" + t + "], e = [" + e + "]");
        //收集错误信息
        //保存错误信息
        //上传到服务器
        if (!handleException(e)) {
            Log.d(TAG, "uncaughtException: in if");
            //未人为处理，调用系统默认的处理器处理
            if (mDefaultHandler != null) {
                mDefaultHandler.uncaughtException(t, e);
            }

        } else {
            Log.d(TAG, "uncaughtException: in else");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            Process.killProcess(Process.myPid());
            System.exit(0);

        }

    }

    /**
     * 人为处理异常
     *
     * @param e
     * @return ture:已处理  false:没有处理
     */
    private boolean handleException(Throwable e) {
        Log.d(TAG, "handleException: ");
        if (e == null) {
            return false;
        }
        //Toast提示
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "UncaughtException", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        //收集错误信息
        cloectErrorInfo();
        //保存错误信息
        saveErrorInfo(e);

        return true;
    }

    private void saveErrorInfo(Throwable e) {
        Log.d(TAG, "saveErrorInfo() called with: e = [" + e + "]");
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : mInfo.entrySet()) {
            String keyName = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(keyName + "=" + value + "\n");

        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Throwable cause = e.getCause();
        //while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = e.getCause();
        //}
        printWriter.close();

        String reslut = writer.toString();
        stringBuffer.append(reslut);

        long curTime = System.currentTimeMillis();
        String time = dataFormat.format(new Date());
        String fileName = "crash-" + time + "-" + curTime + ".log";

        Log.d(TAG, "saveErrorInfo: fileName " + fileName);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            String path = "/data/data/com.lenovo.dingjq1.globalexceptioncatcher/crash/";
                    //"/storage/emulated/0/crash/";
            Log.d(TAG, "saveErrorInfo: path = " + path);
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }
  //          File file = new File(path + fileName);
            Log.d(TAG, "saveErrorInfo: dir.exists() = "+dir.exists());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path + fileName);
                fos.write(stringBuffer.toString().getBytes());

            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    private void cloectErrorInfo() {
        Log.d(TAG, "cloectErrorInfo() called");
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = TextUtils.isEmpty(pi.versionName) ? "未设置版本名称" : pi.versionName;
                String versionCode = pi.versionCode + "";
                mInfo.put("versionName", versionName);
                mInfo.put("versionCode", versionCode);
            }

            Field[] fields = Build.class.getFields();
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        mInfo.put(field.getName(), field.get(null).toString());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
