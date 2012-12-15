package com.ja.saillog.ui;

import com.ja.saillog.R;
import com.ja.saillog.R.id;
import com.ja.saillog.R.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TripListItem extends LinearLayout {

    public TripListItem(Context context) {
        super(context);
        setupView(context);
    }

    public TripListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView(context);
    }

    public void setTripName(String name) {
        tripName.setText(name);
    }

    public void setPlaces(String startLocation, String endLocation) {
        tripInfo.setText(String.format("%s - %s", startLocation, endLocation));
    }

    public void setSelected(boolean isSelected) {
        isSelectedToggle.setChecked(isSelected);
    }

    private void setupView(Context context) {
        View.inflate(context, R.layout.trip_list_item, this);

        tripName = (TextView) findViewById(R.id.tripNameView);
        tripInfo = (TextView) findViewById(R.id.tripInfoView);
        isSelectedToggle = (CheckBox) findViewById(R.id.tripSelectionToggle);
    }

    private TextView tripName;
    private TextView tripInfo;
    private CheckBox isSelectedToggle;
}
