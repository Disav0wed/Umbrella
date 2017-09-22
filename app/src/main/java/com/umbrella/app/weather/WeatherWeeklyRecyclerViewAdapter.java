package com.umbrella.app.weather;

import android.content.Context;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.umbrella.R;
import com.umbrella.app.userinput.UserInputManager;
import com.umbrella.app.weather.models.HourlyWeather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by ibrahimserpici on 9/22/17.
 */

class WeatherWeeklyRecyclerViewAdapter extends RecyclerView.Adapter<WeatherWeeklyRecyclerViewAdapter.ViewHolder> {

    // This constant is the factor who came naturally of ordering the information in groups of 2 hrs. You can change it
    // to 3 or 4 and the information will be ordered in groups of 3 or 4 hrs respectively.
    private final static int GROUPING_FACTOR = 2;

    private Context context;

    private List weeklyWeatherList;

    WeatherWeeklyRecyclerViewAdapter(Context context, List weeklyWeatherList) {
        this.context = context;
        this.weeklyWeatherList = weeklyWeatherList;
    }

    /** ViewHolder pattern: Inner class needed to keep the references between widgets and data to improve the performance */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        GridLayout hourlyWeatherGridLayout;

        ViewHolder(View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.titleTextView);
            hourlyWeatherGridLayout = itemView.findViewById(R.id.hourlyWeatherGridLayout);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_day_row_cardview_layout, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        List dailyWeatherList = (List) weeklyWeatherList.get(position);

