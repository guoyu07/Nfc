package com.yangs.nfc;

import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , DBFragment.OnRefreshDBListener, Toolbar.OnMenuItemClickListener, SearchView.OnQueryTextListener {
    private FragmentManager fm;
    private Toolbar toolbar;
    private LinearLayout tab_ly_1;
    private LinearLayout tab_ly_2;
    private LinearLayout tab_ly_3;
    private TextView tab_tv_1;
    private TextView tab_tv_2;
    private TextView tab_tv_3;
    private ImageView tab_iv_1;
    private ImageView tab_iv_2;
    private ImageView tab_iv_3;
    private RWFragment rwFragment;
    private DBFragment dbFragment;
    private MyFragment myFragment;
    private NfcAdapter mNfcAdapter;
    private PendingIntent pi;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            AppApplication.showToast(this, "设备不支持NFC,功能将无法正常使用!");
        }
        initView();
        switchFragment(1);
    }

    private void initView() {
        fm = getSupportFragmentManager();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("NFC");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);
        tab_ly_1 = (LinearLayout) findViewById(R.id.tab_ly_1);
        tab_ly_2 = (LinearLayout) findViewById(R.id.tab_ly_2);
        tab_ly_3 = (LinearLayout) findViewById(R.id.tab_ly_3);
        tab_tv_1 = (TextView) tab_ly_1.findViewById(R.id.tab_tv_1);
        tab_tv_2 = (TextView) tab_ly_2.findViewById(R.id.tab_tv_2);
        tab_tv_3 = (TextView) tab_ly_3.findViewById(R.id.tab_tv_3);
        tab_iv_1 = (ImageView) tab_ly_1.findViewById(R.id.tab_iv_1);
        tab_iv_2 = (ImageView) tab_ly_2.findViewById(R.id.tab_iv_2);
        tab_iv_3 = (ImageView) tab_ly_3.findViewById(R.id.tab_iv_3);
        tab_ly_1.setOnClickListener(this);
        tab_ly_2.setOnClickListener(this);
        tab_ly_3.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.toolbar_search);
        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);//加载searchview
        searchView.setOnQueryTextListener(this);//为搜索框设置监听事件
        searchView.setQueryHint("按枪身号查找");//设置提示信息
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        pi = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, pi, null, null);
    }

    private void switchFragment(int i) {
        FragmentTransaction transaction = fm.beginTransaction();
        if (rwFragment != null)
            transaction.hide(rwFragment);
        if (dbFragment != null)
            transaction.hide(dbFragment);
        if (myFragment != null)
            transaction.hide(myFragment);
        tab_iv_1.setImageResource(R.drawable.ic_local_offer_24dp);
        tab_tv_1.setTextColor(getResources().getColor(R.color.gray));
        tab_iv_2.setImageResource(R.drawable.ic_data_usage_24dp);
        tab_tv_2.setTextColor(getResources().getColor(R.color.gray));
        tab_iv_3.setImageResource(R.drawable.ic_settings_24dp);
        tab_tv_3.setTextColor(getResources().getColor(R.color.gray));
        switch (i) {
            case 1:
                if (rwFragment == null) {
                    rwFragment = new RWFragment();
                    transaction.add(R.id.frame, rwFragment);
                } else {
                    transaction.show(rwFragment);
                }
                tab_iv_1.setImageResource(R.drawable.ic_local_offer_press_24dp);
                tab_tv_1.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 2:
                if (dbFragment == null) {
                    dbFragment = new DBFragment();
                    transaction.add(R.id.frame, dbFragment);
                } else {
                    transaction.show(dbFragment);
                }
                tab_iv_2.setImageResource(R.drawable.ic_data_usage_press_24dp);
                tab_tv_2.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 3:
                if (myFragment == null) {
                    myFragment = new MyFragment();
                    transaction.add(R.id.frame, myFragment);
                } else {
                    transaction.show(myFragment);
                }
                tab_iv_3.setImageResource(R.drawable.ic_settings_press_24dp);
                tab_tv_3.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tab_ly_1:
                switchFragment(1);
                break;
            case R.id.tab_ly_2:
                switchFragment(2);
                break;
            case R.id.tab_ly_3:
                switchFragment(3);
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null)
            return;
        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)
                || intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Bundle bundle = new Bundle();
            switch (intent.getAction()) {
                case NfcAdapter.ACTION_NDEF_DISCOVERED:
                    bundle.putParcelableArray("ndef", intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES));
                    break;
            }
            AppApplication.showToast(this, "发现新卡片");
            bundle.putParcelable("tag", intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
            FragmentTransaction transaction = fm.beginTransaction();
            if (rwFragment != null)
                transaction.hide(rwFragment);
            if (dbFragment != null)
                transaction.hide(dbFragment);
            if (myFragment != null)
                transaction.hide(myFragment);
            tab_iv_1.setImageResource(R.drawable.ic_local_offer_press_24dp);
            tab_tv_1.setTextColor(getResources().getColor(R.color.colorPrimary));
            tab_iv_2.setImageResource(R.drawable.ic_data_usage_24dp);
            tab_tv_2.setTextColor(getResources().getColor(R.color.gray));
            tab_iv_3.setImageResource(R.drawable.ic_settings_24dp);
            tab_tv_3.setTextColor(getResources().getColor(R.color.gray));
            rwFragment = new RWFragment();
            rwFragment.setArguments(bundle);
            transaction.add(R.id.frame, rwFragment);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void refreshDB() {
        if (rwFragment != null && rwFragment.isLoad)
            rwFragment.searchDB();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_search:
                break;
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        AppApplication.sql = "select * from Info where 枪身号='" + query + "';";
        switchFragment(2);
        if (dbFragment.lRecyclerView != null && dbFragment.isInit)
            dbFragment.lRecyclerView.refresh();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
