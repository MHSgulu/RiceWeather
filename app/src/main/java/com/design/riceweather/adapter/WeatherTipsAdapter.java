package com.design.riceweather.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.design.riceweather.R;
import com.design.riceweather.databinding.ItemWeatherTipsBinding;
import com.design.riceweather.entity.CityWeather;

import java.util.List;

public class WeatherTipsAdapter extends RecyclerView.Adapter<WeatherTipsAdapter.ViewHolder> {

    private final List<CityWeather.ResultBeanX.ResultBean.IndexBean> localDataList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemWeatherTipsBinding binding;

        ViewHolder(View view) {
            super(view);
            binding = ItemWeatherTipsBinding.bind(view);
        }
    }

    public WeatherTipsAdapter(List<CityWeather.ResultBeanX.ResultBean.IndexBean> dataList) {
        localDataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather_tips, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        CityWeather.ResultBeanX.ResultBean.IndexBean data;
        if (position == 5){
            data = localDataList.get(position + 1);
        }else{
            data = localDataList.get(position);
        }
        holder.binding.tvTipsName.setText(data.getIname());
        holder.binding.tvTipsContent.setText(data.getIvalue());
        switch(position){
            case 0:
                holder.binding.ivTips.setBackgroundResource(R.drawable.icon_tips1);
                break;
            case 1:
                holder.binding.ivTips.setBackgroundResource(R.drawable.icon_tips2);
                break;
            case 2:
                holder.binding.ivTips.setBackgroundResource(R.drawable.icon_tips3);
                break;
            case 3:
                holder.binding.ivTips.setBackgroundResource(R.drawable.icon_tips4);
                break;
            case 4:
                holder.binding.ivTips.setBackgroundResource(R.drawable.icon_tips5);
                break;
            case 5:
                holder.binding.ivTips.setBackgroundResource(R.drawable.icon_tips6);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }


}
