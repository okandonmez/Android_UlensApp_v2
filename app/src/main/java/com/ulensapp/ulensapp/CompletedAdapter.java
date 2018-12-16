package com.ulensapp.ulensapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ulensapp.ulensapp.DetailActivities.CompletedDetails;

import java.util.Collections;
import java.util.List;

public class CompletedAdapter extends RecyclerView.Adapter<CompletedAdapter.MyViewHolder> {
    private LayoutInflater layoutInflater;
    List<Information> data = Collections.emptyList();

    public CompletedAdapter (Context context, List<Information> data){
        layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.custom_row_pending, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int i) {
        Information current = data.get(i);
        viewHolder.txtTitle.setText(current.title);
        viewHolder.imgIcon.setImageResource(current.iconId);
        viewHolder.txtAmount.setText(current.amount);
        viewHolder.txtMerchantName.setText(current.merchantName);
        viewHolder.txtDateRow.setText(current.expenseDate);
        viewHolder.txtExpenseId.setText(current.expenseID);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        Context context;
        TextView txtTitle;
        TextView txtMerchantName;
        TextView txtAmount;
        TextView txtDateRow;
        TextView txtExpenseId;
        ImageView imgIcon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            context = itemView.getContext();
            txtTitle = itemView.findViewById(R.id.txtPendingExpense);
            txtMerchantName = itemView.findViewById(R.id.txtMerchantName);
            txtDateRow = itemView.findViewById(R.id.txtDateRow);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtExpenseId = itemView.findViewById(R.id.expenseId);
            imgIcon = itemView.findViewById(R.id.iconExpense);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context,CompletedDetails.class);
                    intent.putExtra("expenseId", txtExpenseId.getText().toString());
                    context.startActivity(intent);
                }
            });


        }
    }
}
