package com.galiazat.diplomtest4opencvimplement.screen.videoSource.list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.galiazat.diplomtest4opencvimplement.R;
import com.galiazat.diplomtest4opencvimplement.entites.SupportedFormat;
import com.galiazat.diplomtest4opencvimplement.screen.videoSource.VideoSourceActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Azat Galiullin.
 */

public class SupportedFormatsAdapter extends RecyclerView.Adapter<SupportesFormatsHolder> {

    private List<SupportedFormat> supportedFormats = new ArrayList<>();
    private WeakReference<VideoSourceActivity> activityWeakReference;

    public SupportedFormatsAdapter(VideoSourceActivity activityWeakReference) {
        this.activityWeakReference = new WeakReference<>(activityWeakReference);
    }

    public void setSupportedFormats(List<SupportedFormat> supportedFormats) {
        this.supportedFormats = supportedFormats;
        notifyDataSetChanged();
    }

    @Override
    public SupportesFormatsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.holder_supported_preview_format, parent, false);
        return new SupportesFormatsHolder(view);
    }

    @Override
    public void onBindViewHolder(SupportesFormatsHolder holder, int position) {
        holder.bind(supportedFormats.get(position), activityWeakReference, position);
    }

    @Override
    public int getItemCount() {
        return supportedFormats.size();
    }

    public void selectedChanged(int selected, int newSelected) {
        notifyItemChanged(selected);
        notifyItemChanged(newSelected);
    }
}
