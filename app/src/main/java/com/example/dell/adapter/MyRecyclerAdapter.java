package com.example.dell.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dell.activity.DetailsActivity;
import com.example.dell.activity.R;
import com.example.dell.vo.ShowVO;

import java.util.List;

/**
 * Created by Dell on 2017/2/9.
 */

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private List<ShowVO> mList;
    private Context mContext;

    public MyRecyclerAdapter(List<ShowVO> mList) {
        this.mList = mList;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_show, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        //点击后跳转
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                ShowVO vo = mList.get(position);
                //点击跳转
                Intent intent = new Intent(mContext, DetailsActivity.class);
                intent.putExtra(DetailsActivity.DE_ADDRESS, vo.getmAddress());
                intent.putExtra(DetailsActivity.DE_IP, vo.getmIp());
                intent.putExtra(DetailsActivity.DE_POST, vo.getmPost());
                intent.putExtra(DetailsActivity.DE_PASSWORD, vo.getmPassword());
                intent.putExtra(DetailsActivity.DE_ENCRYPTION, vo.getmEncryption());
                intent.putExtra(DetailsActivity.DE_NAME, vo.getmName());
                intent.putExtra(DetailsActivity.DE_IMG, vo.getmImg());
                intent.putExtra(DetailsActivity.DE_COUNTRYIMAGE, vo.getmCountry());
                mContext.startActivity(intent);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //文本
        holder.textView.setText(mList.get(position).getmAddress());
        //国旗
        Glide.with(mContext).load(mList.get(position).getmCountry()).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView textView;
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.item_show_cardView);
            textView = (TextView) itemView.findViewById(R.id.item_show_name);
            imageView = (ImageView) itemView.findViewById(R.id.item_show_img);
        }
    }
}
