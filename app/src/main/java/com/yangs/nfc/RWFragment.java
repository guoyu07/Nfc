package com.yangs.nfc;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.TagTechnology;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by yangs on 2017/8/22 0022.
 */

public class RWFragment extends LazyLoadFragment implements View.OnClickListener {
    private View mLay;
    private TextView tv;
    private EditText et;
    private Button bt;
    private Tag tag;

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
        if (isInit) {
            if (!isLoad) {
                mLay = getContentView();
                tv = (TextView) mLay.findViewById(R.id.rw_fragment_tv);
                et = (EditText) mLay.findViewById(R.id.rw_fragment_et);
                bt = (Button) mLay.findViewById(R.id.rw_fragment_bt);
                bt.setOnClickListener(this);
            }
            Bundle bundle = getArguments();
            if (bundle != null) {
                Parcelable[] rawMessages = bundle.getParcelableArray("ndef");
                tag = bundle.getParcelable("tag");
                if (rawMessages != null) {
                    NdefMessage[] messages = new NdefMessage[rawMessages.length];
                    for (int i = 0; i < rawMessages.length; i++) {
                        messages[i] = (NdefMessage) rawMessages[i];
                    }
                    AppApplication.showToast("扫描到 " + messages.length + "张卡片", 0);
                    tv.setText(null);
                    for (NdefMessage msg : messages) {
                        interpretMsg(msg);
                    }
                }
                if (rawMessages == null && tag != null) {
                    tv.setText("\n这张卡片支持以下技术:\n");
                    for (String s : tag.getTechList()) {
                        tv.append(s + "\n");
                    }
                }
            }
        }
    }

    private void interpretMsg(NdefMessage ndefMessage) {
        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                String[] payloads = new String(ndefRecord.getPayload()).split(";");
                if (payloads.length != 6) {
                    AppApplication.showToast("数据有误 : size = " + payloads.length, 1);
                    return;
                }
                String xh = payloads[0];      //型号
                String qsh = payloads[1];      //枪身号
                String qjh = payloads[2];      //枪机号
                String gldw = payloads[3];      //管理单位
                String zrr = payloads[4];      //责任人
                String waqk = payloads[5];      //完好情况
                tv.append("型号: " + xh + "\n" +
                        "枪身号: " + qsh + "\n" +
                        "枪机号: " + qjh + "\n" +
                        "管理单位: " + gldw + "\n" +
                        "责任人: " + zrr + "\n" +
                        "完好情况: " + waqk + "\n");
            } else {
                AppApplication.showToast("卡片数据格式不支持读取", 0);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rw_fragment_bt:
                String text = et.getText().toString().trim();
                if (text.equals("")) {
                    AppApplication.showToast("null input", 0);
                    return;
                }
                NdefRecord ndefRecord = createTextRecord(text, Locale.ROOT, true);
                NdefMessage ndefMessage = new NdefMessage(ndefRecord);
                try {
                    Ndef ndefTag = Ndef.get(tag);
                    if (ndefTag != null) {
                        ndefTag.connect();
                        ndefTag.writeNdefMessage(ndefMessage);
                        ndefTag.close();
                        AppApplication.showToast("写入完成", 0);
                    } else {
                        AppApplication.showToast("卡片不支持写入", 1);
                    }
                } catch (IOException e) {
                    AppApplication.showToast("I/O 错误", 1);
                } catch (FormatException e) {
                    AppApplication.showToast("格式化失败", 1);
                }
        }
    }

    //copy from Android API Docs
    public NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
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