        // Populate our views
        populateTitleView(viewHolder, dailyWeatherList);
        populateGridLayout(viewHolder, dailyWeatherList);
    }

    /**
     * Populate the CardView's titleTextView
     * */
    private void populateTitleView(ViewHolder viewHolder, List dailyWeatherList) {

        // Get the date of the forecast day
        Date forecastDate = ((HourlyWeather) dailyWeatherList.get(0)).getDate();
        Calendar forecastCalendar = Calendar.getInstance();
        forecastCalendar.setTime(forecastDate);

        // Get today's date
        Calendar todayCalendar = Calendar.getInstance();

        // Get tomorrow's date
        Calendar tomorrowCalendar = Calendar.getInstance();
        tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);

        // Compare in order to choo the posible title to be shown
        if (forecastCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) && (forecastCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR))) {

            viewHolder.titleTextView.setText("Today");

        } else if (forecastCalendar.get(Calendar.YEAR) == tomorrowCalendar.get(Calendar.YEAR) && (forecastCalendar.get(Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(Calendar.DAY_OF_YEAR))) {

            viewHolder.titleTextView.setText("Tomorrow");

        } else {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM, dd");
            String forecastDateString = simpleDateFormat.format(forecastDate);

            viewHolder.titleTextView.setText(forecastDateString);
        }
    }


    /**
     * Populate the CardView's GridLayou, which contains 12 slots of 2 hrs each
     * */
    private void populateGridLayout(ViewHolder viewHolder, List dailyWeatherList) {

        // Create a copy of dailyWeatherList (which is sorted by time), and sort it by temperature
        // It also removes the impair forecast values because they're unusefull since we're showing
        // values by groups of two hours, and they only causes problems to et
        List dailyWeatherListByTemperature = new ArrayList();
        for (int i = 0; i < dailyWeatherList.size(); i+=GROUPING_FACTOR) {
            dailyWeatherListByTemperature.add(dailyWeatherList.get(i));
        }
        Collections.sort(dailyWeatherListByTemperature, new TemperatureComparator());

        // Then, get mininum and maximum temperatures to compare and highlight
        String minTemperatureString;
        String maxTemperatureString;
        String temperatureUnits = UserInputManager.getTemperatureUnits();
        if (temperatureUnits.equals("Fahrenheit")) {
            minTemperatureString = ((HourlyWeather) dailyWeatherListByTemperature.get(0)).getTemperatureFahrenheitValue();
            maxTemperatureString = ((HourlyWeather) dailyWeatherListByTemperature.get(dailyWeatherListByTemperature.size() - 1)).getTemperatureFahrenheitValue();
        } else {
            minTemperatureString = ((HourlyWeather) dailyWeatherListByTemperature.get(0)).getTemperatureCelsiusValue();
            maxTemperatureString = ((HourlyWeather) dailyWeatherListByTemperature.get(dailyWeatherListByTemperature.size() - 1)).getTemperatureCelsiusValue();
        }

        // Populate the card's GridLayout. We will only show 12 slots (each two hours)
        int i = 0;
        boolean minTempIdentify = false, maxTempIdentify = false;
        for (i = 0; i < dailyWeatherList.size(); i+=GROUPING_FACTOR) {

            HourlyWeather hourlyWeatherDataModel = (HourlyWeather) dailyWeatherList.get(i);

            LinearLayout hourlyWeatherLinearLayout = (LinearLayout) viewHolder.hourlyWeatherGridLayout.getChildAt(i/GROUPING_FACTOR);
            hourlyWeatherLinearLayout.setVisibility(View.VISIBLE);

            // Populate the timeTextView with the time
            TextView timeTextView = hourlyWeatherLinearLayout.findViewById(R.id.timeTextView);
            timeTextView.setText(hourlyWeatherDataModel.getTimeValue());

            // Populate the conditionImageView with the proper icon.
            // Here is important to mention the use of the method 'mutate()' in the drawable calls.
            // When it is invoked, the constant state shared between all the drawables with the same resource ID, is duplicated. Thus it will be posible
            // to alter the color of a particular instance of the drawable, without reflect the color change on the rest of them.
            int imageResource = context.getResources().getIdentifier(hourlyWeatherDataModel.getConditionResourceID(), null, context.getPackageName());
            Drawable iconDrawable = ContextCompat.getDrawable(context,imageResource);
            iconDrawable = iconDrawable.mutate();

            // Populate the temperatureTextView with the temperature either Fahrenheit or Celsius
            TextView temperatureTextView = hourlyWeatherLinearLayout.findViewById(R.id.temperatureTV);
            String temperatureString;
            if (temperatureUnits.equals("Fahrenheit")) {

                temperatureString = hourlyWeatherDataModel.getTemperatureFahrenheitValue();

                // Detect the max and min temperatures and use color to highlight them
                if (!minTempIdentify && temperatureString.compareTo(minTemperatureString) == 0) {
                    temperatureTextView.setTextColor(ContextCompat.getColor(context,R.color.coolWeatherColor));
                    iconDrawable.setColorFilter(new LightingColorFilter(ContextCompat.getColor(context,android.R.color.black), ContextCompat.getColor(context,R.color.coolWeatherColor))); // Use of 'mutate()' here
                    timeTextView.setTextColor(ContextCompat.getColor(context,R.color.coolWeatherColor));
                    minTempIdentify = true;

                } else if (!maxTempIdentify && temperatureString.compareTo(maxTemperatureString) == 0) {
                    temperatureTextView.setTextColor(ContextCompat.getColor(context,R.color.warmWeatherColor));
                    iconDrawable.setColorFilter(new LightingColorFilter(ContextCompat.getColor(context,android.R.color.black), ContextCompat.getColor(context,R.color.warmWeatherColor))); // Use of 'mutate()' here
                    timeTextView.setTextColor(ContextCompat.getColor(context,R.color.warmWeatherColor));
                    maxTempIdentify = true;

                } else {
                    temperatureTextView.setTextColor(ContextCompat.getColor(context,android.R.color.black));
                    iconDrawable.setColorFilter(new LightingColorFilter(ContextCompat.getColor(context,android.R.color.black), ContextCompat.getColor(context,android.R.color.black))); // Use of 'mutate()' here
                    timeTextView.setTextColor(ContextCompat.getColor(context,android.R.color.black));
                }

                // Just adding the Units abbreviation
                temperatureString = temperatureString + "° F";

            } else {

                temperatureString = hourlyWeatherDataModel.getTemperatureCelsiusValue();

                // Detect the max and min temperatures and use color to highlight them
                if (!minTempIdentify && temperatureString.compareTo(minTemperatureString) == 0) {
                    temperatureTextView.setTextColor(ContextCompat.getColor(context,R.color.coolWeatherColor));
                    iconDrawable.setColorFilter(new LightingColorFilter(ContextCompat.getColor(context,android.R.color.black), ContextCompat.getColor(context,R.color.coolWeatherColor)));
                    timeTextView.setTextColor(ContextCompat.getColor(context,R.color.coolWeatherColor));
                    minTempIdentify = true;

                } else if (!maxTempIdentify && temperatureString.compareTo(maxTemperatureString) == 0) {
                    temperatureTextView.setTextColor(ContextCompat.getColor(context,R.color.warmWeatherColor));
                    iconDrawable.setColorFilter(new LightingColorFilter(ContextCompat.getColor(context,android.R.color.black), ContextCompat.getColor(context,R.color.warmWeatherColor)));
                    timeTextView.setTextColor(ContextCompat.getColor(context,R.color.warmWeatherColor));
                    maxTempIdentify = true;

                } else {
                    temperatureTextView.setTextColor(ContextCompat.getColor(context,android.R.color.black));
                    iconDrawable.setColorFilter(new LightingColorFilter(ContextCompat.getColor(context,android.R.color.black), ContextCompat.getColor(context,android.R.color.black)));
                    timeTextView.setTextColor(ContextCompat.getColor(context,android.R.color.black));
                }

                // Just adding the Units abbreviation
                temperatureString = temperatureString + "° C";
            }

            ImageView conditionImageView = hourlyWeatherLinearLayout.findViewById(R.id.conditionImageView);
            conditionImageView.setImageDrawable(iconDrawable);

            temperatureTextView.setText(temperatureString);
        }

        // Remove children not being used from GridLayout
        if (i/GROUPING_FACTOR < viewHolder.hourlyWeatherGridLayout.getChildCount()) {

            for (int j = i/GROUPING_FACTOR; j < viewHolder.hourlyWeatherGridLayout.getChildCount(); j++) {
                viewHolder.hourlyWeatherGridLayout.getChildAt(j).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return weeklyWeatherList.size();
    }

    /**
     * Compares and sorts a new auxiliar instance of the dailyWeather data model
     *
     * The dailyWeather data model is sorted by time. We need anothe auxiliar copy of this data model
     * to find the maximum and minimum temperatures so we can be able to highlight them on the
     * forecast section
     * */
    private class TemperatureComparator implements Comparator<HourlyWeather> {

        @Override
        public int compare(HourlyWeather hourlyWeatherDataModel, HourlyWeather t1) {

            if (hourlyWeatherDataModel.getTemperatureFahrenheitValue().compareTo(t1.getTemperatureFahrenheitValue()) < 0) {

                return -1;

            } else if (hourlyWeatherDataModel.getTemperatureFahrenheitValue().compareTo(t1.getTemperatureFahrenheitValue()) > 0) {

                return 1;
            }

            return 0;
        }
    }
}
