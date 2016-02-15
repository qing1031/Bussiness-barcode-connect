package com.foodlogiq.distributormobile.viewAdapters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ScrollView;

import com.foodlogiq.distributormobile.activities.CreateIncidentActivity;

/**
 * Mercilessly ripped from Stack Overflow
 * http://stackoverflow.com/questions/8481844/gridview-height-gets-cut/8483078#8483078
 * this should be default in android!!!
 * <p>
 * Allows a grid view to expand to encompass the full height of it's contents. This is to help
 * circumvent any use case where the grid view is going to be used inside of a
 * {@link ScrollView}, where
 * the scroll event will be consumed by the {@link ScrollView} instead of the {@link GridView}
 * </p>
 * <p>
 * NOTE: This isn't in use anywhere in our application as of V1.0, but there's a good chance it will
 * be if we ever decide to show photos inside of a {@link ScrollView} layout. For example,
 * formerly we had the photos {@link GridView} on the same page as the
 * {@link CreateIncidentActivity#INCIDENT_PROBLEM_SLIDE}, which
 * had to be a {@link ScrollView} in order to handle the extra data below the viewport.
 * This class was "written"(read: stolen) to mitigate issues with the
 * {@link ScrollView}/{@link GridView} interaction issues.
 * </p>
 */
public class ExpandableHeightGridView extends GridView {

    boolean expanded = false;

    public ExpandableHeightGridView(Context context) {
        super(context);
    }

    public ExpandableHeightGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableHeightGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Sets expanded to the input value. I feel that when we start using this again it should be
     * noted
     * that the {@link ExpandableHeightGridView#onMeasure(int, int)} method should be called, or
     * maybe
     * android just handles that for us.
     *
     * @param expanded whether or not the gridview should be expanded.
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * @see GridView#onMeasure(int, int)
     * Overrides the default onMeasure method to set the GridView to the height of it's contents,
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // HACK! TAKE THAT ANDROID!
        if (isExpanded()) {
            // Calculate entire height by providing a very large height hint.
            // View.MEASURED_SIZE_MASK represents the largest height possible.
            int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                    MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}