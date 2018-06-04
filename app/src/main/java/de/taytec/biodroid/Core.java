package de.taytec.biodroid;

import android.appwidget.AppWidgetManager;
import android.content.ContextWrapper;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Core {
    public static final int IVAL_INTELECTUAL = 33;
    public static final int IVAL_EMOTIONAL = 28;
    public static final int IVAL_PHYSICAL = 23;

    public static final int HEIGHT = 0;
    public static final int CRITICAL = 1;
    public static final int LOW = 2;

    private Calendar calBirth;
    private Calendar calDate;
    private int ageDays;
    private ArrayList<ChangeListener> changeListeners = new ArrayList<ChangeListener>();


    public Core(Calendar birth, Calendar date) {
        calBirth = birth;
        calDate = date;
        ageDays = computeAge();
    }

    public Core() {
        calBirth = Calendar.getInstance();
        calBirth.set(1989, 10, 9);
        calDate = Calendar.getInstance();
        ageDays = computeAge();
    }

    public Calendar getStartCalendar() {
        return calBirth;
    }

    public Calendar getEndCalendar() {
        return calDate;
    }

    public Core setChangeListener(Core.ChangeListener listener) {
        changeListeners.add(listener);
        return this;
    }

    public void changedDates() {
        ageDays = computeAge();
        for (ChangeListener listener : changeListeners) {
            BioLog.d(getClass().getSimpleName(), "changedDates() class: " + listener.getClass().getSimpleName());
            listener.onCoreChanged(this);
        }
    }

    public void changedBirthday() {
        for (ChangeListener listener : changeListeners) {
            BioLog.d(getClass().getSimpleName(), "changedBirthday() class: " + listener.getClass().getSimpleName());
            listener.onBirthdayChanged(this);
        }
        changedDates();
    }

    public int getAge() {
        return ageDays;
    }

    protected int computeAge() {
        return getDaysSinceAD(calDate) - getDaysSinceAD(calBirth);
    }

    /**
     * Anzahl der Tage nach dem 1.1.0000 zurückgeben
     */
    public int getDaysSinceAD(Calendar cal) {
        int year = cal.get(Calendar.YEAR);
        int month = 1 + cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (month < 3) {
            year--;
            month += 12;
        }
        return day + (int) ((month + 1) * 30.6) + (int) (year * 365.25);
    }

    public int getPhase(int interval) {
        int di = ageDays % interval;
        if (di == 0 || di == interval / 2) {
            return CRITICAL;
        } else if (di < interval / 2) {
            return HEIGHT;
        } else {
            return LOW;
        }
    }

    public void add(int field, int i) {
        calDate.add(field, i);
        changedDates();
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

    public void setStartTime(Date date) {
        calBirth.setTime(date);
        checkCalendar(calBirth);
        changedBirthday();
    }

    public void setBirthday(int year, int month, int day) {
        calBirth.set(year, month, day);
        checkCalendar(calBirth);
        changedBirthday();
    }

    public void setDate(int year, int month, int day) {
        calDate.set(year, month, day);
        checkCalendar(calDate);
        changedDates();
    }

    public interface ChangeListener {
        public void onCoreChanged(Core core);
        public void onBirthdayChanged(Core core);
    }
}