package com.yangs.nfc;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;

/**
 * Created by yangs on 2017/8/22 0022.
 */

public class RWFragment extends LazyLoadFragment implements View.OnClickListener {
    private View mLay;
    @Nullable
    @BindView(R.id.rw_frag_tv_cardinfo)
    TextView tv_cardinfo;
    @Nullable
    @BindView(R.id.rw_frag_bt_edit)
    Button bt_edit;
    @Nullable
    @BindView(R.id.rw_frag_bt_reset)
    Button bt_reset;
    @Nullable
    @BindView(R.id.rw_frag_tv_t)
    TextView tv_t;
    @Nullable
    @BindView(R.id.rw_frag_tv_dbinfo)
    TextView tv_dbinfo;
    @Nullable
    @BindView(R.id.rw_frag_bt_apply)
    Button bt_apply;
    @Nullable
    @BindView(R.id.rw_frag_bt_apply2)
    Button bt_apply2;
    @Nullable
    @BindView(R.id.rw_dialog_edit_et1)
    EditText edit_dialog_et_1;
    @Nullable
    @BindView(R.id.rw_dialog_edit_et2)
    EditText edit_dialog_et_2;
    @Nullable
    @BindView(R.id.rw_dialog_edit_et3)
    EditText edit_dialog_et_3;
    @Nullable
    @BindView(R.id.rw_dialog_edit_et4)
    EditText edit_dialog_et_4;
    @Nullable
    @BindView(R.id.rw_dialog_edit_et5)
    EditText edit_dialog_et_5;
    @Nullable
    @BindView(R.id.rw_dialog_edit_et6)
    EditText edit_dialog_et_6;
    private Tag tag;
    private String xh;       //型号
    private String qsh;      //枪身号
    private String qjh;      //枪机号
    private String gldw;     //管理单位
    private String zrr;      //责任人
    private String waqk;     //完好情况
    private String time;    //更新时间

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int setContentView() {
        return R.layout.rw_fragment_layout;
    }

    @Override
    protected void lazyLoad() {
        mLay = getContentView();
        ButterKnife.bind(this, mLay);
        bt_edit.setOnClickListener(this);
        bt_reset.setOnClickListener(this);
        bt_apply.setOnClickListener(this);
        bt_apply2.setOnClickListener(this);
        tv_cardinfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv_dbinfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        Bundle bundle = getArguments();
        if (bundle != null) {
            Parcelable[] rawMessages = bundle.getParcelableArray("ndef");
            tag = bundle.getParcelable("tag");
            if (rawMessages != null) {
                if (rawMessages.length > 1) {
                    AppApplication.showToast(getContext(), "检测到多个卡片,默认使用第一张");
                }
                interpretMsg((NdefMessage) rawMessages[0]);
            }
            if (rawMessages == null && tag != null) {
                tv_cardinfo.setText("\n这张卡片支持以下技术:\n");
                for (String s : tag.getTechList()) {
                    tv_cardinfo.append(s + "\n");
                }
            }
        }
    }

