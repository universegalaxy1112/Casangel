package com.itikarus.miska.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itikarus.miska.R;
import com.itikarus.miska.models.product_model.ProductAttributes;
import com.itikarus.miska.models.product_model.ProductDetails;
import com.itikarus.miska.models.product_model.ProductMetaData;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * OrderedProductsListAdapter is the adapter class of RecyclerView holding List of Ordered Products in Order_Details
 **/

public class OrderedProductsListAdapter extends RecyclerView.Adapter<OrderedProductsListAdapter.MyViewHolder> {

    private Context context;
    private List<ProductDetails> orderProductsList;

    private ProductAdditionalValuesAdapter metaDataAdapter;


    public OrderedProductsListAdapter(Context context, List<ProductDetails> orderProductsList) {
        this.context = context;
        this.orderProductsList = orderProductsList;
    }


    //********** Called to Inflate a Layout from XML and then return the Holder *********//

    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // Inflate the custom layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_items, parent, false);

        return new MyViewHolder(itemView);
    }


    //********** Called by RecyclerView to display the Data at the specified Position *********//

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // Get the data model based on Position
        final ProductDetails product = orderProductsList.get(position);

        Picasso.get().load(product.getImage())
                .fit()
                .into(holder.checkout_item_cover);


        holder.checkout_item_category.setVisibility(View.GONE);

        holder.checkout_item_title.setText(product.getName());
        holder.checkout_item_quantity.setText(String.valueOf(product.getCustomersBasketQuantity()));
        holder.checkout_item_price.setText("$" + new DecimalFormat("#0.00").format(Double.parseDouble(product.getPrice())));
        holder.checkout_item_price_final.setText("$" + new DecimalFormat("#0.00").format(Double.parseDouble(product.getProductsFinalPrice())));
        holder.checkout_item_price_total.setText("$" + new DecimalFormat("#0.00").format(Double.parseDouble(product.getTotalPrice())));


        List<ProductMetaData> productMetaDataList = new ArrayList<>();
        List<ProductAttributes> productAttributesList = new ArrayList<>();

        productAttributesList = product.getAttributes();

        for (int i = 0; i < productAttributesList.size(); i++) {
            ProductMetaData metaData = new ProductMetaData();
            metaData.setId(productAttributesList.get(i).getId());
            metaData.setKey(productAttributesList.get(i).getName());
            metaData.setValue(productAttributesList.get(i).getOption());

            productMetaDataList.add(metaData);
        }

        // Initialize the ProductAdditionalValuesAdapter for RecyclerView
        metaDataAdapter = new ProductAdditionalValuesAdapter(context, productMetaDataList);

        holder.attributes_recycler.setAdapter(metaDataAdapter);
        holder.attributes_recycler.setLayoutManager(new LinearLayoutManager(context));

        metaDataAdapter.notifyDataSetChanged();


    }


    //********** Returns the total number of items in the data set *********//

    @Override
    public int getItemCount() {
        return orderProductsList.size();
    }


    /********** Custom ViewHolder provides a direct reference to each of the Views within a Data_Item *********/

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout checkout_item;
        private ImageView checkout_item_cover;
        private RecyclerView attributes_recycler;
        private TextView checkout_item_title, checkout_item_quantity, checkout_item_price, checkout_item_price_final, checkout_item_price_total, checkout_item_category;


        public MyViewHolder(final View itemView) {
            super(itemView);

            checkout_item = (LinearLayout) itemView.findViewById(R.id.checkout_item);
            checkout_item_category = (TextView) itemView.findViewById(R.id.checkout_item_category);
            checkout_item_cover = (ImageView) itemView.findViewById(R.id.checkout_item_cover);
            checkout_item_title = (TextView) itemView.findViewById(R.id.checkout_item_title);
            checkout_item_quantity = (TextView) itemView.findViewById(R.id.checkout_item_quantity);
            checkout_item_price = (TextView) itemView.findViewById(R.id.checkout_item_price);
            checkout_item_price_final = (TextView) itemView.findViewById(R.id.checkout_item_price_final);
            checkout_item_price_total = (TextView) itemView.findViewById(R.id.checkout_item_price_total);

            attributes_recycler = (RecyclerView) itemView.findViewById(R.id.order_item_attributes_recycler);
        }

    }


}

