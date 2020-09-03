package com.itikarus.miska.globals

import android.content.Context
import com.itikarus.miska.library.utils.TinyDB

class LocalDB(context: Context) {

    var userFirstName = ""
        set(value) {
            field = value
            update()
        }

    var userLastName = ""
        set(value) {
            field = value
            update()
        }

    var userID = ""
        set(value) {
            field = value
            update()
        }
    var userCookie = ""
        set(value) {
            field = value
            update()
        }
    var userEmail = ""
        set(value) {
            field = value
            update()
        }
    var username = ""
        set(value) {
            field = value
            update()
        }
    var userDisplayName = ""
        set(value) {
            field = value
            update()
        }

    var userPhone = ""
        set(value) {
            field = value
            update()
        }

    var userPicture = ""
        set(value) {
            field = value
            update()
        }

    var address1 = ""
        set(value) {
            field = value
            update()
        }

    var address2 = ""
        set(value) {
            field = value
            update()
        }

    var company = ""
        set(value) {
            field = value
            update()
        }

    var city = ""
        set(value) {
            field = value
            update()
        }

    var state = ""
        set(value) {
            field = value
            update()
        }


    var hasLoginAuth = false
        set(value) {
            field = value
            update()
        }

    init {
        mDB = TinyDB(context)
    }

    companion object {

        private val LOCAL_DB_DATA = "LOCAL_DB_DATA"

        private var mDB: TinyDB? = null

        fun build(context: Context): LocalDB {
            mDB = TinyDB(context)
            try {
                return mDB!!.getObject(LOCAL_DB_DATA, LocalDB::class.java) as LocalDB
            } catch (e: NullPointerException) {
                return LocalDB(context)
            }

        }
    }

    fun update() {
        mDB!!.putObject(LOCAL_DB_DATA, this)
    }

    fun clearDB() {
        userID = ""
        userFirstName = ""
        userLastName = ""
        userCookie = ""
        userEmail = ""
        username = ""
        userDisplayName = ""
        userPhone = ""
        userPicture = ""
        address1 = ""
        address2 = ""
        company = ""
        city = ""
        state = ""
        hasLoginAuth = false
        update()
    }
}