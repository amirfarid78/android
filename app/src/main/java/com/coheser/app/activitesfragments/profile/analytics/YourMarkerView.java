package com.coheser.app.activitesfragments.profile.analytics;

import android.content.Context;
import android.widget.TextView;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.simpleclasses.Functions;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;

public class YourMarkerView extends MarkerView {

    ArrayList<GraphData> list;
    private final TextView title;
    private final TextView text;
    private MPPointF mOffset;

    public YourMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        // find your layout components
        title = findViewById(R.id.title_txt);
        text = findViewById(R.id.text);
    }

    public void setDataList(ArrayList<GraphData> list) {
        this.list = list;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        Functions.printLog(Constants.tag, "x value:" + e.getX());

        title.setText(DateOperations.INSTANCE.changeDateFormat("yyyy-MM-dd", "MMM dd", list.get((int) e.getX()).date));
        text.setText("" + e.getY());

        // this will perform necessary layouting
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {

        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }
}
