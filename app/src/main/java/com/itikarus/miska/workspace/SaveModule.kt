package com.itikarus.miska.workspace

import android.content.Context
import com.itikarus.miska.library.utils.TinyDB
import java.io.File

class SaveModule private constructor(context: Context) {


    val projects = ArrayList<ProjectInfo>()

    var currentUniqueID = 0

    companion object {
        private val TAG = "SaveModule"
        private var mDB: TinyDB? = null
        fun build(context : Context) : SaveModule{
            mDB = TinyDB(context)
            try {
                return mDB?.getObject(TAG, SaveModule::class.java) as SaveModule
            } catch (e: NullPointerException) {
                return SaveModule(context)
            }

        }
    }


    fun getFindProductByID(id : Int) : ProjectInfo?{
        projects.forEach {
            if(it.projectId == id) return it
        }

        return null
    }

    fun removeProjectByID(id : Int){

        val iterator = projects.iterator()
        while (iterator.hasNext()){
            val projectModel = iterator.next()
            if(projectModel.projectId == id){
                val thumbFile = File(projectModel.thumbFileName)
                if(thumbFile.exists()) thumbFile.delete()
                iterator.remove()
            }
        }

        update()
    }

    fun getNextUniqueID() : Int{
        return currentUniqueID + 1
    }

    fun updateCurrnetUniqueID(uniqueID : Int){
        currentUniqueID = uniqueID
        update()
    }

    fun addProject(projectInfo: ProjectInfo){
        projects.add(projectInfo)
        updateCurrnetUniqueID(projectInfo.projectId)
        update()
    }

    fun update() {
        mDB?.putObject(TAG, this)
    }

    init {
        mDB = TinyDB(context)
    }

    var isShowedInstructionForCamera = false
        set(value) {
            field = value
            update()
        }
    var isShowedInstructionForWorkspace = false
        set(value) {
            field = value
            update()
        }
}