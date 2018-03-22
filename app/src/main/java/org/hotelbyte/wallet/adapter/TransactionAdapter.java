package org.hotelbyte.wallet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.hotelbyte.wallet.R;
import org.hotelbyte.wallet.bean.TransactionDisplay;
import org.hotelbyte.wallet.bean.TransactionType;
import org.hotelbyte.wallet.util.Blockies;
import org.hotelbyte.wallet.util.ExchangeCalculator;

import java.util.List;

import me.grantland.widget.AutofitTextView;

import static org.hotelbyte.wallet.settings.Settings.DATE_FORMATTER;


public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private Context context;
    private List<TransactionDisplay> transactionList;
    private int lastPosition = -1;
    private View.OnClickListener listener;
    private View.OnCreateContextMenuListener contextMenuListener;
    private int position;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        AutofitTextView to_address;
        ImageView from_addressicon, to_addressicon;
        TextView from_address, plusminus, walletbalance, month;
        LinearLayout container;

        public MyViewHolder(View view) {
            super(view);
            from_addressicon = view.findViewById(R.id.from_addressicon);
            from_address = view.findViewById(R.id.from_address);
            to_addressicon = view.findViewById(R.id.to_addressicon);
            to_address = view.findViewById(R.id.to_address);
            plusminus = view.findViewById(R.id.plusminus);
            walletbalance = view.findViewById(R.id.walletbalance);
            month = view.findViewById(R.id.month);
            container = view.findViewById(R.id.container);
        }

        public void clearAnimation() {
            container.clearAnimation();
        }
    }


    public TransactionAdapter(List<TransactionDisplay> transactionList, Context context, View.OnClickListener listener, View.OnCreateContextMenuListener l) {
        this.transactionList = transactionList;
        this.context = context;
        this.listener = listener;
        this.contextMenuListener = l;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_w_transaction, parent, false);
        itemView.setOnClickListener(listener);
        itemView.setOnCreateContextMenuListener(contextMenuListener);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        try {
            TransactionDisplay transaction = transactionList.get(position);
            if (transaction == null) {
                return;
            }
            holder.itemView.setOnLongClickListener((v) -> {
                setPosition(position);
                return false;
            });
            if (TransactionType.INCOMING.equals(transaction.getType())) {
                holder.plusminus.setText("+");
            } else {
                holder.plusminus.setText("-");
            }
            holder.from_addressicon.setImageBitmap(Blockies.createIcon(transaction.getFromAddress()));
            holder.from_address.setText(transaction.getFromAddress());
            holder.to_addressicon.setImageBitmap(Blockies.createIcon(transaction.getToAddress()));
            holder.to_address.setText(transaction.getToAddress());
            holder.walletbalance.setText(ExchangeCalculator.getInstance().displayBalanceNicely(transaction.getAmountEther()) + " " + context.getString(R.string.coin_alias));
            holder.plusminus.setTextColor(context.getResources().getColor(transaction.getType() == TransactionType.INCOMING ? R.color.transactionIncoming : R.color.transactionOutgoing));
            holder.walletbalance.setTextColor(context.getResources().getColor(transaction.getType() == TransactionType.INCOMING ? R.color.transactionIncoming : R.color.transactionOutgoing));
            holder.container.setAlpha(1f);
            if (transaction.getConfirmations() == 0) {
                holder.month.setText("Unconfirmed");
                holder.month.setTextColor(context.getResources().getColor(R.color.unconfirmedNew));
                holder.container.setAlpha(0.75f);
            } else if (transaction.getConfirmations() > 12) {
                holder.month.setText(DATE_FORMATTER.format(transaction.getSendDate()));
                holder.month.setTextColor(context.getResources().getColor(R.color.normalBlack));
            } else {
                holder.month.setText(transaction.getConfirmations() + " / 12 Confirmations");
                holder.month.setTextColor(context.getResources().getColor(R.color.unconfirmed));
            }
            setAnimation(holder.container, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onViewRecycled(TransactionAdapter.MyViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(MyViewHolder holder) {
        holder.clearAnimation();
    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}
