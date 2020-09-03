package com.itikarus.miska.tabs.creations

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.itikarus.miska.R
import com.itikarus.miska.base.CameraActivity
import com.itikarus.miska.extentions.gone
import com.itikarus.miska.extentions.invisiable
import com.itikarus.miska.extentions.visiable
import com.itikarus.miska.globals.KeyUtils
import com.itikarus.miska.globals.KeyUtils.REQUEST_CODE_CAMERA
import com.itikarus.miska.globals.KeyUtils.REQUEST_CODE_CAMERA_FROM_CREATION_PAGE
import com.itikarus.miska.globals.KeyUtils.REQUEST_CODE_CREATE_SPACE
import com.itikarus.miska.library.utils.DialogUtils
import com.itikarus.miska.library.utils.ImageUtils
import com.itikarus.miska.tabs.BaseTabActivity
import com.itikarus.miska.tabs.home.CreateSpaceActivity
import com.itikarus.miska.workspace.ProjectInfo
import com.itikarus.miska.workspace.SaveModule
import kotlinx.android.synthetic.main.activity_creations.*
import kotlinx.android.synthetic.main.item_creation.view.*

class CreationsActivity : BaseTabActivity() {

    override fun onActivityLoad() {
        projects = SaveModule.build(this).projects
        mAdapter.notifyDataSetChanged()
    }

    private var projects = ArrayList<ProjectInfo>()
    lateinit var mAdapter : ProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creations)

        mAdapter = ProjectAdapter()
        gridView.adapter = mAdapter

        getMainActivity().onActivityResultMap[javaClass.simpleName] = {requestCode, resultCode, data ->
            if(requestCode == REQUEST_CODE_CAMERA_FROM_CREATION_PAGE && resultCode == Activity.RESULT_OK){
                val imagePath = data?.getStringExtra(KeyUtils.INTENT_KEY_IMG_PATH)
                val intent = Intent(this@CreationsActivity, CreateSpaceActivity::class.java)
                intent.putExtra(KeyUtils.INTENT_KEY_IMG_PATH, imagePath)

                getMainActivity().startActivityForResult(intent, REQUEST_CODE_CREATE_SPACE)

            }
        }

        getMainActivity().onActivityResultMap[javaClass.simpleName + "2"] = {_, _, _ ->
            onActivityLoad()
        }
    }

    override fun onBackPressed() {
        getMainActivity().switchTab(0)
    }

    inner class ProjectAdapter : BaseAdapter(){

        var layoutInflater : LayoutInflater? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            if(layoutInflater == null) layoutInflater = LayoutInflater.from(this@CreationsActivity)

            val view : View
            val cvh : CustomViewHolder
            if(convertView == null){
                view = layoutInflater!!.inflate(R.layout.item_creation, parent,false)
                cvh = CustomViewHolder(view)
                view.tag = cvh
            }else{
                view = convertView
                cvh = view.tag as CustomViewHolder
            }
            with(cvh){
                val model  = getItem(position)
                if(position == count - 1){
                    addNewPanel.visiable()
                    thumbViewer.gone()
                    btnRemove.invisiable()

                    addNewPanel.setOnClickListener {
                        getMainActivity().requestPermission(arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )){
                            val intent = Intent(this@CreationsActivity, CameraActivity::class.java)
                            getMainActivity().startActivityForResult(intent, REQUEST_CODE_CAMERA_FROM_CREATION_PAGE)
                        }
                    }
                }else{
                    addNewPanel.gone()
                    thumbViewer.visiable()
                    btnRemove.visiable()

                    thumbViewer.setOnClickListener {
                        getMainActivity().requestPermission(arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )){
                            val intent = Intent(this@CreationsActivity, CreateSpaceActivity::class.java)
                            intent.putExtra(KeyUtils.INTENT_KEY_CREATE_SPACE, model!!.projectId)
                            getMainActivity().startActivityForResult(intent, REQUEST_CODE_CREATE_SPACE)
                        }
                    }

                    btnRemove.setOnClickListener {
                        DialogUtils.showOkayNoDialog(this@CreationsActivity, getString(R.string.coution), getString(R.string.msg_delete_project), object : DialogUtils.OnOkayNoEvent{
                            override fun onOkay() {
                                SaveModule.build(this@CreationsActivity).removeProjectByID(model!!.projectId)
                                projects = SaveModule.build(this@CreationsActivity).projects
                                mAdapter.notifyDataSetChanged()
                            }

                            override fun onNo() {

                            }
                        })
                    }
                }

                titleViewer.text = model?.projectName
                timeViewer.text = model?.projectDt
                thumbViewer.setImageBitmap(ImageUtils.getSafeDecodeBitmap(model?.thumbFileName, 300))
            }
            return view
        }

        override fun getItem(position: Int): ProjectInfo? {
            return if (position == count - 1){
                null
            }else{
                projects[position]
            }

        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return projects.size + 1
        }

        inner class CustomViewHolder(view : View){
            val addNewPanel = view.rl_addNewPanel
            val titleViewer = view.tv_nameViewer
            val timeViewer = view.tv_timeViewer
            val thumbViewer = view.iv_thumbViewer
            val btnRemove = view.iv_btnDelete
        }
    }
}
