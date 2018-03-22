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
import org.hotelbyte.wallet.bean.WalletDisplay;
import org.hotelbyte.wallet.storage.AddressNameStorage;
import org.hotelbyte.wallet.util.Blockies;

import java.util.List;

import me.grantland.widget.AutofitTextView;


public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.MyViewHolder> {

    private Context context;
    private List<WalletDisplay> boxlist;
    private int lastPosition = -1;
    private View.OnClickListener listener;
    private View.OnLongClickListener longClickListener;
    private View.OnCreateContextMenuListener contextMenuListener;
    private int position;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView walletname, walletbalance;
        public ImageView addressimage, type;
        AutofitTextView walletaddress;
        public LinearLayout container;

        public MyViewHolder(View view) {
            super(view);
            walletaddress = (AutofitTextView) view.findViewById(R.id.walletaddress);
            walletname = (TextView) view.findViewById(R.id.from_address);
            walletbalance = (TextView) view.findViewById(R.id.walletbalance);
            addressimage = (ImageView) view.findViewById(R.id.addressimage);
            type = (ImageView) view.findViewById(R.id.type);
            container = (LinearLayout) view.findViewById(R.id.container);
        }

        public void clearAnimation() {
            container.clearAnimation();
        }
    }


    public WalletAdapter(List<WalletDisplay> boxlist, Context context, View.OnClickListener listener, View.OnLongClickListener longClickListener, View.OnCreateContextMenuListener l) {
        this.boxlist = boxlist;
        this.context = context;
        this.listener = listener;
        this.longClickListener = longClickListener;
        this.contextMenuListener = l;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_w_address, parent, false);

        itemView.setOnClickListener(listener);
        itemView.setOnLongClickListener(longClickListener);
        itemView.setOnCreateContextMenuListener(contextMenuListener);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        WalletDisplay box = boxlist.get(position);

        holder.walletaddress.setText(box.getPublicKey());
        String walletname = AddressNameStorage.getInstance(context).get(box.getPublicKey());
        holder.walletname.setText(walletname == null ? "New Wallet" : walletname);
        holder.addressimage.setImageBitmap(Blockies.createIcon(box.getPublicKey()));
        if (box.getBalance() >= 0) {
            holder.walletbalance.setText(box.getBalance() + " " + context.getString(R.string.coin_alias));
        }
        holder.type.setVisibility(box.getType() == WalletDisplay.NORMAL || box.getType() == WalletDisplay.CONTACT ? View.INVISIBLE : View.VISIBLE);

        setAnimation(holder.container, position);
    }


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onViewRecycled(WalletAdapter.MyViewHolder holder) {
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
        return boxlist.size();
    }
}
