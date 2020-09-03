package com.itikarus.miska.globals

import android.os.Environment
import java.io.File

object GlobalData {

    val APP_ROOT_DIRECTORY_PATH = Environment.getExternalStorageDirectory().path + "/" + ".MIKSA"
    val APP_TEMP_DIRECTORY_PATH = "$APP_ROOT_DIRECTORY_PATH/TEMP"
    val APP_CREATIONS_DIRECTORY_PATH = "$APP_ROOT_DIRECTORY_PATH/CREATIONS"

    fun getAppRootDirectory(): File {
        return File(APP_ROOT_DIRECTORY_PATH)
    }

    fun getAppTempDirectory(): File {
        return File(APP_TEMP_DIRECTORY_PATH)
    }

    fun getAppCreationsDirectory(): File {
        return File(APP_CREATIONS_DIRECTORY_PATH)
    }

    fun getCameraTempFilePath(): String {
        return getCameraTempFile().path
    }

    fun getCameraTempFile(): File {
        return File(APP_TEMP_DIRECTORY_PATH, "temp_take_photo.jpg")
    }
}