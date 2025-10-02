package com.coheser.app.activitesfragments.videorecording

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.activities.MulticastStreamerActivity
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.activitesfragments.shoping.AddProducts.ListProducts
import com.coheser.app.simpleclasses.DateOprations
import com.coheser.app.simpleclasses.Dialogs
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isOpenGLVersionSupported
import com.coheser.app.simpleclasses.Functions.isWorkManagerRunning
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.TicTicApp
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.coheser.app.databinding.FragmentVideoCreationBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject

class CreateContentFragment(var fragmentCallBack: FragmentCallBack) : BottomSheetDialogFragment(), View.OnClickListener {

    var streamingId = ""
    lateinit var binding: FragmentVideoCreationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =DataBindingUtil.inflate(inflater,R.layout.fragment_video_creation, container, false)

        binding.goBack.setOnClickListener(this)
        binding.postArgearLayout.setOnClickListener(this)
        binding.liveLayout.setOnClickListener(this)
        binding.createDish.visibility = View.VISIBLE
        binding.createDish.setOnClickListener(this)
        binding.spacelayout.setOnClickListener(this)
        return binding.root
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.live_layout -> {
                liveStreamingId
            }

            R.id.goBack -> dismiss()
            R.id.post_argear_layout -> {
                val isOpenGLSupported = isOpenGLVersionSupported(context, 0x00030001)
                if (isOpenGLSupported) {
                    openVideoCamera()
                } else {
                    Toast.makeText(
                        binding.root.context,
                       requireContext().getString(R.string.your_device_opengl_verison_is_not_compatible_to_use_this_feature),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            R.id.spacelayout->{
                dismiss()
                if(fragmentCallBack!=null){
                    fragmentCallBack.onResponce(Bundle())
                }
            }

            R.id.createDish -> {
                    dismiss()

                    val intent = Intent(context, ListProducts::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)

            }

        }
    }



    fun openVideoCamera() {
        if (isWorkManagerRunning(requireContext(), "videoUpload")) {
            Toast.makeText(
                binding.root.context,
                requireContext().getString(R.string.video_already_in_progress),
                Toast.LENGTH_SHORT
            ).show()
            Dialogs.showAlert(
                activity,
                requireContext().getString(R.string.app_name),
                requireContext().getString(R.string.video_already_in_progress)
            )
        } else {
            dismiss()
            val intent = Intent(binding.root.context, com.coheser.app.activitesfragments.videorecording.VideoRecoderActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }

    private val liveStreamingId: Unit
        private get() {
            val parameters = JSONObject()
            try {
                parameters.put(
                    "user_id",
                    getSharedPreference(binding.root.context).getString(com.coheser.app.simpleclasses.Variables.U_ID, "0")
                )
                parameters.put("started_at", DateOprations.getCurrentDate("yyyy-MM-dd HH:mm:ss"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            showLoader(activity, false, false)
            VolleyRequest.JsonPostRequest(
                activity,
                com.coheser.app.apiclasses.ApiLinks.liveStream,
                parameters,
                getHeaders(binding.root.context)
            ) { resp ->
                checkStatus(activity, resp)
                cancelLoader()
                try {
                    val jsonObject = JSONObject(resp)
                    val code = jsonObject.optString("code")
                    if (code == "200") {
                        val msgObj = jsonObject.getJSONObject("msg")
                        val streamingObj = msgObj.getJSONObject("LiveStreaming")
                        streamingId = streamingObj.optString("id")
                        gotoRoleActivity()
                    }
                } catch (e: Exception) {
                    Log.d(com.coheser.app.Constants.tag, "Exception : $e")
                }
            }
        }



    fun gotoRoleActivity() {
        val model=LiveUserModel()
        model.userId=getSharedPreference(binding.root.context).getString(Variables.U_ID, "")
        model.userName = getSharedPreference(binding.root.context).getString(Variables.U_NAME, "")
        model.userPicture=getSharedPreference(binding.root.context).getString(Variables.U_PIC, "")
        model.streamingId =streamingId
        model.isDualStreaming = true
        model.onlineType ="multicast"

        val intent = Intent()
        val myApp = requireActivity().application as TicTicApp
        myApp.engineConfig().uid = getSharedPreference(binding.root.context).getString(Variables.U_ID, "")
        myApp.engineConfig().channelName = streamingId

        intent.putExtra("data",model)
        intent.setClass(requireActivity(), MulticastStreamerActivity::class.java)
        dismiss()
        startActivity(intent)
    }

}