package de.taytec.biodroid;


import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BioDroidActivity
        extends FragmentActivity
        implements BioView.BioCalendarProvider
{

    private Button tv_birth;
    private Button tv_today;
    private BioView bioview;
    private FavoritesAdapter favoritesAdapter;

    private final Calendar calBirth = Calendar.getInstance();
    private final Calendar calToday = Calendar.getInstance();

	private ViewPager mViewPager;
    private AlertDialog mHistFragment;

    private TabAdapter mTabAdapter;
    public boolean debug;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        if (getPackageName().endsWith(".debug")) {
            BioLog.debug = true;
        }

        BioLog.d(getClass().getSimpleName(), "onCreate() package=" + getPackageName());

        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

		favoritesAdapter = new FavoritesAdapter(this,
			new SimpleDateFormat(getResources().getString(R.string.format_date)));

        tv_birth = (Button) findViewById(R.id.editBirthday);
        tv_today = (Button) findViewById(R.id.editToday);

        initActionBar();
		bioview = (BioView)findViewById(R.id.surface);
		bioview.setCalendarProvider(this);
        bioview.setTextSize(tv_birth.getTextSize());

        if (null != savedInstanceState) {
            onRestoreInstanceState(savedInstanceState);
        }
        else {
            restoreActivityPreferences();
        }

        BioLog.d(getClass().getSimpleName(), "onCreate() done");
    }

    @Override
    protected void onResume() {
        BioLog.d(getClass().getSimpleName(), "onResume()");
        super.onResume();
        updateDisplay();
        BioLog.d(getClass().getSimpleName(), "onResume() done");
    }

    /**
     * initialize ActionBar Tabs
     */
    private void initActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.descPager);
        mViewPager.setAdapter(getTabAdapter());
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        for (int i = 0; i < mTabAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(mTabAdapter.getPageTitle(i))
                    .setTabListener(new ActionBar.TabListener() {
                        @Override
                        public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
                            BioLog.d(getPackageName(), "(BioDroidActivity:TabListener) : onTabSelected() " + tab.getPosition());
                            mViewPager.setCurrentItem(tab.getPosition(), true);
                        }

                        @Override
                        public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {

                        }

                        @Override
                        public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {

                        }
                    }));
        }
    }

    /**
     * persistent save the activity state
     */
    protected void storeActivityPreferences()
	{
        BioLog.d(getClass().getSimpleName(), "storeActivityPreferences()");
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		Editor ed = preferences.edit();
		ed.putLong("birthday", calBirth.getTime().getTime());
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			ed.putInt("version", pInfo.versionCode);
		}
		catch (NameNotFoundException e)
		{
			BioLog.e(getPackageName(), "storeActivityPreferences() : PackageManager.GET_META_DATA", e);
		}
		favoritesAdapter.storeToPreferences(ed, "history");
		ed.apply();
    }

    /**
     * restore activity state from preferences
     */
    protected void restoreActivityPreferences()
	{
        BioLog.d(getClass().getSimpleName(), "restoreActivityPreferences()");
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            int oldVersion = preferences.getInt("version", 0);
            if (oldVersion < 12)
            {
                showIntro();
            }
		}
		catch (NameNotFoundException e)
		{
			BioLog.e(getPackageName(), "restoreActivityPreferences() : PackageManager.GET_META_DATA", e);
		}	
		favoritesAdapter.restoreFromPreferences(preferences, "history");
		//
		// restore birthday
		//
		long bdTime = preferences.getLong("birthday", -1);
		if (-1 != bdTime)
		{
			calBirth.setTime(new Date(bdTime));
		}
		else
		{
			calBirth.set(2003, 2, 6);
		}
    }

    private void showIntro() {
        ShowCase showCase = new ShowCase(this);

        try {
            ActionViewTarget target = new ActionViewTarget(this, ActionViewTarget.Type.TITLE);
            target.getPoint();
            showCase.addCase(target, R.string.showCaseAboutTitle, R.string.showCaseAboutText);
        }
        catch (NullPointerException e)
        {
            Log.e(getClass().getSimpleName(), "show case title", e);
        }

        showCase.addCase(new ViewTarget(tv_birth), R.string.showCaseBirthTitle, R.string.showCaseBirthText);
        showCase.addCase(new ViewTarget(tv_today), R.string.showCaseDateTitle, R.string.showCaseDateText);
        showCase.addCase(new ViewTarget(findViewById(R.id.dateReset)), R.string.showCaseResetTitle, R.string.showCaseResetText);

        try {
            ActionItemTarget target = new ActionItemTarget(this, R.id.menuTheory);
            target.getPoint();
            showCase.addCase(target, R.string.showCaseOverflowTitle, R.string.showCaseOverflowText);
        }
        catch (NullPointerException e) {
            Log.e(getClass().getSimpleName(), "show case theory", e);
        }

        showCase.addCase(new ViewTarget(bioview), R.string.showCaseGraphTitle, R.string.showCaseGraphText);
        showCase.addCase(new ViewTarget(mViewPager), R.string.showCaseDetailTitle, R.string.showCaseDetailText);
        showCase.showDemo();
    }

     @Override
    protected void onStop()
	{
		BioLog.d(getClass().getSimpleName(), "onStop()");
		super.onStop();
		//storeActivityPreferences();
        BioLog.d(getClass().getSimpleName(), "onStop() done");
    }


    @Override
    protected void onRestoreInstanceState(Bundle state)
	{
		BioLog.d(getClass().getSimpleName(), "onRestoreInstanceState()");
		super.onRestoreInstanceState(state);
		if (state.containsKey("today"))
		{
			calToday.setTimeInMillis(state.getLong("today"));
		}
        restoreActivityPreferences();
        BioLog.d(getClass().getSimpleName(), "onRestoreInstanceState() done");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
	{
		BioLog.d(getClass().getSimpleName(), "onSaveInstanceState()");
		super.onSaveInstanceState(outState);
		storeActivityPreferences();
		outState.putLong("today", calToday.getTimeInMillis());
        BioLog.d(getClass().getSimpleName(), "onSaveInstanceState() done");
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.actionbar_buttons, menu);
        return super.onCreateOptionsMenu(menu);
	}


    /**
     * 
     * @return
     */
    protected AlertDialog createAboutDialog()
	{	
		String versionName = "";
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			versionName = pInfo.versionName;
		}
		catch (NameNotFoundException e)
		{
			BioLog.e(getPackageName(), "createAboutDialog() : PackageManager.GET_META_DATA", e);
		}	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setMessage(android.text.Html.fromHtml(getResources().getString(R.string.About).replace("VERSION", versionName)))
			.setCancelable(false)
			.setPositiveButton(R.string.button_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
		AlertDialog alert = builder.create();
		return alert;
    }

    /**
     * 
     */
    protected void updateDisplay()
	{
		bioview.invalidate();

		SimpleDateFormat df = new SimpleDateFormat(
			getResources().getString(R.string.format_date));
		tv_today.setText(df.format(calToday.getTime()));
        tv_birth.setText(df.format(calBirth.getTime()));
        favoritesAdapter.add(calBirth.getTime());

        TabAdapter tabAdapter = (TabAdapter) mViewPager.getAdapter();
        tabAdapter.updateDescription();

    }

    /**
     * check date values of Calendar to ensure that all fields are correct
     * 
     * @param cal
     * @return
     */
    protected Calendar checkCalendar(Calendar cal)
	{
		if (cal.get(Calendar.YEAR) < cal.getActualMinimum(Calendar.YEAR))
		{
			cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		}
		else if (cal.get(Calendar.YEAR) > cal.getActualMaximum(Calendar.YEAR))
		{
			cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		}

		if (cal.get(Calendar.MONTH) < cal.getActualMinimum(Calendar.MONTH))
		{
			cal.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
		}
		else if (cal.get(Calendar.MONTH) > cal.getActualMaximum(Calendar.MONTH))
		{
			cal.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
		}

		if (cal.get(Calendar.DAY_OF_MONTH) < cal.getActualMinimum(Calendar.DAY_OF_MONTH))
		{
			cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		}
		else if (cal.get(Calendar.DAY_OF_MONTH) > cal.getActualMaximum(Calendar.DAY_OF_MONTH))
		{
			cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		}

		return cal;
    }

    /**
     * Invoked when the user selects an item from the Menu.
     * 
     * @param item
     *            the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menuAbout:
                showIntro();
				return true;
			case R.id.menuTheory:
                TheoryFragment.newInstance().show(getSupportFragmentManager(), TheoryFragment.TAG);
				return true;
		}
		return false;
    }

    private void showHistoryDialog() {
        if (mHistFragment == null) {
            View view = getLayoutInflater().inflate(R.layout.favorites_fragment, null);
            ListView favList = (ListView) view.findViewById(R.id.fav_list);
            favoritesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            favList.setAdapter(favoritesAdapter);
            favList.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    BioLog.d(getPackageName(), "ListView.OnItemClickListener.onItemClick: " + position);
                    FavoritesAdapter.BioDate selectedItem = (FavoritesAdapter.BioDate) adapterView.getItemAtPosition(position);
                    calBirth.setTime(selectedItem);
                    updateDisplay();
                    mHistFragment.cancel();
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle(R.string.label_btn_history)
                    .setCancelable(true)
                    .setView(view)
                    .setPositiveButton(R.string.button_new, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mHistFragment.cancel();
                            showBirthdayPickerDialog(null);
                        }
                    })
                    .setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mHistFragment.cancel();
                        }
                    });
            mHistFragment = builder.create();
        }
        mHistFragment.show();
    }

    /**
     *
     * @param v
     */
    @SuppressWarnings("UnusedParameters")
    public void showHistoryDialog(View v) {
        showHistoryDialog();
    }

    /**
     * reset date to compute for (endCalendar) to today
     *
     * @param v
     */
    @SuppressWarnings("UnusedParameters")
    public void resetDateToToday(View v) {
        calToday.setTime(Calendar.getInstance().getTime());
        updateDisplay();
    }

    /**
     * show the dialog to set the date to compute for
     *
     * @param v
     */
    @SuppressWarnings("UnusedParameters")
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), DatePickerFragment.TagEndDate);
    }

    /**
     * show a date picker dialog to set a new birthday ( start calendar )
     *
     * @param v
     */
    @SuppressWarnings("UnusedParameters")
    public void showBirthdayPickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), DatePickerFragment.TagStartDate);
    }

    /**
     * set birthday from date picker
     *
     * - update favoritesAdapter
     * - redraw ui
     *
     * @param year
     * @param month
     * @param day
     */
    public void setStartCalendar(int year, int month, int day) {
        calBirth.set(year, month, day);
        favoritesAdapter.add(calBirth.getTime());
        storeActivityPreferences();
        updateDisplay();
    }

    /**
     * set date to compute for
     *
     * @param year
     * @param month
     * @param day
     */
    private void setEndCalendar(int year, int month, int day) {
        calToday.set(year, month, day);
        updateDisplay();
    }


    /**
     * provide date picker fragment for birthday and second date
     */
	public static class DatePickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener
	{
        static final public String TagStartDate = "birthdayPicker";
        static final public String TagEndDate = "datePicker";

        @SuppressWarnings("NullableProblems")
        @Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
            if (getActivity() instanceof BioDroidActivity) {
                Calendar cal = getTag().equals(TagStartDate)
                        ? ((BioDroidActivity) getActivity()).getStartCalendar()
                        : ((BioDroidActivity) getActivity()).getEndCalendar();
                DatePickerDialog dpd = new DatePickerDialog(getActivity(), this,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH));
                dpd.setTitle(getTag().equals(TagStartDate) ? getString(R.string.title_dialog_birthday) : getString(R.string.title_dialog_date));
                return dpd;
            } else {
                return super.onCreateDialog(savedInstanceState);
            }
        }

        @Override
		public void onDateSet(DatePicker view, int year, int month, int day)
		{
            if (getActivity() instanceof BioDroidActivity) {
                if (getTag().equals(TagStartDate)) {
                    ((BioDroidActivity) getActivity()).setStartCalendar(year, month, day);
                }
                else {
                    ((BioDroidActivity) getActivity()).setEndCalendar(year, month, day);
                }
            }
		}
	}

    TabAdapter getTabAdapter() {
        if (null == mTabAdapter) {
            mTabAdapter = new TabAdapter(getSupportFragmentManager());
        }
        return mTabAdapter;
    }

    /**
	 * 
	 * @author tay
	 * 
	 */
	public class TabAdapter extends FragmentPagerAdapter {

        private final int[] tfIconIds = {R.drawable.sin_phy, R.drawable.sin_emo, R.drawable.sin_int};
        private final int[] tfTitleIds = {R.string.tab_phy, R.string.tab_emo, R.string.tab_int};
        private final int[] tfDescriptionIds = {R.array.desc_phy, R.array.desc_emo, R.array.desc_int};
        private final int[] tfIntervals = {BioView.IVAL_PHYSICAL, BioView.IVAL_EMOTIONAL, BioView.IVAL_INTELECTUAL};
        private final int[] tfPhases;
        private final TabFragment[] tfTabs;
        private final String[] tfDescriptions;

        public TabAdapter(FragmentManager fm) {
            super(fm);
            BioLog.d(getClass().getSimpleName(), "new TabAdapter()");
            tfTabs = new TabFragment[tfIconIds.length];
            tfDescriptions = new String[tfIconIds.length];
            tfPhases = new int[tfIconIds.length];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TabFragment tf = (TabFragment) super.instantiateItem(container, position);
            tfTabs[position] = tf;
            return tf;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            tfTabs[position] = null;
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return tfIconIds.length;
        }

        @Override
        public Fragment getItem(int position) {
            BioLog.d(getClass().getSimpleName(), "TabAdapter::getItem() " + position);
            tfTabs[position] = TabFragment.getInstance(position, tfIconIds[position], tfTitleIds[position], getDescription(position));
            return tfTabs[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            BioLog.d(getClass().getSimpleName(), "getPageTitle() " + position);
            return getResources().getString(tfTitleIds[position]);
        }

        public Drawable getIcon(int position) {
            BioLog.d(getClass().getSimpleName(), "getIcon() " + position);
            return getResources().getDrawable(tfIconIds[position]);
        }

        public String getDescription(int position) {
            int phase = bioview.getPhase(tfIntervals[position]);
            BioLog.d(getClass().getSimpleName(), "getDescription(" + position + "," + phase + ")");
            return getResources().getStringArray(tfDescriptionIds[position])[phase];
        }

        public void updateDescription() {
            int position = mViewPager.getCurrentItem();
            if (null != tfTabs[position]) {
                BioLog.d(getClass().getSimpleName(), "updateDescription(" + position + ") child: " + tfTabs[position]);
                tfTabs[position].showDescription(getDescription(position));
            }
        }
    }

    /*
     * Interface BioView.BioCalendarProvider
     */

    @Override
    public Calendar getEndCalendar()
    {
        return calToday;
    }

    @Override
    public Calendar getStartCalendar()
    {
        return calBirth;
    }

    @Override
    public void onScroll()
    {
        updateDisplay();
    }
}
