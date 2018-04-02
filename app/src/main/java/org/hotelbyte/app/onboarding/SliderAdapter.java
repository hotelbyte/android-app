package org.hotelbyte.app.onboarding;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.hotelbyte.app.R;

/**
 * Created by lexfaraday
 */
public class SliderAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;

    private int[] slideImages = {
            R.drawable.slide_icon_01,
            R.drawable.slide_icon_02,
            R.drawable.slide_icon_03
    };

    private int[] slideHeaders = {
            R.string.slide_header_one,
            R.string.slide_header_two,
            R.string.slide_header_three
    };

    private int[] slideDescriptions = {
            R.string.slide_body_one,
            R.string.slide_body_two,
            R.string.slide_body_three
    };

    public SliderAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return slideHeaders.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return  view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View view = null;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (layoutInflater != null) {
            view = layoutInflater.inflate(R.layout.slide_layout, container, false);

            ImageView slideImageView = view.findViewById(R.id.slideImageView);
            TextView slideHeadingTextView = view.findViewById(R.id.slideHeadingText);
            TextView slideBodyTextView = view.findViewById(R.id.slideBodyText);

            slideImageView.setImageResource(slideImages[position]);
            slideHeadingTextView.setText(context.getResources().getString(slideHeaders[position]));
            slideBodyTextView.setText(context.getResources().getString(slideDescriptions[position]));

            container.addView(view);
        }

        return view;
    }

    // TODO modify relative layout to constraint layout
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof ConstraintLayout) {
            container.removeView((ConstraintLayout) object);
        } else if (object instanceof RelativeLayout) {
            container.removeView((RelativeLayout) object);
        }
    }
}
