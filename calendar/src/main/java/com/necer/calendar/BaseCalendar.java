package com.necer.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.necer.R;
import com.necer.adapter.BaseCalendarAdapter;
import com.necer.listener.OnDateChangedListener;
import com.necer.listener.OnYearMonthChangedListener;
import com.necer.utils.Attrs;
import com.necer.utils.Util;
import com.necer.view.BaseCalendarView;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by necer on 2018/9/11.
 * qq群：127278900
 */
public abstract class BaseCalendar extends ViewPager {

    protected int mCalendarSize;
    private BaseCalendarAdapter calendarAdapter;
    private Attrs attrs;
    protected BaseCalendarView mCurrView;//当前显示的页面
    protected BaseCalendarView mLastView;//当前显示的页面的上一个页面
    protected BaseCalendarView mNextView;//当前显示的页面的下一个页面

    protected LocalDate mSelectDate;//日历上面点击选中的日期,包含点击选中和翻页选中
    protected LocalDate mOnClickDate;//专值点击选中的日期

    private List<LocalDate> mPointList;

    protected OnYearMonthChangedListener onYearMonthChangedListener;
    //上次回调的年，月
    protected int mLaseYear;
    protected int mLastMonth;


    protected OnDateChangedListener onDateChangedListener;

    public BaseCalendar(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.NCalendar);

        attrs = new Attrs();
        attrs.solarTextColor = ta.getColor(R.styleable.NCalendar_solarTextColor, getResources().getColor(R.color.solarTextColor));
        attrs.todaySolarTextColor = ta.getColor(R.styleable.NCalendar_todaySolarTextColor, getResources().getColor(R.color.todaySolarTextColor));
        attrs.lunarTextColor = ta.getColor(R.styleable.NCalendar_lunarTextColor, getResources().getColor(R.color.lunarTextColor));
        attrs.selectCircleColor = ta.getColor(R.styleable.NCalendar_selectCircleColor, getResources().getColor(R.color.selectCircleColor));
        attrs.hintColor = ta.getColor(R.styleable.NCalendar_hintColor, getResources().getColor(R.color.hintColor));
        attrs.solarTextSize = ta.getDimension(R.styleable.NCalendar_solarTextSize, Util.sp2px(context, 18));
        attrs.lunarTextSize = ta.getDimension(R.styleable.NCalendar_lunarTextSize, Util.sp2px(context, 10));
        attrs.lunarDistance = ta.getDimension(R.styleable.NCalendar_lunarDistance, Util.sp2px(context, 15));
        attrs.holidayDistance = ta.getDimension(R.styleable.NCalendar_holidayDistance, Util.sp2px(context, 15));
        attrs.holidayTextSize = ta.getDimension(R.styleable.NCalendar_holidayTextSize, Util.sp2px(context, 10));
        attrs.selectCircleRadius = ta.getDimension(R.styleable.NCalendar_selectCircleRadius, Util.dp2px(context, 22));
        attrs.isShowLunar = ta.getBoolean(R.styleable.NCalendar_isShowLunar, true);
        attrs.isDefaultSelect = ta.getBoolean(R.styleable.NCalendar_isDefaultSelect, true);
        attrs.pointSize = ta.getDimension(R.styleable.NCalendar_pointSize, Util.dp2px(context, 2));
        attrs.pointDistance = ta.getDimension(R.styleable.NCalendar_pointDistance, Util.dp2px(context, 12));
        attrs.pointColor = ta.getColor(R.styleable.NCalendar_pointColor, getResources().getColor(R.color.pointColor));
        attrs.hollowCircleColor = ta.getColor(R.styleable.NCalendar_hollowCircleColor, getResources().getColor(R.color.hollowCircleColor));
        attrs.hollowCircleStroke = ta.getDimension(R.styleable.NCalendar_hollowCircleStroke, Util.dp2px(context, 1));
        attrs.monthCalendarHeight = (int) ta.getDimension(R.styleable.NCalendar_calendarHeight, Util.dp2px(context, 300));
        attrs.duration = ta.getInt(R.styleable.NCalendar_duration, 240);
        attrs.isShowHoliday = ta.getBoolean(R.styleable.NCalendar_isShowHoliday, true);
        attrs.holidayColor = ta.getColor(R.styleable.NCalendar_holidayColor, getResources().getColor(R.color.holidayColor));
        attrs.workdayColor = ta.getColor(R.styleable.NCalendar_workdayColor, getResources().getColor(R.color.workdayColor));
        attrs.backgroundColor = ta.getColor(R.styleable.NCalendar_backgroundColor, getResources().getColor(R.color.white));
        attrs.firstDayOfWeek = ta.getInt(R.styleable.NCalendar_firstDayOfWeek, Attrs.SUNDAY);
        attrs.pointLocation = ta.getInt(R.styleable.NCalendar_pointLocation, Attrs.UP);
        attrs.defaultCalendar = ta.getInt(R.styleable.NCalendar_defaultCalendar, Attrs.MONTH);
        attrs.holidayLocation = ta.getInt(R.styleable.NCalendar_holidayLocation, Attrs.TOP_RIGHT);

        String startString = ta.getString(R.styleable.NCalendar_startDate);
        String endString = ta.getString(R.styleable.NCalendar_endDate);


        ta.recycle();

        mPointList = new ArrayList<>();

        LocalDate startDate = new LocalDate(startString == null ? "1901-01-01" : startString);
        LocalDate endDate = new LocalDate(endString == null ? "2099-12-31" : endString);

        mCalendarSize = getCalendarSize(startDate, endDate, attrs.firstDayOfWeek);
        int currNum = getTwoDateNum(startDate, new LocalDate(), attrs.firstDayOfWeek);

