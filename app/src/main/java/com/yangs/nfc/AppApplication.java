package com.yangs.nfc;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yangs on 2017/8/22 0022.
 */

public class AppApplication extends Application {

    public static SQLiteDatabase db;
    public static String sql = "select * from Info";
    public static SharedPreferences save;

    @Override
    public void onCreate() {
        super.onCreate();
        save = getSharedPreferences("main", MODE_PRIVATE);
        db = getApplicationContext().openOrCreateDatabase("info.db", Context.MODE_PRIVATE, null);
        db.execSQL("create table if not exists Info(枪身号 TEXT PRIMARY KEY,枪机号 TEXT,型号 TEXT" +
                ",管理单位 TEXT,责任人 TEXT,完好情况 TEXT,更新时间 TEXT);");
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static int writeDB(Context context, String qsh, String qjh, String xh, String gldw,
                              String zrr, String waqk) {
        String t_gldw = "";
        String t_zrr = "";
        String sql = "select * from Info where 枪身号='" + qsh + "';";
        Cursor cursor = null;
        try {
            cursor = AppApplication.db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    t_gldw = cursor.getString(3);
                    t_zrr = cursor.getString(4);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null)
                cursor.close();
        }
        //可以保留管理单位和责任人记录,以 , 分隔
        t_gldw = stringMerge(gldw, t_gldw);
        t_zrr = stringMerge(zrr, t_zrr);
        try {
            ContentValues cv = new ContentValues();
            cv.put("枪身号", qsh);
            cv.put("枪机号", qjh);
            cv.put("型号", xh);
            cv.put("管理单位", t_gldw);
            cv.put("责任人", t_zrr);
            cv.put("完好情况", waqk);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            cv.put("更新时间", df.format(new Date()));
            AppApplication.db.replace("Info", null, cv);
            AppApplication.showToast(context, "Success");
            return 0;
        } catch (Exception e) {
            AppApplication.showToast(context, "失败: " + e.toString());
        }
        return -1;
    }

    //a:edit text , b: sql text
    public static String stringMerge(String a, String b) {
        String c = a;
        for (String d : b.split(",")) {
            if (!c.contains(d))
                c = c + "," + d;
        }
        return c;
    }

    public static int writeTag(Context context, Tag tag, String msg) {
        Ndef ndefTag = Ndef.get(tag);
        if (ndefTag != null) {
            NdefRecord ndefRecord = createTextRecord(msg, Locale.ROOT, true);
            NdefMessage ndefMessage = new NdefMessage(ndefRecord);
            try {
                ndefTag.connect();
                ndefTag.writeNdefMessage(ndefMessage);
                ndefTag.close();
                AppApplication.showToast(context, "操作成功,请重新放置卡片");
                return 0;
            } catch (IOException e) {
                AppApplication.showToast(context, "I/O 错误,请重新放置卡片");
            } catch (FormatException e) {
                AppApplication.showToast(context, e.toString());
            }
        } else {
            AppApplication.showToast(context, "卡片不支持写操作");
        }
        return -1;
    }

    //copy from Android API Docs
    public static NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }
}
