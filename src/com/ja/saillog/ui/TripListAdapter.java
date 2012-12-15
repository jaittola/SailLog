package com.ja.saillog.ui;

import com.ja.saillog.database.TripDB;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

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
       TripListItem tli = new TripListItem(context);
        return setTripContent(tli, cursor);
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
        TripListItem tli = (TripListItem) view;
        tli.setTripName(cursor.getString(cursor.getColumnIndex(TripDB.tripNameColumn)));
        tli.setPlaces(cursor.getString(cursor.getColumnIndex(TripDB.startLocationColumn)),
                      cursor.getString(cursor.getColumnIndex(TripDB.endLocationColumn)));
        tli.setSelected(1 == cursor.getInt(cursor.getColumnIndex(TripDB.selectedColumn)));

        return view;
    }
}