        calendarAdapter = getCalendarAdapter(context, attrs, mCalendarSize, currNum);
        setAdapter(calendarAdapter);

        setBackgroundColor(attrs.backgroundColor);

        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(final int position) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        reDraw(position);
                    }
                });
            }
        });

        setCurrentItem(currNum);

    }

    private void reDraw(int position) {

        this.mCurrView = calendarAdapter.getBaseCalendarView(position);
        this.mLastView = calendarAdapter.getBaseCalendarView(position - 1);
        this.mNextView = calendarAdapter.getBaseCalendarView(position + 1);


        LocalDate initialDate = mCurrView.getInitialDate();
        //当前页面的初始值和上个页面选中的日期，相差几月或几周，再又上个页面选中的日期得出当前页面选中的日期
        if (mSelectDate != null) {
            int currNum = getTwoDateNum(mSelectDate, initialDate, attrs.firstDayOfWeek);//得出两个页面相差几个
            mSelectDate = getDate(mSelectDate, currNum);
        } else {
            mSelectDate = initialDate;
        }

        //绘制的规则：1、默认选中，每个页面都会有选中。1、默认不选中，但是点击了当前页面的某个日期
        boolean isDraw = attrs.isDefaultSelect || (mSelectDate.equals(mOnClickDate));
        notifyView(mSelectDate, isDraw);

        //选中回调 ,绘制了才会回到
        if (isDraw) {
            onSelcetDate(mSelectDate);
        }

        //年月回调
        onYearMonthChanged(mSelectDate.getYear(), mSelectDate.getMonthOfYear());
        //日期回调
        onDateChanged(mSelectDate, isDraw);
    }

    public void setPointList(List<String> list) {
        mPointList.clear();
        for (int i = 0; i < list.size(); i++) {
            mPointList.add(new LocalDate(list.get(i)));
        }
        if (mCurrView != null) {
            mCurrView.invalidate();
        }
        if (mLastView != null) {
            mLastView.invalidate();
        }
        if (mNextView != null) {
            mNextView.invalidate();
        }
    }

    //刷新页面
    protected void notifyView(LocalDate currectSelectDate, boolean isDraw) {
        this.mSelectDate = currectSelectDate;
        if (mCurrView != null) {
            mCurrView.setSelectDate(currectSelectDate, mPointList, isDraw);
        }

        if (mLastView != null) {
            mLastView.setSelectDate(getLastSelectDate(currectSelectDate), mPointList, isDraw);
        }
        if (mNextView != null) {
            mNextView.setSelectDate(getNextSelectDate(currectSelectDate), mPointList, isDraw);
        }
    }


    protected abstract BaseCalendarAdapter getCalendarAdapter(Context context, Attrs attrs, int calendarSize, int currNum);

    /**
     * 日历的页数
     *
     * @return
     */
    protected abstract int getCalendarSize(LocalDate startDate, LocalDate endDate, int type);

    /**
     * 日历开始日期和结束日期的相差数量
     *
     * @return
     */
    protected abstract int getTwoDateNum(LocalDate startDate, LocalDate endDate, int type);

    /**
     * 相差count之后的的日期
     *
     * @param localDate
     * @param count
     * @return
     */
    protected abstract LocalDate getDate(LocalDate localDate, int count);

    /**
     * 重绘当前页面时，获取上个月选中的日期
     *
     * @return
     */
    protected abstract LocalDate getLastSelectDate(LocalDate currectSelectDate);


    /**
     * 重绘当前页面时，获取下个月选中的日期
     *
     * @return
     */
    protected abstract LocalDate getNextSelectDate(LocalDate currectSelectDate);


    /**
     * 日历上面选中的日期，有选中圈的才会回调
     *
     * @param localDate
     */
    protected abstract void onSelcetDate(LocalDate localDate);

    /**
     * 年份和月份变化回调,点击和翻页都会回调，不管有没有日期选中
     *
     * @param year
     * @param month
     */
    public void onYearMonthChanged(int year, int month) {
        if (onYearMonthChangedListener != null && (year != mLaseYear || month != mLastMonth)) {
            mLaseYear = year;
            mLastMonth = month;
            onYearMonthChangedListener.onYearMonthChanged(this, year, month);
        }
    }

    /**
     * 任何操作都会回调
     *
     * @param localDate
     * @param isDraw    页面是否选中
     */
    public void onDateChanged(LocalDate localDate, boolean isDraw) {
        if (onDateChangedListener != null) {
            onDateChangedListener.onDateChanged(this, localDate, isDraw);
        }
    }

    public void setOnYearMonthChangeListener(OnYearMonthChangedListener onYearMonthChangedListener) {
        this.onYearMonthChangedListener = onYearMonthChangedListener;
    }

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        this.onDateChangedListener = onDateChangedListener;
    }


    /**
     * 跳转日期
     *
     * @param formatDate
     */
    public void jumpDate(String formatDate) {
        LocalDate jumpDate = new LocalDate(formatDate);
        mOnClickDate = jumpDate;
        int num = getTwoDateNum(mSelectDate, jumpDate, attrs.firstDayOfWeek);
        setCurrentItem(getCurrentItem() + num, Math.abs(num) == 1);
        notifyView(jumpDate, true);
    }


    //
    protected void jumpDate(LocalDate localDate, boolean isDraw) {
        if (mSelectDate != null) {
            int num = getTwoDateNum(mSelectDate, localDate, attrs.firstDayOfWeek);
            setCurrentItem(getCurrentItem() + num, Math.abs(num) == 1);
            notifyView(localDate, isDraw);
        }
    }


    protected Attrs getAttrs() {
        return attrs;
    }

}
