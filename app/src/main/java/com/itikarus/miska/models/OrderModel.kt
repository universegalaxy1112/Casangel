package com.itikarus.miska.models

class OrderModel{

    var orderID = ""
    var orderDt = ""
    var orders = ArrayList<OrderedProductModel>()

    class OrderedProductModel{
        var itemCnt = 1
        lateinit var productModel : ProductModel
    }

}