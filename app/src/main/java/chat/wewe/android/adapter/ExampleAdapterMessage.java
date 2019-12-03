package chat.wewe.android.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import chat.wewe.android.R;
import chat.wewe.android.fragment.chatroom.ExampleItem;
import chat.wewe.android.fragment.chatroom.ExampleItemM;

public class ExampleAdapterMessage extends RecyclerView.Adapter<ExampleAdapterMessage.ExampleViewHolder> {
    private ArrayList<ExampleItemM> mExampleList;

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView1;
        public TextView mTextView2;

        public ExampleViewHolder(View itemView) {
            super(itemView);
            mTextView1 = itemView.findViewById(R.id.text);
            mTextView2 = itemView.findViewById(R.id.text2);
        }
    }

    public ExampleAdapterMessage(ArrayList<ExampleItemM> exampleList) {
        mExampleList = exampleList;
    }

    @Override
    public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_item2, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(ExampleViewHolder holder, int position) {
        ExampleItemM currentItem = mExampleList.get(position);

        holder.mTextView1.setText(currentItem.getText1());
        holder.mTextView2.setText(currentItem.getText2());
    }

    @Override
    public int getItemCount() {
        return mExampleList.size();
    }



}