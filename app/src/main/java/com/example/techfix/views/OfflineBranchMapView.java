package com.example.techfix.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.example.techfix.R;
import com.example.techfix.models.Branch;
import com.example.techfix.utils.BranchLocationHelper;
import com.example.techfix.utils.BranchMapLauncher;

import java.util.List;

public class OfflineBranchMapView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Branch> branches = BranchLocationHelper.branches();

    public OfflineBranchMapView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        setContentDescription("Branch map. Tap a marker area to open directions.");
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(getContext().getColor(R.color.tech_map_surface));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setColor(getContext().getColor(R.color.tech_map_line));
        paint.setStrokeWidth(2);
        for (int x = 0; x < getWidth(); x += 45) {
            canvas.drawLine(x, 0, x, getHeight(), paint);
        }
        for (int y = 0; y < getHeight(); y += 35) {
            canvas.drawLine(0, y, getWidth(), y, paint);
        }
        for (int i = 0; i < branches.size(); i++) {
            float x = getWidth() * (i == 0 ? .68f : .32f);
            float y = getHeight() * (i == 0 ? .28f : .72f);
            paint.setColor(getContext().getColor(R.color.tech_accent));
            canvas.drawCircle(x, y, 9, paint);
            paint.setColor(getContext().getColor(R.color.tech_text_primary));
            paint.setTextSize(12);
            canvas.drawText(branches.get(i).getLocationName().replace(" Branch", ""),
                    x + 12, y + 4, paint);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
            int index = event.getX() > getWidth() / 2f && event.getY() < getHeight() / 2f
                    ? 0 : 1;
            BranchMapLauncher.open(getContext(), branches.get(index));
        }
        return true;
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }
}
