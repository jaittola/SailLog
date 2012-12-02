package com.ja.saillog;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class TripListAdapter extends CursorAdapter {

    public TripListAdapter(Context context, Cursor cursor, boolean autoRequery) {
        super(context, cursor, autoRequery);
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        setTripContent(view, cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View tv = inflater.inflate(R.layout.trip_list_item, parent, false);
        return setTripContent(tv, cursor);
    }
    
    public void done() {
        Cursor c = getCursor();
        c.close();
    }
    
    public void requery() {
        getCursor().requery();
    }
    
    public boolean hasStableIds() {
        return true;
    }
    
    private View setTripContent(View view, Cursor cursor) {
        TextView tv = (TextView) view;
        tv.setText(cursor.getString(cursor.getColumnIndex("trip_name")));

        return view;
    }
}
