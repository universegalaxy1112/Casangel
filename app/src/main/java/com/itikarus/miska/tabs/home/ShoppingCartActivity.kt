package com.itikarus.miska.tabs.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.itikarus.miska.CheckoutActivity
import com.itikarus.miska.LoginActivity
import com.itikarus.miska.R
import com.itikarus.miska.SignupActivity
import com.itikarus.miska.adapters.CouponsAdapter
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.extentions.convertPriceString
import com.itikarus.miska.extentions.verticalize
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.globals.LocalDB
import com.itikarus.miska.library.utils.DateTimeUtils
import com.itikarus.miska.models.api_response_model.ErrorResponse
import com.itikarus.miska.models.cart_model.CartDetails
import com.itikarus.miska.models.coupons_model.CouponDetails
import com.itikarus.miska.models.order_model.OrderDetails
import com.itikarus.miska.models.order_model.OrderProducts
import com.itikarus.miska.models.order_model.OrderShippingMethod
import com.itikarus.miska.models.user_model.UserBilling
import com.itikarus.miska.models.user_model.UserShipping
import com.itikarus.miska.network.APIClient
import kotlinx.android.synthetic.main.activity_shopping_cart.*
import kotlinx.android.synthetic.main.item_cart.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ShoppingCartActivity : BaseActivity() {

    private var cartItemsList = ArrayList<CartDetails>()
    private var couponsList = ArrayList<CouponDetails>()
    private lateinit var cartItemsAdapter: CartItemListAdapter
    private lateinit var couponsAdapter: CouponsAdapter


    private var cartSubTotal = 0.0
    private var cartTotalPrice = 0.0
    private var customerID = ""
    private var customerToken = ""
    private var customerEmailAddress = ""
    private var disableOtherCoupons = false

    var cartDiscount: Double = 0.toDouble()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart)

        rcv_productListView.isNestedScrollingEnabled = false
        rcv_couponListView.isNestedScrollingEnabled = false

        cartItemsAdapter = CartItemListAdapter()
        rcv_productListView.adapter = cartItemsAdapter
        rcv_productListView.verticalize(this)

        couponsAdapter = CouponsAdapter(this, couponsList, true)
        rcv_couponListView.adapter = couponsAdapter
        rcv_couponListView.verticalize(this)

        apply_coupon_btn.setOnClickListener {
            if (cart_coupon_code.text.toString().isNotEmpty()) {
                getCouponInfo(cart_coupon_code.text.toString())
            }
        }

        arrayOf(iv_btnDone, btnFinalPurchase).forEach {
            it.setOnClickListener {
                if (LocalDB.build(this).hasLoginAuth) {
                    proceedCheckout()
                } else {
                    val intent = Intent(this, SignupActivity::class.java)
                    startActivityForResult(intent, 1004)
                }
            }
        }

        iv_btnBack.setOnClickListener {
            finish()
        }

        tv_addMoreProducts.setOnClickListener {
            finish()
        }

        loadData()

        // Update Cart's Total
        updateCart()
    }

    private fun loadData() {
        cartItemsList = GlobalStorage.getCartItems()
        cartItemsAdapter.notifyDataSetChanged()
    }

    private fun getCouponInfo(coupon_code: String) {

        showProgressDialog("Applying coupon...")

        val params = LinkedHashMap<String, String>()
        params["code"] = coupon_code


        val call = APIClient.getInstance()
            .getCouponInfo(
                params
            )


        call.enqueue(object : Callback<List<CouponDetails>> {
            override fun onResponse(
                call: Call<List<CouponDetails>>,
                response: retrofit2.Response<List<CouponDetails>>
            ) {

                hideProgressDialog()

                // Check if the Response is successful
                if (response.isSuccessful) {
                    if (response.body()!!.isNotEmpty()) {

                        val couponDetails = response.body()!![0]

                        if (couponsList.size != 0 && couponDetails.isIndividualUse) {

                            val dialog = AlertDialog.Builder(this@ShoppingCartActivity)

                            dialog.setTitle(getString(R.string.add_coupon))
                            dialog.setMessage(getString(R.string.coupon_removes_other_coupons))

                            dialog.setPositiveButton(getString(R.string.ok)) { _, _ ->
                                if (validateCoupon(couponDetails))
                                    applyCoupon(couponDetails)
                            }

                            dialog.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                                dialog.dismiss()
                            }
                            dialog.show()

                        } else {
                            if (validateCoupon(couponDetails))
                                applyCoupon(couponDetails)
                        }
                    } else {
                        showSnackBarForCoupon(getString(R.string.invalid_coupon))
                    }

                } else {
                    val converter: Converter<ResponseBody, ErrorResponse> =
                        APIClient.retrofit.responseBodyConverter(
                            ErrorResponse::class.java,
                            arrayOfNulls<Annotation>(0)
                        )
                    var error: ErrorResponse
                    try {
                        error = converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        error = ErrorResponse()
                    }

                    Toast.makeText(
                        this@ShoppingCartActivity,
                        "Error : " + error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<CouponDetails>>, t: Throwable) {
                hideProgressDialog()
                Toast.makeText(
                    this@ShoppingCartActivity,
                    "NetworkCallFailure : $t",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    //*********** Apply given Coupon to checkout ********//

    fun applyCoupon(coupon: CouponDetails) {

        var validItemsCount = 0
        var couponDiscount = 0.0


        if (coupon.discountType.equals("fixed_product", ignoreCase = true)) {

            for (i in cartItemsList.indices) {
                if (cartItemsList[i].isProductValidForCoupon) {
                    validItemsCount += cartItemsList[i].cartProduct.customersBasketQuantity
                    couponDiscount += java.lang.Double.parseDouble(coupon.amount) * cartItemsList[i].cartProduct.customersBasketQuantity
                }
            }

        } else if (coupon.discountType.equals("fixed_cart", ignoreCase = true)) {

            couponDiscount = java.lang.Double.parseDouble(coupon.amount)

            for (i in cartItemsList.indices) {
                if (cartItemsList[i].isProductValidForCoupon) {
                    validItemsCount += cartItemsList[i].cartProduct.customersBasketQuantity
                }
            }

        } else if (coupon.discountType.equals("percent", ignoreCase = true)) {

            couponDiscount = cartSubTotal * java.lang.Double.parseDouble(coupon.amount) / 100

            for (i in cartItemsList.indices) {
                if (cartItemsList[i].isProductValidForCoupon) {
                    validItemsCount += cartItemsList[i].cartProduct.customersBasketQuantity
                }
            }

        }


        if (couponDiscount + cartDiscount >= cartSubTotal) {
            showSnackBarForCoupon(getString(R.string.coupon_cannot_be_applied))

        } else {

            val productDiscount = couponDiscount / validItemsCount

            for (i in cartItemsList.indices) {
                if (cartItemsList[i].isProductValidForCoupon) {
                    if (0 > java.lang.Double.parseDouble(cartItemsList[i].cartProduct.totalPrice) - productDiscount) {
                        cartItemsList[i].isProductValidForCoupon = false
                        validItemsCount -= cartItemsList[i].cartProduct.customersBasketQuantity
                    }
                }
            }


            for (i in cartItemsList.indices) {
                if (cartItemsList[i].isProductValidForCoupon) {
                    coupon.valid_items.add(cartItemsList[i].cartID)
                }
            }


            var coupon_applied_already = false

            if (couponsList.size != 0) {
                for (i in couponsList.indices) {
                    if (coupon.code.equals(couponsList[i].code, ignoreCase = true))
                        coupon_applied_already = true
                }
            }


            if (!disableOtherCoupons) {
                if (!coupon_applied_already) {
                    if (validItemsCount > 0) {

                        if (coupon.isIndividualUse) {
                            couponsList.clear()
                            disableOtherCoupons = true
                        }


                        coupon.valid_items_count = validItemsCount
                        coupon.discount = couponDiscount.toString()



                        couponsList.add(coupon)
                        cart_coupon_code.setText("")
                        couponsAdapter.notifyDataSetChanged()


                        updateCart()

                    } else {
                        showSnackBarForCoupon(getString(R.string.coupon_cannot_be_applied))
                    }
                } else {
                    showSnackBarForCoupon(getString(R.string.coupon_applied))
                }
            } else {
                showSnackBarForCoupon(getString(R.string.coupon_cannot_used_with_existing))
            }

        }

    }

    //*********** Update Cart Products and Cart Coupons ********//

    fun updateCart() {

        var total = 0.0
        var subtotal = 0.0
        var totalDiscount = 0.0

        // Calculate Cart's total Price
        for (i in cartItemsList.indices) {
            subtotal += java.lang.Double.parseDouble(cartItemsList[i].cartProduct.productsFinalPrice)
        }


        for (x in cartItemsList.indices) {
            cartItemsList[x].cartProduct.totalPrice =
                cartItemsList[x].cartProduct.productsFinalPrice
        }


        if (couponsList.size > 0) {
            for (i in couponsList.indices) {

                var validItemsCount = 0
                var cartHasValidItemsForCoupon = false
                for (x in cartItemsList.indices) {
                    if (couponsList[i].valid_items.contains(cartItemsList[x].cartID)) {
                        cartHasValidItemsForCoupon = true
                        validItemsCount += cartItemsList[x].cartProduct.customersBasketQuantity
                    }

                    couponsList[i].valid_items_count = validItemsCount
                }


                // Check if Coupon is Valid
                if (!validateCoupon(couponsList[i]) || !cartHasValidItemsForCoupon) {

                    for (x in couponsList.indices) {
                        if (couponsList[i].code.equals(couponsList[x].code, ignoreCase = true)) {
                            couponsList.removeAt(x)
                        }
                    }
                    couponsAdapter.notifyDataSetChanged()

                } else {
                    var couponDiscount = 0.0

                    if (couponsList[i].discountType.equals("fixed_product", ignoreCase = true)) {
                        for (x in cartItemsList.indices) {
                            if (couponsList[i].valid_items.contains(cartItemsList[x].cartID)) {
                                couponDiscount += java.lang.Double.parseDouble(couponsList[i].amount) * cartItemsList[x].cartProduct.customersBasketQuantity
                            }
                        }
                    } else if (couponsList[i].discountType.equals(
                            "fixed_cart",
                            ignoreCase = true
                        )
                    ) {
                        couponDiscount = java.lang.Double.parseDouble(couponsList[i].amount)
                    } else if (couponsList[i].discountType.equals("percent", ignoreCase = true)) {
                        couponDiscount =
                            subtotal * java.lang.Double.parseDouble(couponsList[i].amount) / 100
                    }


                    val productDiscount = couponDiscount / couponsList[i].valid_items_count

                    for (x in cartItemsList.indices) {
                        if (couponsList[i].valid_items.contains(cartItemsList[x].cartID)) {
                            val totalPrice =
                                java.lang.Double.parseDouble(cartItemsList[x].cartProduct.totalPrice) - productDiscount * cartItemsList[x].cartProduct.customersBasketQuantity
                            cartItemsList[x].cartProduct.totalPrice = totalPrice.toString()
                        }
                    }


                    couponsList[i].discount = couponDiscount.toString()
                }
            }
        }


        couponsAdapter.notifyDataSetChanged()
        cartItemsAdapter.notifyDataSetChanged()


        for (i in couponsList.indices) {
            // Calculate total Discount
            totalDiscount += java.lang.Double.parseDouble(couponsList[i].discount)
        }


        total = subtotal - totalDiscount

        cartTotalPrice = total
        cartSubTotal = subtotal
        cartDiscount = totalDiscount

        setCartTotal()

    }

    //*********** Calculate and Set the Cart's Total Price ********//

    @SuppressLint("SetTextI18n")
    fun setCartTotal() {

        cart_discount.text = "$" + this.cartDiscount.convertPriceString()
        cart_subtotal.text = "$" + cartSubTotal.convertPriceString()
        tv_totalPriceViewer.text =
            getString(R.string.total) + " : " + "$" + cartTotalPrice.convertPriceString()


        if (couponsList.size > 0) {
            cart_prices.visibility = View.VISIBLE
        } else {
            cart_prices.visibility = View.GONE
        }

    }

    inner class CartItemListAdapter : RecyclerView.Adapter<CartItemListAdapter.CustomViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val layoutInflater = LayoutInflater.from(this@ShoppingCartActivity)
            val itemView = layoutInflater.inflate(R.layout.item_cart, parent, false)
            return CustomViewHolder(itemView)
        }

        override fun getItemCount(): Int {
            return cartItemsList.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val model = cartItemsList[position]

            with(holder) {
                com.squareup.picasso.Picasso.get().load(model.cartProduct.images[0].src)
                    .placeholder(R.drawable.ic_placeholder).into(itemImgViewer)
                itemPriceViewer.text = "$" + model.cartProduct.price.toDouble().convertPriceString()
                itemCountViewer.text = model.cartProduct.customersBasketQuantity.toString()
                itemTotalPriceViewer.text =
                    "$" + model.cartProduct.productsFinalPrice.toDouble().convertPriceString()

                // Holds Product Quantity
                val number = intArrayOf(1)
                number[0] = model.cartProduct.customersBasketQuantity

                btnItemCntPlus.setOnClickListener {
                    if (model.cartProduct.stockQuantity == null || number[0] < java.lang.Long.parseLong(
                            model.cartProduct.stockQuantity
                        )
                    ) {
                        // Increase Quantity by 1
                        number[0] = number[0] + 1
                        itemCountViewer.text = "" + number[0]

                        // Calculate Product Price with selected Quantity
                        val price =
                            java.lang.Double.parseDouble(model.cartProduct.price) * number[0]

                        // Set Final Price and Quantity
                        model.cartProduct.totalPrice = "" + price
                        model.cartProduct.productsFinalPrice = "" + price
                        model.cartProduct.customersBasketQuantity = number[0]

                        notifyItemChanged(holder.adapterPosition)

                        // Calculate Cart's Total Price Again
                        updateCart()
                    }
                }

                btnItemCntMinus.setOnClickListener {
                    // Check if the Quantity is greater than the minimum Quantity
                    if (number[0] > 1) {
                        // Decrease Quantity by 1
                        number[0] = number[0] - 1
                        itemCountViewer.text = "" + number[0]

                        // Calculate Product Price with selected Quantity
                        val price =
                            java.lang.Double.parseDouble(model.cartProduct.price) * number[0]

                        // Set Final Price and Quantity
                        model.cartProduct.totalPrice = "" + price
                        model.cartProduct.productsFinalPrice = "" + price
                        model.cartProduct.customersBasketQuantity = number[0]

                        notifyItemChanged(holder.adapterPosition)


                        // Calculate Cart's Total Price Again
                        updateCart()
                    }
                }

                btnRemoveCartItem.setOnClickListener {
                    cartItemsList.remove(model)
                    notifyItemRemoved(holder.adapterPosition)
                    updateCart()
                    if (cartItemsList.isEmpty()) finish()
                }
            }
        }


        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemImgViewer = view.siv_itemViewer
            val itemPriceViewer = view.tv_itemPriceViewer
            val itemCountViewer = view.tv_itemCountViewer
            val itemTotalPriceViewer = view.tv_itemTotalPriceViewer
            val btnItemCntPlus = view.tv_itemCntPlus
            val btnItemCntMinus = view.tv_itemCntMinus
            val btnRemoveCartItem = view.iv_removeCartItem
        }
    }

    //*********** Validate given Coupon ********//

    private fun validateCoupon(coupon: CouponDetails): Boolean {

        var user_used_this_coupon_counter = 0

        var user_usage_limit_exceeds = false               // false
        var coupon_usage_limit_exceeds = false             // false
        var items_limit_exceeds_to_usage = false           // false

        var user_email_valid_for_coupon = false            // true

        var any_valid_item_in_cart = false                 // true
        var any_valid_category_item_in_cart = false        // true

        var all_sale_items_in_cart = true                  // false
        var all_excluded_items_in_cart = true              // false
        var all_excluded_category_items_in_cart = true     // false


        for (i in 0 until coupon.usedBy.size) {
            if (!"".equals(customerID, ignoreCase = true))
                if (coupon.usedBy.contains(customerID))
                    user_used_this_coupon_counter += 1
        }


        if (coupon.usageLimitPerUser != 0 && user_used_this_coupon_counter >= coupon.usageLimitPerUser) {
            user_usage_limit_exceeds = true
        }


        if (coupon.usageLimit != 0 && coupon.usageCount >= coupon.usageLimit) {
            coupon_usage_limit_exceeds = true
        }


        if (coupon.limitUsageToXItems != 0 && cartItemsList.size >= coupon.limitUsageToXItems) {
            items_limit_exceeds_to_usage = true
        }



        if (coupon.emailRestrictions.size > 0 && !"".equals(
                customerEmailAddress,
                ignoreCase = true
            )
        ) {
            if (isStringExistsInList(customerEmailAddress, coupon.emailRestrictions)) {
                user_email_valid_for_coupon = true
            }
        } else {
            user_email_valid_for_coupon = true
        }



        for (i in cartItemsList.indices) {

            var isValidProduct = true
            var isExcludedOnSale = false
            var isExcludedProduct = true
            var anyValidCategory = false
            var anyExcludedCategory = true


            val productID = cartItemsList[i].cartProduct.id
            var categoryIDs: List<String> = ArrayList()
            if (!"".equals(
                    cartItemsList[i].cartProduct.categoryIDs,
                    ignoreCase = true
                ) && cartItemsList[i].cartProduct.categoryIDs != null
            ) {
                categoryIDs = cartItemsList[i].cartProduct.categoryIDs.replace(
                    "\\s",
                    ""
                ).split(",")
            }

            val categoryIDsList = java.util.ArrayList<Int>()
            if (categoryIDs.isNotEmpty()) {
                for (j in categoryIDs.indices) {
                    categoryIDsList.add(Integer.parseInt(categoryIDs[j]))
                }
            }


            if (coupon.isExcludeSaleItems) {
                if (!cartItemsList[i].cartProduct.isOnSale) {
                    all_sale_items_in_cart = false
                } else {
                    isExcludedOnSale = true
                }
            } else {
                all_sale_items_in_cart = false
            }


            if (coupon.productIds.size > 0) {
                if (coupon.productIds.contains(productID)) {
                    any_valid_item_in_cart = true
                } else {
                    isValidProduct = false
                }
            } else {
                any_valid_item_in_cart = true
            }


            if (coupon.productCategories.size > 0 && categoryIDs.isNotEmpty()) {
                for (y in categoryIDs.indices) {
                    if (coupon.productCategories.contains(categoryIDsList[y])) {
                        anyValidCategory = true
                        any_valid_category_item_in_cart = true
                    }
                }
            } else {
                anyValidCategory = true
                any_valid_category_item_in_cart = true
            }


            if (coupon.excludedProductIds.size > 0) {
                if (!coupon.excludedProductIds.contains(productID)) {
                    isExcludedProduct = false
                    all_excluded_items_in_cart = false
                }
            } else {
                isExcludedProduct = false
                all_excluded_items_in_cart = false
            }


            if (coupon.excludedProductCategories.size > 0 && categoryIDs.isNotEmpty()) {
                for (y in categoryIDs.indices) {
                    if (!coupon.excludedProductCategories.contains(categoryIDsList[y])) {
                        anyExcludedCategory = false
                        all_excluded_category_items_in_cart = false
                    }
                }
            } else {
                anyExcludedCategory = false
                all_excluded_category_items_in_cart = false
            }



            if (!isExcludedOnSale && !isExcludedProduct && !anyExcludedCategory && isValidProduct && anyValidCategory) {
                cartItemsList[i].isProductValidForCoupon = true
            } else {
                cartItemsList.run { get(i).isProductValidForCoupon = false }
            }

        }


        if (coupon.expiryDate == null || !DateTimeUtils.checkIsDatePassed(coupon.expiryDate)) {
            if (!coupon_usage_limit_exceeds) {
                if (!user_usage_limit_exceeds) {
                    if (user_email_valid_for_coupon) {
                        if (java.lang.Double.parseDouble(coupon.minimumAmount) <= cartSubTotal) {
                            if (java.lang.Double.parseDouble(coupon.maximumAmount) == 0.0 || cartSubTotal <= java.lang.Double.parseDouble(
                                    coupon.maximumAmount
                                )
                            ) {
                                if (!items_limit_exceeds_to_usage) {
                                    if (!all_sale_items_in_cart) {
                                        if (!all_excluded_category_items_in_cart) {
                                            if (!all_excluded_items_in_cart) {
                                                if (any_valid_category_item_in_cart) {
                                                    if (any_valid_item_in_cart) {

                                                        return true

                                                    } else {
                                                        showSnackBarForCoupon(getString(R.string.coupon_is_not_for_these_products))
                                                        return false
                                                    }
                                                } else {
                                                    showSnackBarForCoupon(getString(R.string.coupon_is_not_for_these_categories))
                                                    return false
                                                }
                                            } else {
                                                showSnackBarForCoupon(getString(R.string.coupon_is_not_for_excluded_products))
                                                return false
                                            }
                                        } else {
                                            showSnackBarForCoupon(getString(R.string.coupon_is_not_for_excluded_categories))
                                            return false
                                        }
                                    } else {
                                        showSnackBarForCoupon(getString(R.string.coupon_is_not_for_sale_items))
                                        return false
                                    }
                                } else {
                                    showSnackBarForCoupon(getString(R.string.coupon_is_not_for_too_many_products))
                                    return false
                                }
                            } else {
                                showSnackBarForCoupon(getString(R.string.coupon_max_amount_is_less_than_order_total))
                                return false
                            }
                        } else {
                            showSnackBarForCoupon(getString(R.string.coupon_min_amount_is_greater_than_order_total))
                            return false
                        }
                    } else {
                        showSnackBarForCoupon(getString(R.string.coupon_is_not_for_you))
                        return false
                    }
                } else {
                    showSnackBarForCoupon(getString(R.string.coupon_used_by_you))
                    return false
                }
            } else {
                showSnackBarForCoupon(getString(R.string.coupon_used_by_all))
                return false
            }
        } else {
            cart_coupon_code.error = getString(R.string.coupon_expired)
            return false
        }

    }

    //*********** Set Order Details and Proceed to Checkout ********//

    private fun proceedCheckout() {

        // Get the customerID and customerToken and defaultAddressID from SharedPreferences
        customerID = LocalDB.build(this).userID
        customerToken = LocalDB.build(this).userCookie


        val orderDetails = OrderDetails()
        val orderProductsList = java.util.ArrayList<OrderProducts>()

        for (i in cartItemsList.indices) {
            val orderProduct = OrderProducts()

            Log.i(
                "variationID",
                "selectedVariationID = " + cartItemsList[i].cartProduct.selectedVariationID
            )
            orderProduct.id = cartItemsList[i].cartProduct.id
            orderProduct.productId = cartItemsList[i].cartProduct.id
            orderProduct.variationId = cartItemsList[i].cartProduct.selectedVariationID
            orderProduct.quantity = cartItemsList[i].cartProduct.customersBasketQuantity
            orderProduct.name = cartItemsList[i].cartProduct.name
            orderProduct.price = cartItemsList[i].cartProduct.price
            orderProduct.subtotal = cartItemsList[i].cartProduct.productsFinalPrice
            orderProduct.total = cartItemsList[i].cartProduct.totalPrice
            orderProduct.taxClass = cartItemsList[i].cartProduct.taxClass

            orderProductsList.add(orderProduct)
        }


        orderDetails.isSetPaid = false
        orderDetails.discountTotal = cartDiscount.toString()
        orderDetails.total = cartTotalPrice.toString()
        orderDetails.dateCreated = DateTimeUtils.getDateString()

        orderDetails.orderCoupons = couponsList
        orderDetails.orderProducts = orderProductsList

        orderDetails.token = customerToken
        orderDetails.customerId = customerID


        val localDB = LocalDB.build(this)

        val userBilling = UserBilling()
        userBilling.firstName = localDB.userFirstName
        userBilling.lastName = localDB.userLastName
        userBilling.address1 = localDB.address1
        userBilling.address2 = localDB.address2
        userBilling.company = localDB.company
        userBilling.city = localDB.city
        userBilling.state = localDB.state
        userBilling.country = "CO"
        userBilling.postcode = ""
        userBilling.email = localDB.userEmail
        userBilling.phone = localDB.userPhone

        val userShipping = UserShipping()
        userShipping.firstName = localDB.userFirstName
        userShipping.lastName = localDB.userLastName
        userShipping.address1 = localDB.address1
        userShipping.address2 = localDB.address2
        userShipping.company = localDB.company
        userShipping.city = localDB.city
        userShipping.state = localDB.state
        userShipping.country = "CO"
        userShipping.postcode = ""

        val shippingMethodsList = ArrayList<OrderShippingMethod>()
        val shippingMethods = OrderShippingMethod()
        shippingMethods.methodId = ""
        shippingMethods.methodTitle = ""
        shippingMethods.total = ""
        shippingMethodsList.add(shippingMethods)


        orderDetails.isSameAddress = false
        orderDetails.billing = userBilling
        orderDetails.shipping = userShipping
        orderDetails.orderShippingMethods = shippingMethodsList


        // Save the OrderDetails
        GlobalStorage.addOrderDetails(orderDetails)

        startActivity(CheckoutActivity::class.java)
    }

    //*********** Show SnackBar with given Message  ********//

    private fun showSnackBarForCoupon(msg: String) {
        val snackbar = Snackbar.make(
            rootView,
            Html.fromHtml("<font color=\"#ffffff\">$msg</font>"),
            Snackbar.LENGTH_LONG
        )
        snackbar.show()
    }

    //*********** Check if the given String exists in the given List ********//

    private fun isStringExistsInList(str: String, stringList: List<String>): Boolean {
        var isExists = false

        for (i in stringList.indices) {
            if (stringList[i].equals(str, ignoreCase = true)) {
                isExists = true
            }
        }

        return isExists
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1004 && resultCode == Activity.RESULT_OK) {
            proceedCheckout()
        }
    }
}
