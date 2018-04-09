package org.hotelbyte.app.wallet;

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

import org.hotelbyte.app.R;
import org.hotelbyte.app.wallet.WalletFragment.OnListFragmentInteractionListener;
import org.hotelbyte.app.storage.AddressNameStorage;
import org.hotelbyte.app.util.Blockies;

import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AccountBean} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class WalletRecyclerViewAdapter extends RecyclerView.Adapter<WalletRecyclerViewAdapter.MyViewHolder> {

    private final List<AccountBean> mValues;
    private final OnListFragmentInteractionListener mListener;

    private Context context;
    private int lastPosition = -1;

    public WalletRecyclerViewAdapter(List<AccountBean> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        this.context = context;

        //this.longClickListener = longClickListener;    View.OnClickListener listener, View.OnLongClickListener longClickListener, View.OnCreateContextMenuListener l
        //this.contextMenuListener = l;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public TextView walletname, walletbalance;
        public ImageView addressimage, type;
        AutofitTextView walletaddress;
        public LinearLayout container;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            walletaddress = view.findViewById(R.id.walletaddress);
            walletname = view.findViewById(R.id.from_address);
            walletbalance = view.findViewById(R.id.walletbalance);
            addressimage = view.findViewById(R.id.addressimage);
            type = view.findViewById(R.id.type);
            container = view.findViewById(R.id.container);
        }

        public void clearAnimation() {
            container.clearAnimation();
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_wallet_item, parent, false);
        //return new ViewHolder(view);

        /*itemView.setOnClickListener(listener);
        itemView.setOnLongClickListener(longClickListener);
        itemView.setOnCreateContextMenuListener(contextMenuListener);*/


        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        AccountBean accountBean = mValues.get(position);

        holder.walletaddress.setText(accountBean.getPublicKey());

        String walletname = AddressNameStorage.getInstance(context).get(accountBean.getPublicKey());
        holder.walletname.setText(walletname == null ? "New Wallet" : walletname);

        holder.addressimage.setImageBitmap(Blockies.createIcon(accountBean.getPublicKey()));
        if (accountBean.getBalance() >= 0) {
            holder.walletbalance.setText(accountBean.getBalance() + " " + context.getString(R.string.coin_alias));
        }
        holder.type.setVisibility(accountBean.getType() == AccountBean.NORMAL || accountBean.getType() == AccountBean.CONTACT ? View.INVISIBLE : View.VISIBLE);

        setAnimation(holder.container, position);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(accountBean);
                }
            }
        });
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
        return mValues.size();
    }
}
