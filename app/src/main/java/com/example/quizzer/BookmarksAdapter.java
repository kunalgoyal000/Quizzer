package com.example.quizzer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.Viewholder> {

    private List<QuestionModel> list;

    public BookmarksAdapter(List<QuestionModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public BookmarksAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarksAdapter.Viewholder holder, int position) {
        holder.setData(list.get(position).getQuestion(),list.get(position).getCorrectANS(),position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        private TextView question,answer;
        private ImageButton deleteBtn;
        public Viewholder(@NonNull View itemView) {
            super(itemView);

            question=itemView.findViewById(R.id.question);
            answer=itemView.findViewById(R.id.answer);
            deleteBtn= itemView.findViewById(R.id.delete_btn);

        }

        private void setData(String question, String answer, final int position){

            this.question.setText("Question: "+question);
            this.answer.setText("Answer: "+answer);

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    list.remove(position);

                    notifyItemRemoved(position);
                    Toast.makeText(itemView.getContext(), "Removed from bookmarks", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
