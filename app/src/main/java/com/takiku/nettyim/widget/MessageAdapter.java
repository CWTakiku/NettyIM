package com.takiku.nettyim.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.takiku.im_lib.entity.AppMessage;
import com.takiku.nettyim.R;

import java.util.ArrayList;
import java.util.List;

import static com.takiku.nettyim.Constants.MSG_STATUS_FAILED;
import static com.takiku.nettyim.Constants.MSG_STATUS_READ;
import static com.takiku.nettyim.Constants.MSG_STATUS_SEND;
import static com.takiku.nettyim.Constants.MSG_STATUS_SENDING;


public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String myselfId;
    List<AppMessage> list=new ArrayList<>();
    public static final int SEND_TYPE=1;
    public static final int RECEIVE_TYPE=2;


    public MessageAdapter(String userId){
        this.myselfId=userId;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==SEND_TYPE){
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_send_layout,parent,false);
         return new FrameSendHolder(view);
        }else if (viewType==RECEIVE_TYPE){
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.message_receive_layout,parent,false);
            return new FrameReceiveHolder(view);
        }
        return null;
    }
    public void setMessageList(List<AppMessage> messageList){
        this.list=messageList;
    }
    public void addMessage(AppMessage appMessage){
        for (int i=0;i<list.size();i++){
            AppMessage target=list.get(i);
            if (target.getHead().getMsgId().equals(appMessage.getHead().getMsgId())){
                target=appMessage;
                return;
            }
        }
        list.add(appMessage);
        notifyItemChanged(getItemCount()-1);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FrameReceiveHolder) {
            FrameReceiveHolder receiveHolder = (FrameReceiveHolder) holder;
            receiveHolder.contentView.setText(list.get(position).getBody());
            switch (list.get(position).msgStatus) {
                case MSG_STATUS_SEND:
                    receiveHolder.errorView.setVisibility(View.GONE);
                    receiveHolder.statusView.setText("已发送");
                    break;
                case MSG_STATUS_FAILED:
                    receiveHolder.errorView.setVisibility(View.VISIBLE);
                    receiveHolder.statusView.setText("发送失败");

                case MSG_STATUS_READ:
                    receiveHolder.errorView.setVisibility(View.GONE);
                    receiveHolder.statusView.setText("已读");
                    break;
                case MSG_STATUS_SENDING:
                    receiveHolder.errorView.setVisibility(View.GONE);
                    receiveHolder.statusView.setText("发送中...");
                    break;
            }
        } else if (holder instanceof FrameSendHolder) {
            FrameSendHolder frameSendHolder= (FrameSendHolder) holder;
            frameSendHolder.contentView.setText(list.get(position).getBody());
            switch (list.get(position).msgStatus) {
                case MSG_STATUS_SEND:
                    frameSendHolder.errorView.setVisibility(View.GONE);
                    frameSendHolder.statusView.setText("已发送");
                    break;
                case MSG_STATUS_FAILED:
                    frameSendHolder.errorView.setVisibility(View.VISIBLE);
                    frameSendHolder.statusView.setText("发送失败");

                case MSG_STATUS_READ:
                    frameSendHolder.errorView.setVisibility(View.GONE);
                    frameSendHolder.statusView.setText("已读");
                    break;
                case MSG_STATUS_SENDING:
                    frameSendHolder.errorView.setVisibility(View.GONE);
                    frameSendHolder.statusView.setText("发送中...");
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getHead().getFromId().equals(myselfId)){
            return SEND_TYPE;
        }else {
            return RECEIVE_TYPE;
        }
    }

    /**
     * 更新单条消息状态
     * @param appMessage
     */
    public void onItemChange(AppMessage appMessage){
            for (int i=0;i<list.size();i++){
                AppMessage target=list.get(i);
                if (target.getHead().getMsgId().equals(appMessage.getHead().getMsgId())){
                       target=appMessage;
                       notifyItemChanged(i);
                       break;
                }
            }
    }



    public class FrameSendHolder extends RecyclerView.ViewHolder {
         private ImageView errorView;
         private TextView contentView;
         private TextView statusView;

        public FrameSendHolder(@NonNull View itemView) {
            super(itemView);
            errorView=itemView.findViewById(R.id.chat_item_fail);
            contentView=itemView.findViewById(R.id.chat_item_content_text);
            statusView=itemView.findViewById(R.id.chat_item_content_status);
        }
    }
    public class FrameReceiveHolder extends RecyclerView.ViewHolder {

        private ImageView errorView;
        private TextView contentView;
        private TextView statusView;

        public FrameReceiveHolder(@NonNull View itemView) {
            super(itemView);
            errorView=itemView.findViewById(R.id.chat_item_fail);
            contentView=itemView.findViewById(R.id.chat_item_content_text);
            statusView=itemView.findViewById(R.id.chat_item_content_status);
        }
    }
}
