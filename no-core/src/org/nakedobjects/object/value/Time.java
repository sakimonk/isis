package org.nakedobjects.object.value;

import org.nakedobjects.Clock;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.ValueParseException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;


/**
 * Value object representing a time value.
 * <p>
 * NOTE: this class currently does not support about listeners
 * </p>
 */

/* other methods to implement

 comparision methods

 sameHourAs() hour ==hour
 sameMinuteAs() minutes = minutes
 sameTimeAs(hour, min) hour == hour & minutes == minutes

 withinNextTimePeriod(int hours, int minutes);
 withinTimePeriod(Date d, int hours, int minutes);
 withinPreviousTimePeriod(int hours, int minutes); d.hour >= this.hour >= d.hour + hours & d.minutes >= this.minutes >= d.minutes + minutes
 */
public class Time extends Magnitude {
	private static final Logger LOG = Logger.getLogger(Time.class);
    private static final DateFormat LONG_FORMAT = DateFormat.getTimeInstance(DateFormat.LONG);
    private static final DateFormat MEDIUM_FORMAT = DateFormat.getTimeInstance(DateFormat.MEDIUM);
	private static final long serialVersionUID = 1L;
	private static final DateFormat SHORT_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT);
	private static final DateFormat ISO_LONG = new SimpleDateFormat("HH:mm");
	private static final DateFormat ISO_SHORT = new SimpleDateFormat("HHmm");
    private final static long zero;
	private final static int zoneOffset;
	public static final int MINUTE = 60;
	public static final int HOUR = 60 * MINUTE;
	public static final int DAY = 24 * HOUR;
	private static Clock clock;
	
	public static void setClock(Clock clock) {
	    Time.clock = clock;
	}

    static {
   //     zoneOffset = TimeZone.getDefault().getRawOffset();
        zoneOffset = new Date().getTimezoneOffset() * 60 * 1000;
        	
        Calendar cal = Calendar.getInstance();
		// set to 1-Jan-1970 00:00:00 (the epoch)
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.clear(Calendar.AM_PM);
		cal.clear(Calendar.HOUR);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, 1970);
        zero = cal.getTime().getTime() - zoneOffset;
        
        
        LOG.info("Zone offset " + zoneOffset);
        LOG.info("Locale " + Locale.getDefault());
        LOG.info("Short fomat " + SHORT_FORMAT.format(new Date()));
        LOG.info("Medium fomat " + MEDIUM_FORMAT.format(new Date()));
        LOG.info("Long fomat " + LONG_FORMAT.format(new Date()));
    }

    static long getZero() {
        return zero / 1000;
    }

    private java.util.Date date;
    private transient DateFormat format = SHORT_FORMAT;

    /*
    Create a Time object for storing a time with the time set to the current time.
    */
    public Time() {
        if(clock == null) {
            throw new NakedObjectRuntimeException("Clock not set up");
        }
        setValue(new java.util.Date(clock.getTime()));
    }

    /*
    Create a Time object for storing a time with the time set to the specified hours and minutes.
    */
    public Time(int hour, int minute) {
        setValue(hour, minute);
    }

    /*
    Create a Time object for storing a time with the time set to the specified time.
    */
    public Time(Time time) {
        date = time.date;
    }

    /**
     Add the specified hours and minutes to this time value.
     */
    public void add(int hours, int minutes) {
        checkCanOperate();

        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        set(cal);
    }

    public Calendar calendarValue() {
        if (date == null) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return c;
    }

    private void checkTime(int hour, int minute, int second) {
        if ((hour < 0) || (hour > 23)) {
            throw new IllegalArgumentException(
                "Hour must be in the range 0 - 23 inclusive");
        }

        if ((minute < 0) || (minute > 59)) {
            throw new IllegalArgumentException(
                "Minute must be in the range 0 - 59 inclusive");
        }

        if ((second < 0) || (second > 59)) {
            throw new IllegalArgumentException(
                "Second must be in the range 0 - 59 inclusive");
        }
    }

    public void clear() {
        date = null;
    }

    public void copyObject(Naked object) {
        if (!(object instanceof Time)) {
            throw new IllegalArgumentException(
                "Can only copy the value of  a Date object");
        }

        date = (object == null) ? null : ((Time) object).date;
    }

    /**
     Returns a Calendar object with the irrelevant field (determined by this objects type) set to zero.
     */
    private Calendar createCalendar() {
        Calendar cal = Calendar.getInstance();

        // clear all aspects of the time that are not used
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.clear(Calendar.AM_PM);
		cal.clear(Calendar.HOUR);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, 1970);

        return cal;
    }

    public java.util.Date dateValue() {
        return (date == null) ? null : date;
    }

    /**
     @deprecated  replaced by dateValue
     @see #dateValue
     */
    public java.util.Date getDate() {
        checkCanOperate();

        return date;
    }

    /**
     *  Return true if the date is blank
     */
    public boolean isEmpty() {
        return date == null;
    }

    /**
     returns true if the time of this object has the same value as the specified time
     */
    public boolean isEqualTo(Magnitude time) {
        if (time instanceof Time) {
            return (date == null) ? false : (date.equals(((Time) time).date));
        } else {
            throw new IllegalArgumentException("Parameter must be of type Time");
        }
    }

    /**
     returns true if the time of this object is earlier than the specified time
     */
    public boolean isLessThan(Magnitude time) {
        checkCanOperate();

        if (time instanceof Time) {
            return (date != null) && !time.isEmpty() &&
            date.before(((Time) time).date);
        } else {
            throw new IllegalArgumentException("Parameter must be of type Time");
        }
    }

    /**
     * The number of seconds since midnight.
     */
    public long longValue() {
        checkCanOperate();

        return (date.getTime()  -  zoneOffset) / 1000;
    }

    public void parse(String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            text = text.trim();

            String str = text.toLowerCase();
            Calendar cal = createCalendar();

            if (str.equals("now")) {
            } else if (str.startsWith("+")) {
                int hours;

                hours = Integer.valueOf(str.substring(1)).intValue();
                cal.setTime(date);
                cal.add(Calendar.HOUR_OF_DAY, hours);
            } else if (str.startsWith("-")) {
                int hours;

                hours = Integer.valueOf(str.substring(1)).intValue();
                cal.setTime(date);
                cal.add(Calendar.HOUR_OF_DAY, -hours);
            } else {
                DateFormat[] formats = new DateFormat[] {
                        LONG_FORMAT, MEDIUM_FORMAT, SHORT_FORMAT, ISO_LONG, ISO_SHORT
                    };

                for (int i = 0; i < formats.length; i++) {
                    try {
                        cal.setTime(formats[i].parse(text));

                        break;
                    } catch (ParseException e) {
                        if ((i + 1) == formats.length) {
                            throw new ValueParseException(e, "Invalid time '" + text + "' for locale " + Locale.getDefault());
                        }
                    }
                }
            }

            set(cal);
        }
    }

    /**
     * Reset this time so it contains the current time.
     * @see org.nakedobjects.object.NakedValue#reset()
     */
    public void reset() {
        setValue(new Date(clock.getTime()));
    }

    public void restoreString(String data) {
        if (data.equals("NULL")) {
            clear();
        } else {
            int hour = Integer.valueOf(data.substring(0, 2)).intValue();
            int minute = Integer.valueOf(data.substring(2)).intValue();
            setValue(hour, minute);
        }
    }

    public String saveString() {
        Calendar cal = calendarValue();

        if (cal == null) {
            return "NULL";
        } else {
            StringBuffer data = new StringBuffer(4);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            data.append((hour <= 9) ? "0" : "");
            data.append(hour);

            int minute = cal.get(Calendar.MINUTE);
            data.append((minute <= 9) ? "0" : "");
            data.append(minute);

            return data.toString();
        }
    }

    private void set(Calendar cal) {
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, 1970);
        date = cal.getTime();
    }

    /*
    Sets this object's time to be the same as the specified hour, minute and second.
    */
    public void setValue(int hour, int minute) {
        checkTime(hour, minute, 0);

        Calendar cal = createCalendar();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        set(cal);
    }

    public void setValue(java.util.Date date) {
        if (date == null) {
            this.date = null;
        } else {
            Calendar cal = Calendar.getInstance();

            cal.setTime(date);
            set(cal);
        }
    }

	public void setValue(long time) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(new Date(time * 1000));
        set(cal);
	}

    public void setValue(Time time) {
        if (time == null) {
            date = null;
        } else {
            this.date = new Date(time.date.getTime());
        }
    }

    public Title title() {
        return new Title((date == null) ? "" : format.format(date));
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/