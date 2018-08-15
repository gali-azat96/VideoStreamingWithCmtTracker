package com.galiazat.videoStreamingCmt.screen.videoSource.list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.galiazat.videoStreamingCmt.R;
import com.galiazat.videoStreamingCmt.entites.SupportedFormat;
import com.galiazat.videoStreamingCmt.screen.videoSource.VideoSourceActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Azat Galiullin.
 */

public class SupportedFormatsAdapter extends RecyclerView.Adapter<SupportedFormatsHolder> {

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
    public SupportedFormatsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.holder_supported_preview_format, parent, false);
        return new SupportedFormatsHolder(view);
    }

    @Override
    public void onBindViewHolder(SupportedFormatsHolder holder, int position) {
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
