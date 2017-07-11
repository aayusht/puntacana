package com.aayush.drunk;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import static com.aayush.drunk.Body.*;

/**
 * Created by account on 7/10/17.
 */

public class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.CustomViewHolder> {

    Date beginTime, endTime;
    int beginIndex, endIndex;

    public DrinkAdapter(Date beginTime, Date endTime) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        setIndices();
    }

    public void setIndices() {
        this.beginIndex = getDrinkIndex(endTime);
        this.endIndex = getDrinkIndex(beginTime);
        endIndex = endIndex == -1 ? drinks.size() - 1 : endIndex - 1;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drink_card, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, int position) {
        Drink drink = drinks.get(beginIndex + position);
        holder.drinkInfo.setText(drink.toString());
        holder.drinkTime.setText(DateFormat.getTimeInstance().format(drink.timestamp));
    }

    @Override
    public int getItemCount() {
        return beginIndex == -1 ? 0 : endIndex - beginIndex + 1;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder { // Custom View Holder Class for the feed of socials
        TextView drinkInfo;
        TextView drinkTime;

        public CustomViewHolder(View view) {
            super(view);
            this.drinkInfo = (TextView) view.findViewById(R.id.drinkInfo);
            this.drinkTime = (TextView) view.findViewById(R.id.drinkTime);
        }
    }
}

