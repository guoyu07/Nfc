package com.yangs.nfc;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.EditText;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnItemLongClickListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yangs on 2017/8/22 0022.
 */

public class DBFragment extends LazyLoadFragment implements OnItemClickListener, OnRefreshListener {
    @Nullable
    @BindView(R.id.db_frag_lr)
    LRecyclerView lRecyclerView;
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
    private LRecyclerViewAdapter lRecyclerViewAdapter;
    private DataAdapter dataAdapter;
    private List<DB> list;
    private View mLay;
    private OnRefreshDBListener onRefreshDBListener;

    @Override

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int setContentView() {
        return R.layout.db_fragment_layout;
    }

    @Override
    protected void lazyLoad() {
        if (isInit) {
            if (!isLoad) {
                mLay = getContentView();
                onRefreshDBListener = (OnRefreshDBListener) getActivity();
                ButterKnife.bind(this, mLay);
                lRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                list = new ArrayList<>();
                dataAdapter = new DataAdapter(list);
                lRecyclerViewAdapter = new LRecyclerViewAdapter(dataAdapter);
                lRecyclerViewAdapter.setOnItemClickListener(this);
                lRecyclerView.setHeaderViewColor(R.color.colorAccent, R.color.gray, android.R.color.white);
                lRecyclerView.setAdapter(lRecyclerViewAdapter);
                lRecyclerView.setHasFixedSize(true);
                lRecyclerView.setOnRefreshListener(this);
                lRecyclerView.refresh();
            }
        }
    }

    @Override
    public void onItemClick(View view, final int position) {
        final DB db = list.get(position);
        String his_gldw = "";
        String[] a = db.getGldw().split(",");
        for (int i = 1; i < a.length; i++) {
            if (his_gldw.equals(""))
                his_gldw = a[i];
            else
                his_gldw = his_gldw + "," + a[i];
        }
        String his_zrr = "";
        String[] b = db.getZrr().split(",");
        for (int i = 1; i < b.length; i++) {
            if (his_zrr.equals(""))
                his_zrr = b[i];
            else
                his_zrr = his_zrr + "," + b[i];
        }
        new AlertDialog.Builder(getContext()).setTitle("详细").setCancelable(true)
                .setMessage("型号: " + db.getXh() + "\n" +
                        "枪身号: " + db.getQsh() + "\n" +
                        "枪机号: " + db.getQjh() + "\n" +
                        "管理单位: " + db.getGldw().split(",")[0] + "\n" +
                        "历史管理单位: " + his_gldw + "\n" +
                        "责任人: " + db.getZrr().split(",")[0] + "\n" +
                        "历史责任人: " + his_zrr + "\n" +
                        "完好情况: " + db.getWaqk() + "\n" +
                        "更新时间: " + db.getTime()).setPositiveButton("修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                View view = getActivity().getLayoutInflater().inflate(R.layout.rw_dialog_edit, null);
                ButterKnife.bind(DBFragment.this, view);
                edit_dialog_et_1.setText(db.getXh());
                edit_dialog_et_2.setText(db.getQsh());
                edit_dialog_et_3.setText(db.getQjh());
                edit_dialog_et_4.setText(db.getGldw());
                edit_dialog_et_5.setText(db.getZrr());
                edit_dialog_et_6.setText(db.getWaqk());
                new AlertDialog.Builder(getContext()).setTitle("修改标签数据").setCancelable(false)
                        .setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (AppApplication.writeDB(getContext(),
                                edit_dialog_et_2.getText().toString().trim(),
                                edit_dialog_et_3.getText().toString().trim(),
                                edit_dialog_et_1.getText().toString().trim(),
                                edit_dialog_et_4.getText().toString().trim(),
                                edit_dialog_et_5.getText().toString().trim(),
                                edit_dialog_et_6.getText().toString().trim()) == 0) {
                            if (lRecyclerView == null)
                                lRecyclerView = (LRecyclerView) mLay.findViewById(R.id.db_frag_lr);
                            lRecyclerView.refresh();
                            onRefreshDBListener.refreshDB();
                            dialog.dismiss();
                        } else {
                            AppApplication.showToast(getContext(), "更新数据库失败");
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        }).setNegativeButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                new AlertDialog.Builder(getContext()).setTitle("枪身号: " + list.get(position)
                        .getQsh()).setCancelable(false).setMessage("您确定要删除吗?")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AppApplication.db.delete("Info", "枪身号=?", new String[]{list.get(position)
                                        .getQsh()});
                                dialog.dismiss();
                                onRefreshDBListener.refreshDB();
                                AppApplication.showToast(getContext(), "删除完成");
                                dataAdapter.getList().remove(position);
                                lRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

            }
        }).create().show();

    }

    @Override
    public void onRefresh() {
        if (lRecyclerView == null)
            lRecyclerView = (LRecyclerView) mLay.findViewById(R.id.db_frag_lr);
        Cursor cursor = null;
        try {
            dataAdapter.getList().clear();
            cursor = AppApplication.db.rawQuery(AppApplication.sql, null);
            if (cursor.moveToFirst()) {
                do {
                    String mxh = cursor.getString(2);
                    String mqsh = cursor.getString(0);
                    String mqjh = cursor.getString(1);
                    String mgldw = cursor.getString(3);
                    String mzrr = cursor.getString(4);
                    String mwaqk = cursor.getString(5);
                    String mtime = cursor.getString(6);
                    DB db = new DB();
                    db.setXh(mxh);
                    db.setQsh(mqsh);
                    db.setQjh(mqjh);
                    db.setGldw(mgldw);
                    db.setZrr(mzrr);
                    db.setWaqk(mwaqk);
                    db.setTime(mtime);
                    list.add(db);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            AppApplication.showToast(getContext(), e.toString());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        lRecyclerView.refreshComplete(10);
        lRecyclerViewAdapter.notifyDataSetChanged();
        if (list.size() == 0)
            AppApplication.showToast(getContext(), "数据库中没有记录");
        else
            AppApplication.showToast(getContext(), "数据库中有" + list.size() + "条记录");
        AppApplication.sql = "select * from Info";
    }

    public interface OnRefreshDBListener {
        public void refreshDB();
    }
}
