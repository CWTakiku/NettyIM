package com.takiku.nettyim.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.nettyim.R;

import java.util.ArrayList;
import java.util.List;

import static com.takiku.im_lib.util.Constants.MSG_STATUS_FAILED;
import static com.takiku.im_lib.util.Constants.MSG_STATUS_READ;
import static com.takiku.im_lib.util.Constants.MSG_STATUS_SEND;
import static com.takiku.im_lib.util.Constants.MSG_STATUS_SENDING;
import static com.takiku.im_lib.util.Constants.MSG_STATUS_WITHDRAW;


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
        for (int i=list.size()-1;i>=0;i--){
            AppMessage target=list.get(i);
            if (target.getHead().getMsgId().equals(appMessage.getHead().getMsgId())){
                target=appMessage;
                return;
            }
        }
        list.add(appMessage);
        notifyItemChanged(getItemCount()-1);

    }
    public void updateMessage(ReplyMessage replyMessage){
        for (int i=list.size()-1;i>=0;i--){
            AppMessage target=list.get(i);
            if (target.getHead().getMsgId().equals(replyMessage.getMsgId())){
                target.msgStatus=replyMessage.getStatusReport();
                notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof FrameReceiveHolder) {
            FrameReceiveHolder receiveHolder = (FrameReceiveHolder) holder;
            receiveHolder.contentView.setText(list.get(position).getBody());
            receiveHolder.contentView.setVisibility(View.VISIBLE);
            receiveHolder.contentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MenuItemPopWindow menuItemPopWindow = null;
                    menuItemPopWindow=  MenuItemPopWindow.builder(v.getContext(), new MenuItemPopWindow.MenuItemListenr() {
                        @Override
                        public void onItem(int flag) {
                            AppMessage appMessage=list.get(position);
                            switch (flag){
                                case MenuItemPopWindow.MENU_TYPE_READ:
                                    receiveHolder.statusView.setText("已读");
                                    if (operationMessageListener!=null){
                                        operationMessageListener.operationMessage(appMessage,flag);
                                    }

                                    break;
                            }

                        }
                    },false);
                    menuItemPopWindow.showAsDropDown(receiveHolder.contentView,0,-100);
                    return true;
                }
            });
            receiveHolder.statusView.setText("");
            switch (list.get(position).msgStatus) {
                case MSG_STATUS_WITHDRAW:
                    receiveHolder.errorView.setVisibility(View.GONE);
                    receiveHolder.statusView.setText("消息被撤回...");
                    receiveHolder.contentView.setVisibility(View.INVISIBLE);
                    break;
            }
        } else if (holder instanceof FrameSendHolder) {
            final FrameSendHolder frameSendHolder= (FrameSendHolder) holder;
            frameSendHolder.contentView.setText(list.get(position).getBody());
            frameSendHolder.contentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                 MenuItemPopWindow menuItemPopWindow=  MenuItemPopWindow.builder(v.getContext(), new MenuItemPopWindow.MenuItemListenr() {
                       @Override
                       public void onItem(int flag) {
                           AppMessage appMessage=list.get(position);
                          switch (flag){
                              case MenuItemPopWindow.MENU_TYPE_RECALL:
                                  frameSendHolder.contentView.setVisibility(View.INVISIBLE);
                                  frameSendHolder.statusView.setText("已撤回");
                                  if (operationMessageListener!=null){
                                      operationMessageListener.operationMessage(appMessage,flag);
                                  }
                                  break;
                          }

                       }
                   },true);
                 menuItemPopWindow.showAsDropDown(frameSendHolder.contentView,0,-100);
                    return true;
                }
            });
            frameSendHolder.contentView.setVisibility(View.VISIBLE);
            frameSendHolder.statusView.setText("发送中...");
            switch (list.get(position).msgStatus) {
                case MSG_STATUS_SEND:
                    frameSendHolder.errorView.setVisibility(View.GONE);
                    frameSendHolder.statusView.setText("已发送");
                    break;
                case MSG_STATUS_FAILED:
                    frameSendHolder.errorView.setVisibility(View.VISIBLE);
                    frameSendHolder.statusView.setText("发送失败");
                    break;
                case MSG_STATUS_SENDING:
                    frameSendHolder.errorView.setVisibility(View.GONE);
                    break;
                case MSG_STATUS_READ:
                    frameSendHolder.errorView.setVisibility(View.GONE);
                    frameSendHolder.statusView.setText("已读");
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
    public interface OperationMessageListener{
     void   operationMessage(AppMessage appMessage, int  flag);
    }
    private OperationMessageListener operationMessageListener;
    public void setOperationMessageListener(OperationMessageListener operationMessageListener){
        this.operationMessageListener=operationMessageListener;
    }
}
