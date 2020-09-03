package com.itikarus.miska.base

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import com.itikarus.miska.R
import com.itikarus.miska.globals.GlobalData
import com.itikarus.miska.library.utils.ImageUtils
import com.otaliastudios.cameraview.CameraListener
import kotlinx.android.synthetic.main.activity_camera.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import com.itikarus.miska.globals.KeyUtils.INTENT_KEY_IMG_PATH
import com.itikarus.miska.globals.KeyUtils.REQUEST_CODE_SELECT_FILE
import com.itikarus.miska.workspace.SaveModule
import java.io.File


class CameraActivity : BaseActivity() {

    private var imagePath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        civ_galleryView.setImageBitmap(getLatestImage())

        cv_cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray?) {
                var bitmap = ImageUtils.toBitmap(jpeg)
                bitmap = ImageUtils.rotateBitmap(bitmap, 90)
                if (!File(GlobalData.APP_TEMP_DIRECTORY_PATH).exists()) File(GlobalData.APP_TEMP_DIRECTORY_PATH).mkdirs()
                imagePath = GlobalData.getCameraTempFilePath()
                ImageUtils.saveBitmapToFile(
                    bitmap,
                    GlobalData.getCameraTempFilePath(),
                    Bitmap.CompressFormat.JPEG
                )
                sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(File(imagePath))
                    )
                )
                civ_galleryView.setImageBitmap(getLatestImage())
                returnResult()
            }
        })

        arrayOf(iv_btnShutter, civ_galleryView, iv_btnReturn, iv_btnClose).forEach {
            it.setOnClickListener {
                when (it.id) {
                    R.id.iv_btnShutter -> {
                        Toast.makeText(this, getString(R.string.take_picture), Toast.LENGTH_LONG)
                            .show()
                        cv_cameraView.capturePicture()
                    }
                    R.id.civ_galleryView -> {
                        galleryIntent()
                    }
                    R.id.iv_btnReturn -> {
                        finish()
                    }
                    R.id.iv_btnClose -> {
                        rl_dialogInstruction.visibility = View.GONE
                        SaveModule.build(this).isShowedInstructionForCamera = true
                    }
                }
            }
        }

        if (SaveModule.build(this).isShowedInstructionForCamera) rl_dialogInstruction.visibility =
            View.GONE
    }

    private fun galleryIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) return
        if (requestCode == REQUEST_CODE_SELECT_FILE) {
            if (data == null) return
            val imgUri = data.data
            imagePath = ImageUtils.getRealPathFromURI(this, imgUri)
            returnResult()
        }
    }

    private fun returnResult() {
        val intent = Intent()

        intent.putExtra(INTENT_KEY_IMG_PATH, imagePath)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        cv_cameraView.start()
    }

    override fun onPause() {
        super.onPause()
        cv_cameraView.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cv_cameraView.destroy()
    }

    @SuppressLint("Recycle")
    private fun getLatestImage(): Bitmap? {
        val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.MIME_TYPE
        )

        val cursor = contentResolver
            .query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"
            )

        // Put it in the image view
        if (cursor.moveToFirst()) {
            val imageLocation = cursor.getString(1)
            val imageFile = File(imageLocation)
            if (imageFile.exists()) {   // TODO: is there a better way to do this?
                return ImageUtils.getSafeDecodeBitmap(imageLocation, 300)
            }
        }

        return null
    }
}
