package com.itikarus.miska.workspace

import com.itikarus.miska.models.product_model.ProductDetails

class ProjectModel {
    var productModel : ProductDetails? = null

    var imageUrl = ""

    var firstLoad = true
    var width: Int = 0
    var height: Int = 0
    var displayWidth: Int = 0
    var displayHeight: Int = 0
    var centerX: Float = 0.toFloat()
    var centerY: Float = 0.toFloat()
    var scaleX: Float = 0.toFloat()
    var scaleY: Float = 0.toFloat()
    var angle: Float = 0.toFloat()

    var minX: Float = 0.toFloat()
    var maxX: Float = 0.toFloat()
    var minY: Float = 0.toFloat()
    var maxY: Float = 0.toFloat()
    var cancelPointX: Float = 0.toFloat()
    var cancelPointY: Float = 0.toFloat()
    var donePointX: Float = 0.toFloat()
    var donePointY: Float = 0.toFloat()
    var flipBtnX : Float = 0.toFloat()
    var flipBtnY : Float = 0.toFloat()
    var aheadBtnX : Float = 0.toFloat()
    var aheadBtnY : Float = 0.toFloat()
    var behindBtnX : Float = 0.toFloat()
    var behindBtnY : Float = 0.toFloat()
    var isGrabAreaSelected = false
    var isLatestSelected = false
    var grabAreaX1: Float = 0.toFloat()
    var grabAreaY1: Float = 0.toFloat()
    var grabAreaX2: Float = 0.toFloat()
    var grabAreaY2: Float = 0.toFloat()

    var startMidX: Float = 0.toFloat()
    var startMidY: Float = 0.toFloat()
}