    private void interpretMsg(NdefMessage ndefMessage) {
        tv_cardinfo.setText(null);
        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                byte[] payload = ndefRecord.getPayload();
                byte status = payload[0];
                int enc = status & 0x80; // Bit mask 7th bit 1
                String encString = null;
                if (enc == 0)
                    encString = "UTF-8";
                else
                    encString = "UTF-16";
                int ianaLength = status & 0x3F; // Bit mask bit 5..0
                String content;
                try {
                    content = new String(payload, ianaLength + 1,
                            payload.length - 1 - ianaLength, encString);
                } catch (Exception e) {
                    AppApplication.showToast(getContext(), e.toString());
                    return;
                }
                String[] payloads = content.split(";");
                if (payloads.length != 6) {
                    tv_cardinfo.append("空的卡片 \n" + new String(ndefRecord.getPayload()));
                    return;
                }
                this.xh = payloads[0];
                this.qsh = payloads[1];
                this.qjh = payloads[2];
                this.gldw = payloads[3];
                this.zrr = payloads[4];
                this.waqk = payloads[5];
                tv_cardinfo.append("型号: " + xh + "\n" +
                        "枪身号: " + qsh + "\n" +
                        "枪机号: " + qjh + "\n" +
                        "管理单位: " + gldw + "\n" +
                        "责任人: " + zrr + "\n" +
                        "完好情况: " + waqk);
                searchDB();
            } else {
                AppApplication.showToast(getContext(), "卡片数据格式不支持读取");
            }
        }
    }

    public String searchDB() {
        if (tv_t == null)
            tv_t = (TextView) mLay.findViewById(R.id.rw_frag_tv_t);
        tv_t.setText("数据库中的信息");
        String msg = "";
        String sql = "select * from Info where 枪身号='" + this.qsh + "';";
        Cursor cursor = null;
        try {
            cursor = AppApplication.db.rawQuery(sql, null);
            cursor.moveToFirst();
            tv_dbinfo.setText("select * from Info where 枪身号=" + qsh + ";\n");
            do {
                String mxh = cursor.getString(2);
                String mqsh = cursor.getString(0);
                String mqjh = cursor.getString(1);
                String mgldw = cursor.getString(3).split(",")[0];
                String mzrr = cursor.getString(4).split(",")[0];
                String mwaqk = cursor.getString(5);
                String mtime = cursor.getString(6);
                if (mxh.equals(this.xh) && mqsh.equals(this.qsh) && mqjh.equals(this.qjh) &&
                        mgldw.equals(this.gldw) && mzrr.equals(this.zrr) && mwaqk.equals(this.waqk)) {
                    tv_t.setText("数据库中的信息  (一致)");
                    tv_t.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    tv_t.setText("数据库中的信息  (不一致)");
                    tv_t.setTextColor(Color.RED);
                }
                tv_dbinfo.append("型号: " + mxh + "\n" +
                        "枪身号: " + mqsh + "\n" +
                        "枪机号: " + mqjh + "\n" +
                        "管理单位: " + mgldw + "\n" +
                        "责任人: " + mzrr + "\n" +
                        "完好情况: " + mwaqk + "\n" +
                        "更新时间: " + mtime);
                msg = mxh + ";" + mqsh + ";" + mqjh + ";" + mgldw + ";" + mzrr + ";" + mwaqk;
            } while (cursor.moveToNext());
            bt_apply.setText("同步到数据库");
        } catch (Exception e) {
            if (tv_dbinfo == null)
                tv_dbinfo = (TextView) mLay.findViewById(R.id.rw_frag_tv_dbinfo);
            if (bt_apply == null)
                bt_apply = (Button) mLay.findViewById(R.id.rw_frag_bt_apply);
            tv_dbinfo.setText("数据库中没有信息");
            bt_apply.setText("添加到数据库");
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return msg;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rw_frag_bt_edit:
                if (tag == null) {
                    AppApplication.showToast(getContext(), "没有检测到卡片");
                    return;
                }
                View view = getActivity().getLayoutInflater().inflate(R.layout.rw_dialog_edit, null);
                ButterKnife.bind(this, view);
                edit_dialog_et_1.setText(this.xh);
                edit_dialog_et_2.setText(this.qsh);
                edit_dialog_et_3.setText(this.qjh);
                edit_dialog_et_4.setText(this.gldw);
                edit_dialog_et_5.setText(this.zrr);
                edit_dialog_et_6.setText(this.waqk);
                new AlertDialog.Builder(getContext()).setTitle("修改标签数据").setCancelable(false)
                        .setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = edit_dialog_et_1.getText().toString().trim() + ";" +
                                edit_dialog_et_2.getText().toString().trim() + ";" +
                                edit_dialog_et_3.getText().toString().trim() + ";" +
                                edit_dialog_et_4.getText().toString().trim() + ";" +
                                edit_dialog_et_5.getText().toString().trim() + ";" +
                                edit_dialog_et_6.getText().toString().trim();
                        if (AppApplication.writeTag(getActivity(), tag, text) == 0) {
                            dialog.dismiss();
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
            case R.id.rw_frag_bt_reset:
                if (tag != null) {
                    new AlertDialog.Builder(getContext()).setTitle("提示").setCancelable(true)
                            .setMessage("将会擦除卡片内的数据,是否继续?")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AppApplication.writeTag(getContext(), tag, "");
                                    dialog.dismiss();
                                }
                            }).create().show();
                } else
                    AppApplication.showToast(getContext(), "没有检测到卡片");
                break;
            case R.id.rw_frag_bt_apply:
                if (tag == null || this.qsh == null) {
                    AppApplication.showToast(getContext(), "没有检测到卡片");
                    return;
                }
                if (AppApplication.writeDB(getActivity(), this.qsh, this.qjh, this.xh, this.gldw,
                        this.zrr, this.waqk) == 0) {
                    searchDB();
                } else {
                    AppApplication.showToast(getContext(), "更新数据库失败");
                }
                break;
            case R.id.rw_frag_bt_apply2:
                if (tag == null) {
                    AppApplication.showToast(getContext(), "没有检测到卡片");
                    return;
                }
                final String msg = searchDB();
                if (msg.equals("")) {
                    AppApplication.showToast(getContext(), "数据库中没有记录");
                    return;
                }
                new AlertDialog.Builder(getContext()).setTitle("提示").setCancelable(true)
                        .setMessage("数据库中的数据会同步到卡片内,卡片里的数据将被重写,是否继续?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AppApplication.writeTag(getContext(), tag, msg);
                                dialog.dismiss();
                            }
                        }).create().show();
                break;
        }
    }

}
