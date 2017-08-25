package com.yangs.nfc;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.github.jdsjlzx.recyclerview.LRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yangs on 2017/8/22 0022.
 */

public class MyFragment extends LazyLoadFragment implements View.OnClickListener {
    @BindView(R.id.my_ll_info)
    LinearLayout ll_info;
    @BindView(R.id.my_ll_add)
    LinearLayout ll_add;
    @BindView(R.id.my_ll_reset)
    LinearLayout ll_reset;
    @BindView(R.id.my_ll_about)
    LinearLayout ll_about;
    private View mLay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int setContentView() {
        return R.layout.my_fragment_layout;
    }

    @Override
    protected void lazyLoad() {
        if (isInit) {
            if (!isLoad) {
                mLay = getContentView();
                ButterKnife.bind(this, mLay);
                ll_info.setOnClickListener(this);
                ll_add.setOnClickListener(this);
                ll_reset.setOnClickListener(this);
                ll_about.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_ll_info:
                String info = AppApplication.save.getString("info", "");
                View info_dia = getActivity().getLayoutInflater().inflate(R.layout.my_dialog_info, null);
                final EditText et = (EditText) info_dia.findViewById(R.id.my_dialog_info_et);
                et.setText(info);
                final AlertDialog info_dialog = new AlertDialog.Builder(getContext()).setView(info_dia).setTitle("修改")
                        .setCancelable(true).setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String s = et.getText().toString().trim();
                                if (!s.equals("")) {
                                    AppApplication.save.edit().putString("info", s).apply();
                                    AppApplication.showToast(getContext(), "修改成功");
                                    dialog.dismiss();
                                } else
                                    AppApplication.showToast(getContext(), "输入为空!");
                            }
                        }).create();
                if (info.equals("")) {
                    info_dialog.show();
                } else {
                    new AlertDialog.Builder(getContext()).setTitle("个人资料").setCancelable(true)
                            .setMessage(info)
                            .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    info_dialog.show();
                                }
                            }).create().show();
                }
                break;
            case R.id.my_ll_add:
                View view = getActivity().getLayoutInflater().inflate(R.layout.rw_dialog_edit, null);
                final EditText et_1 = (EditText) view.findViewById(R.id.rw_dialog_edit_et1);
                final EditText et_2 = (EditText) view.findViewById(R.id.rw_dialog_edit_et2);
                final EditText et_3 = (EditText) view.findViewById(R.id.rw_dialog_edit_et3);
                final EditText et_4 = (EditText) view.findViewById(R.id.rw_dialog_edit_et4);
                final EditText et_5 = (EditText) view.findViewById(R.id.rw_dialog_edit_et5);
                final EditText et_6 = (EditText) view.findViewById(R.id.rw_dialog_edit_et6);
                new AlertDialog.Builder(getContext()).setTitle("添加数据").setCancelable(false)
                        .setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (AppApplication.writeDB(getContext(),
                                et_2.getText().toString().trim(),
                                et_3.getText().toString().trim(),
                                et_1.getText().toString().trim(),
                                et_4.getText().toString().trim(),
                                et_5.getText().toString().trim(),
                                et_6.getText().toString().trim()) == 0) {
                            AppApplication.showToast(getContext(), "添加成功");
                            dialog.dismiss();
                        } else {
                            AppApplication.showToast(getContext(), "添加数据失败");
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
            case R.id.my_ll_reset:
                new AlertDialog.Builder(getContext()).setTitle("警告").setCancelable(true)
                        .setMessage("此操作会清空数据库,数据将丢失,是否继续?")
                        .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    AppApplication.db.execSQL(
                                            "delete from Info");
                                    AppApplication.showToast(getContext(), "清空完成!");
                                } catch (Exception e) {
                                }
                            }
                        }).create().show();
                break;
            case R.id.my_ll_about:
                new AlertDialog.Builder(getContext()).setTitle("关于").setCancelable(true)
                        .setMessage("NFC读写器1.0\n安全保存枪支信息。")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                break;
        }
    }
}
