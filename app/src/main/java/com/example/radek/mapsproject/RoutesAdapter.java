package com.example.radek.mapsproject;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.MyViewHolder> {

    private List<Route> routesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView route;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            route = itemView.findViewById(R.id.route);
        }
    }

    RoutesAdapter(List<Route> routesList) {
        this.routesList = routesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View item  = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.route_list_row, viewGroup, false);

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        Route route = routesList.get(i);
        myViewHolder.route.setText(route.getRoute());

    }

    @Override
    public int getItemCount() {
        return routesList.size();
    }


}
