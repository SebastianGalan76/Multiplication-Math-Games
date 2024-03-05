package com.coresaken.multiplication.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.data.CorrectionEquation;
import com.coresaken.multiplication.util.Utils;

import java.util.List;

public class CorrectionAdapter extends RecyclerView.Adapter<CorrectionAdapter.ViewHolder>{
    List<CorrectionEquation> dataSet;
    Context context;

    public CorrectionAdapter(List<CorrectionEquation> dataSet, Context context) {
        this.dataSet = dataSet;
        this.context = context;
    }

    @NonNull
    @Override
    public CorrectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_correction_equation, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CorrectionAdapter.ViewHolder viewHolder, int position) {
        CorrectionEquation correctionEquation = dataSet.get(position);

        viewHolder.onBindView(context, correctionEquation);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;

        TextView tv_equation, tv_points;

        public ViewHolder(View view) {
            super(view);

            tv_equation = view.findViewById(R.id.tv_equation);
            tv_points = view.findViewById(R.id.tv_points);

        }

        public void onBindView(Context context, CorrectionEquation correctionEquation){
            this.context = context;

            tv_equation.setText(Utils.convertOperatorSign(correctionEquation.equation.toString()));
            tv_points.setText(String.valueOf(correctionEquation.points));
        }
    }
}
