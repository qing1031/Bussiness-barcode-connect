package com.foodlogiq.distributormobile.viewAdapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.foodlogiq.distributormobile.R;
import com.foodlogiq.distributormobile.entityClasses.SparseProduct;
import com.foodlogiq.flqassets.FLQActivity;

import java.util.ArrayList;

/**
 * Handles showing the contents of a Sparse product. A delete icon is shown, to allow removal of
 * the SparseProduct from the array. This can be hidden, if desired, by adding the parent view using
 * {@link #addSlideToHiddenButtons(Integer)}
 */
public class SparseProductListAdapter extends ArrayAdapter<SparseProduct> {
    private final FLQActivity activity;
    private final ListView listView;
    private final ArrayList<SparseProduct> values;
    private final ArrayList<Integer> hiddenButtonViews;
    private View wrapperView;
    private ArrayList<View> emptyViews = new ArrayList<>();
    private ArrayList<View> nonEmptyViews = new ArrayList<>();

    public SparseProductListAdapter(
            FLQActivity activity,
            ArrayList<SparseProduct> values,
            View wrapperView,
            ListView listView
    ) {
        super(activity, R.layout.list_item_sparse_product, values);
        this.activity = activity;
        this.listView = listView;
        this.values = values;
        this.hiddenButtonViews = new ArrayList<>();
        this.listView.setAdapter(this);
        this.wrapperView = wrapperView;
        this.registerDataSetObserver(getDataSetObserver());

    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        SparseProduct sparseProduct = values.get(position);

        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.list_item_sparse_product, parent, false);

        if (position % 2 != 0) {
            rowView.setBackground(activity.getResources().getDrawable(R.color.lt_gray));
        }

        //Gather values
        String gtin = sparseProduct.getGlobalLocationTradeItemNumber();
        String description = sparseProduct.getDescription();
        String name = sparseProduct.getName();
        String lot = !sparseProduct.getLot().isEmpty() ? sparseProduct.getLot() : "None";
        int quantity = sparseProduct.getQuantityAmount();


        //Gather views
        TextView nameView = (TextView) rowView.findViewById(R.id.sparse_product_name);
        TextView gtinView = (TextView) rowView.findViewById(R.id.sparse_product_gtin);
        TextView descriptionView = (TextView) rowView.findViewById(R.id.sparse_product_description);
        TextView lotView = (TextView) rowView.findViewById(R.id.sparse_product_lot);
        TextView quantityView = (TextView) rowView.findViewById(R.id.sparse_product_quantity);
        TextView useByDateView = (TextView) rowView.findViewById(R.id.sparse_product_use_by_date);
        TextView packDateView = (TextView) rowView.findViewById(R.id.sparse_product_pack_date);

        //Update views
        if (!name.isEmpty()) {
            nameView.setText(name);
            gtinView.setText(gtin);
            gtinView.setVisibility(View.VISIBLE);

            if (!description.isEmpty()) {
                descriptionView.setText(description);
            } else {
                descriptionView.setVisibility(View.GONE);
            }

        } else {
            nameView.setText(gtin);
            gtinView.setVisibility(View.GONE);
            descriptionView.setVisibility(View.GONE);
        }
        lotView.setText(lot);
        quantityView.setText(String.valueOf(quantity));

        if (sparseProduct.getUseByDate() != null) {
            useByDateView.setText(sparseProduct
                    .getUseByDateAsSimpleString());
            rowView.findViewById(R.id.sparse_product_use_by_date_wrapper).setVisibility(View
                    .VISIBLE);
        } else {
            rowView.findViewById(R.id.sparse_product_use_by_date_wrapper).setVisibility(View.GONE);
        }

        if (sparseProduct.getPackDate() != null) {
            packDateView.setText(sparseProduct
                    .getPackDateAsSimpleString());
            rowView.findViewById(R.id.sparse_product_pack_date_wrapper).setVisibility(View.VISIBLE);
        } else {
            rowView.findViewById(R.id.sparse_product_pack_date_wrapper).setVisibility(View.GONE);
        }

        if (!getHiddenButtonViews().contains(parent.getId())) {
            initAnimations(this, rowView, position);
        }
        rowView.setId(position);
        return rowView;
    }

    public ArrayList<Integer> getHiddenButtonViews() {
        return hiddenButtonViews;
    }

    public void addDependentView(View view, boolean onEmpty) {
        if (onEmpty) {
            this.emptyViews.add(view);
        } else {
            this.nonEmptyViews.add(view);
        }
    }

    public void addSlideToHiddenButtons(Integer slideIndex) {
        this.hiddenButtonViews.add(slideIndex);
    }


    public int getTotalQuantities() {
        int qty = 0;
        for (SparseProduct p : this.values) {
            qty += p.getQuantityAmount();
        }
        return qty;
    }

    private void initAnimations(SparseProductListAdapter spla, View rowView, int position) {
        final GestureDetector gestureDetector = new GestureDetector(
                activity.getApplicationContext(),
                new MyGestureDetector(spla, rowView, position)
        );
        rowView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return !gestureDetector.onTouchEvent(event);
            }
        });
    }

    public DataSetObserver getDataSetObserver() {
        return new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (SparseProductListAdapter.this.isEmpty()) {
                    getWrapperView().findViewById(R.id.scanned_items_list_view).setVisibility(View
                            .GONE);
                    getWrapperView().findViewById(R.id.scan_list_header).setVisibility(View
                            .GONE);
                    getWrapperView().findViewById(R.id.empty_scan_layout).setVisibility(View
                            .VISIBLE);
                    for (View v : getEmptyViews()) {
                        v.setVisibility(View.VISIBLE);
                    }
                    for (View v : getNonEmptyViews()) {
                        v.setVisibility(View.GONE);
                    }
                    View qtyView = wrapperView.findViewById(R.id.total_qty);
                    if (qtyView != null) {
                        ((TextView) qtyView).setText("0");
                    }
                } else {
                    getWrapperView().findViewById(R.id.scanned_items_list_view).setVisibility(View
                            .VISIBLE);
                    getWrapperView().findViewById(R.id.scan_list_header).setVisibility(View
                            .VISIBLE);
                    getWrapperView().findViewById(R.id.empty_scan_layout).setVisibility(View.GONE);
                    for (View v : getEmptyViews()) {
                        v.setVisibility(View.GONE);
                    }
                    for (View v : getNonEmptyViews()) {
                        v.setVisibility(View.VISIBLE);
                    }
                    View qtyView = wrapperView.findViewById(R.id.total_qty);
                    if (qtyView != null) {
                        ((TextView) qtyView).setText(String.valueOf(getTotalQuantities()));
                    }
                }
            }
        };
    }

    public View getWrapperView() {
        return wrapperView;
    }

    public ArrayList<View> getEmptyViews() {
        return emptyViews;
    }

    public ArrayList<View> getNonEmptyViews() {
        return nonEmptyViews;
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
        private final View view;
        private final SparseProductListAdapter spla;
        private final int position;

        public MyGestureDetector(SparseProductListAdapter spla, View view, int position) {
            this.view = view;
            this.spla = spla;
            this.position = position;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                spla.remove(values.get(position));
                spla.notifyDataSetChanged();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }


}