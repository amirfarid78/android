package com.coheser.app.adapters;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.exoplayer2.util.Log;
import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.models.SliderModel;
import com.coheser.app.simpleclasses.Functions;

import java.util.ArrayList;


public class SlidingAdapter extends PagerAdapter {


    AdapterClickListener adapterClickListener;
    private final ArrayList<SliderModel> imageList;
    private LayoutInflater inflater;
    private final Context context;

    public SlidingAdapter(Context context, ArrayList<SliderModel> IMAGES, AdapterClickListener click_listener) {
        this.context = context;
        this.imageList = IMAGES;
        this.adapterClickListener = click_listener;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View imageLayout = inflater.inflate(R.layout.item_slider_layout, view, false);

        assert imageLayout != null;

        final SimpleDraweeView imageView = imageLayout.findViewById(R.id.save_image);
        final RelativeLayout slider_rlt = imageLayout.findViewById(R.id.slider_rlt);

        imageView.setController(Functions.frescoImageLoad(imageList.get(position).getImage(), R.drawable.image_placeholder, imageView, false));
        Log.d(Constants.tag,"slider image :"+imageList.get(position).getImage());

        slider_rlt.setOnClickListener(v -> {
            adapterClickListener.onItemClick(v, position, imageList.get(position));

        });

        view.addView(imageLayout, 0);


        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
