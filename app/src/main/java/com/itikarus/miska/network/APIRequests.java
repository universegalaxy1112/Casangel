package com.itikarus.miska.network;

import com.itikarus.miska.models.category_model.CategoryDetails;
import com.itikarus.miska.models.coupons_model.CouponDetails;
import com.itikarus.miska.models.order_model.OrderDetails;
import com.itikarus.miska.models.post_model.PostCategory;
import com.itikarus.miska.models.post_model.PostDetails;
import com.itikarus.miska.models.product_model.ProductDetails;
import com.itikarus.miska.models.user_model.Nonce;
import com.itikarus.miska.models.user_model.UserData;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;


/**
 * APIRequests contains all the Network Request Methods with relevant API Endpoints
 **/

public interface APIRequests {


    //******************** Category Data ********************//

    @GET("wp-json/wc/v2/products/categories")
    Call<List<CategoryDetails>> getAllCategories(@QueryMap Map<String, String> args);


    @GET("wp-json/wc/v2/products/categories/{id}")
    Call<CategoryDetails> getSingleCategory(@Path("id") String category_id);


    @GET("wp-json/wc/v2/products")
    Call<List<ProductDetails>> getAllProducts(@QueryMap Map<String, Object> args );

    @GET("wp-json/wc/v2/products/{id}")
    Call<ProductDetails> getSingleProduct(          @Path("id") String product_id);

    @GET("wp-json/wp/v2/posts/{id}")
    Call<PostDetails> getSinglePost(                @Path("id") String post_id);

    @GET("wp-json/wp/v2/posts")
    Call<List<PostDetails>> getAllPosts(@QueryMap Map<String, Object> args );

    @GET("wp-json/wp/v2/categories")
    Call<List<PostCategory>> getPostCategories(@QueryMap Map<String, String> args );


    //******************** User Data ********************//

    @GET("api/get_nonce")
    Call<Nonce> getNonce(@QueryMap Map<String, String> args );

    @FormUrlEncoded
    @POST("api/AndroidAppUsers/android_register")
    Call<UserData> processRegistration(@Field("insecure") String insecure,
                                       @Field("first_name") String firstname,
                                       @Field("last_name") String lastname,
                                       @Field("username") String username,
                                       @Field("email") String email_address,
                                       @Field("password") String password,
                                       @Field("company") String company,
                                       @Field("address_1") String address1,
                                       //@Field("address_2") String address2,
                                       @Field("city") String city,
                                       //@Field("state") String state,
                                       @Field("phone") String phone,
                                       @Field("nonce") String nonce);

    @FormUrlEncoded
    @POST("api/AndroidAppUsers/android_generate_cookie")
    Call<UserData> processLogin(                    @Field("insecure") String insecure,
                                                    @Field("username") String customers_username,
                                                    @Field("password") String customers_password);


    //******************** Coupon Data ********************//

    @GET("wp-json/wc/v2/coupons")
    Call<List<CouponDetails>> getCouponInfo(@QueryMap Map<String, String> args );

    //******************** Order Data ********************//

    @FormUrlEncoded
    @POST("api/AndroidAppSettings/android_data_link")
    Call<String> placeOrder(                        @Field("insecure") String insecure,
                                                    @Field("order_link") String order_data);

    @GET("wp-json/wc/v2/orders")
    Call<List<OrderDetails>> getAllOrders(          @QueryMap Map<String, String> args );


    @GET("wp-json/wc/v2/orders/{id}")
    Call<OrderDetails> getSingleOrder(@Path("id") String order_id
    );

    @FormUrlEncoded
    @POST("api/AndroidAppUsers/android_forgot_password")
    Call<UserData> processForgotPassword(           @Field("insecure") String insecure,
                                                    @Field("email") String customers_email_address);
}

