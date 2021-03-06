package com.example.quizzer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Viewholder> {

    private List<CategoryModel> categoryModelList;

    public CategoryAdapter(List<CategoryModel> categoryModelList) {
        this.categoryModelList = categoryModelList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {


        holder.setData(categoryModelList.get(position).getUrl(), categoryModelList.get(position).getName(),categoryModelList.get(position).getSets());
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        private CircleImageView imageview;
        private TextView textView;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            imageview = itemView.findViewById(R.id.image_view);
            textView = itemView.findViewById(R.id.title);

        }

        private void setData(String url, final String title, final int sets) {

            Glide.with(itemView.getContext()).load(url).into(imageview);
            this.textView.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent setsIntent=new Intent(itemView.getContext(),SetsActivity.class);
                    setsIntent.putExtra("title",title);
                    setsIntent.putExtra("sets",sets);

                    itemView.getContext().startActivity(setsIntent);
                }
            });
        }
    }
}
