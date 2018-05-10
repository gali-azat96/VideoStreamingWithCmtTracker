package com.galiazat.diplomtest4opencvimplement.screen.videoSource.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.galiazat.diplomtest4opencvimplement.R;
import com.galiazat.diplomtest4opencvimplement.entites.SupportedFormat;
import com.galiazat.diplomtest4opencvimplement.screen.videoSource.VideoSourceActivity;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Azat Galiullin.
 */

public class SupportesFormatsHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.supported_preview_title)
    TextView title;
    @BindView(R.id.supported_preview_selected_ic)
    ImageView selectedIcon;

    public SupportesFormatsHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(SupportedFormat supportedFormat, WeakReference<VideoSourceActivity> activityWeakReference,
                     int pos) {
        title.setText(supportedFormat.getSize().width + "x" + supportedFormat.getSize().height);
        selectedIcon.setVisibility(supportedFormat.isSelected() ? View.VISIBLE : View.GONE);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!supportedFormat.isSelected()){
                    VideoSourceActivity activity = activityWeakReference.get();
                    if (activity != null){
                        activity.onSupportedFormatClicked(pos);
                    }
                }
            }
        });
    }
}
