package com.example.watchly.UI;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CenterScalingLayoutManager extends LinearLayoutManager {

    private final float shrinkAmount = 0.15f; // Scale down for side items
    private final float shrinkDistance = 0.9f; // Distance to start shrinking

    public CenterScalingLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        scaleChildren();
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            scaleChildren();
        }
    }

    void scaleChildren() {
        float midpoint = getWidth() / 2.0f;
        float d0 = 0.0f;
        float d1 = shrinkDistance * midpoint;
        float s0 = 1.4f; // Scale for the center item
        float s1 = 0.8f; // Scale for side items
        float z0 = 10f;  // Elevation for the center item
        float z1 = 0f;   // Elevation for side items

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null) {
                float childMidpoint = (getDecoratedRight(child) + getDecoratedLeft(child)) / 2.0f;
                float d = Math.min(d1, Math.abs(midpoint - childMidpoint));
                float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
                float elevation = z0 + (z1 - z0) * (d - d0) / (d1 - d0);

                // Apply scale
                child.setScaleX(scale);
                child.setScaleY(scale);

                // Apply elevation (Z-ordering)
                child.setZ(elevation);
            }
        }
    }


}