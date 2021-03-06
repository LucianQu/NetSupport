package com.hbmcc.wangsen.netsupport.ui.fragment.forth.basestationdatabase;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.hbmcc.wangsen.netsupport.App;
import com.hbmcc.wangsen.netsupport.R;
import com.hbmcc.wangsen.netsupport.base.BaseBackFragment;
import com.hbmcc.wangsen.netsupport.database.LteBasesCustom;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomBasestationDatabaseFragment extends BaseBackFragment {
    private static final String TAG = "BasestationDatabaseFrag";
    ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();

    private static final String ARG_TITLE = "arg_title";
    private String mTitle;

    private TabLayout mTab;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private Button btnFragmentBasestionDatabaseImportDataCustom;
    ProgressDialog progressDialog;
    AlertDialog.Builder alertDialog;
    private long startTime; //起始时间
    private long endTime;//结束时间

    public static CustomBasestationDatabaseFragment newInstance(String title) {

        CustomBasestationDatabaseFragment fragment = new CustomBasestationDatabaseFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTitle = bundle.getString(ARG_TITLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom_basestation, container,
                false);
        initView(view);
        return attachToSwipeBack(view);
    }

    private void initView(View view) {
        mViewPager = view.findViewById(R.id.viewpager_fragment_basestastion_custom);
        btnFragmentBasestionDatabaseImportDataCustom = view.findViewById(R.id.btn_fragment_basestion_database_import_custom);

    }

    /**
     * 这里演示:
     * 比较复杂的Fragment页面会在第一次start时,导致动画卡顿
     * Fragmentation提供了onEnterAnimationEnd()方法,该方法会在 入栈动画 结束时回调
     * 所以在onCreateView进行一些简单的View初始化(比如 toolbar设置标题,返回按钮; 显示加载数据的进度条等),
     * 然后在onEnterAnimationEnd()方法里进行 复杂的耗时的初始化 (比如FragmentPagerAdapter的初始化 加载数据等)
     */
    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        initDelayView();
    }

    private void initDelayView() {
        btnFragmentBasestionDatabaseImportDataCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog = new AlertDialog.Builder(_mActivity);
                alertDialog.setTitle("提示")
                        .setMessage("该操作将清空原有规划自定义数据，是否继续")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog = ProgressDialog.show(_mActivity, "提示", "规划自定义数据导入中，请稍等...",
                                        true, false);
                                importLteDatabase();
                            }
                        });
                alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        });
    }
    //导入工参
    public boolean importLteDatabase() {
        startTime = System.currentTimeMillis();
        if (com.hbmcc.wangsen.netsupport.util.FileUtils.isFileExist(com.hbmcc.wangsen.netsupport.util.FileUtils.getLteInputFilecustom())) {
            newCachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    File lteDatabaseFile = new File(com.hbmcc.wangsen.netsupport.util.FileUtils.getLteInputFilecustom());//获得文件对象规划自定义(模板).csv
                    LteBasesCustom lteBasesCustom;//获取工参实体类的实例
                    List<LteBasesCustom> lteBasesCustomList = new ArrayList<>();//创建实体类集合
                    String inString;
                    int i = 0;
                    try {
                        LitePal.deleteAll(LteBasesCustom.class);//删除LteBasestationCell数据表
                        BufferedReader reader =
                                new BufferedReader(new InputStreamReader(new FileInputStream(lteDatabaseFile), "GBK"));//获得输入流
                        while ((inString = reader.readLine()) != null) {//一行一行读，判断是否为空
                            String[] inStringSplit = inString.split(",");
                            if (inStringSplit.length != 5) {
                                _mActivity.runOnUiThread(new Runnable() {//开启子线程进行提示
                                    @Override
                                    public void run() {
                                        Toast.makeText(App.getContext(), "导入的工参数据格式不对", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                            i++;

                            if (i > 2) {
                                lteBasesCustom = new LteBasesCustom();
                                lteBasesCustom.setName(inStringSplit[0]);
                                lteBasesCustom.setCity(inStringSplit[1]);
                                lteBasesCustom.setLng(Float.parseFloat(inStringSplit[2]));
                                lteBasesCustom.setLat(Float.parseFloat
                                        (inStringSplit[3]));
                                lteBasesCustom.setRemark(inStringSplit[4]);
                                lteBasesCustomList.add(lteBasesCustom);
                            }
                        }
                        if (i == 2) {
                            _mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();//如果文件中没有数据，则取消进度框提示
                                    Toast.makeText(App.getContext(), "文件无数据", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        LitePal.saveAll(lteBasesCustomList);
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        final int cellNums = i;
                        _mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(App.getContext(), "第" + cellNums + "行数据异常，请处理", Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    } finally {
                        endTime = System.currentTimeMillis();
                        final long usedTime = (int) ((endTime - startTime) / 1000);
                        final int cellNums = i;
                        _mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();//导入完成后，取消进度对话框显示
                                Toast.makeText(App.getContext(), "共导入"+ cellNums + "行数据，用时" + String.format("%d " + "s", usedTime), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        } else {
            progressDialog.dismiss();//如果找不到文件，则取消进度框提示
            Toast.makeText(getContext(), "规划自定义文件不存在", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}