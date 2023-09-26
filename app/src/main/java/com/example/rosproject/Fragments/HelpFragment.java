package com.example.rosproject.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.example.rosproject.R;

import java.util.ArrayList;

public class HelpFragment extends Fragment {
    private static View view;

    public HelpFragment(){}

    @Override
    public void onCreate(Bundle instance){
        super.onCreate(instance);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(view == null){
            view = inflater.inflate(R.layout.fragment_help, container, false);

            TabHost mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
            mTabHost.setup();

            ViewPager mViewPager = (ViewPager) view.findViewById(R.id.pager);
            TabsAdapter mTabsAdapter = new TabsAdapter((FragmentActivity) getActivity(), mTabHost, mViewPager);

            mTabsAdapter.addTab(mTabHost.newTabSpec("one").setIndicator("安装"), PageOneFragment.class, null);
            mTabsAdapter.addTab(mTabHost.newTabSpec("two").setIndicator("使用"), PageTwoFragment.class, null);
            mTabsAdapter.addTab(mTabHost.newTabSpec("three").setIndicator("常见问题"), PageThreeFragment.class, null);
        }

        return view;
    }

    public static class TabsAdapter extends FragmentPagerAdapter
            implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener{

        private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<>();

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);

            Log.d("HelpFragment", "Creating Fragment " + position);

            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        static final class TabInfo
        {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args){
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory{
            private final Context mContext;
            public DummyTabFactory(Context context){
                mContext = context;
            }

            public View createTabContent(String tag){
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }


        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager){
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.addOnPageChangeListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));
            tabSpec.getTag();

            TabInfo info = new TabInfo(clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }



        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
        }
    }
}
