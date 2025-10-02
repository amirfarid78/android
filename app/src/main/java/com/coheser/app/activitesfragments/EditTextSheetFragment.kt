package com.coheser.app.activitesfragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.activitesfragments.comments.CommentTagedFriendsFragment
import com.coheser.app.databinding.FragmentEditTextSheetBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditTextSheetFragment : BottomSheetDialogFragment, View.OnClickListener {
    lateinit var binding: FragmentEditTextSheetBinding
    var callBack: FragmentCallBack? = null
    var commentType: String? = null
    var tagedUser = ArrayList<UserModel>()

    companion object{
        val commentTypeOwn = "OwnComment"
        val commentTypeReply = "replyComment"
        val commentSelectNumber="selectNumber"
    }

    constructor(
        commentType: String?,
        tagedUser: ArrayList<UserModel>?,
        callBack: FragmentCallBack?
    ) {
        this.callBack = callBack

        if (tagedUser != null) {
            this.tagedUser = tagedUser
        }
        this.commentType = commentType
    }

    constructor()

    override fun onDetach() {
        hideKeyboard()
        super.onDetach()
    }

    private fun hideKeyboard() {
        binding.messageEdit.clearFocus()
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.messageEdit.windowToken, 0)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_text_sheet, container, false)

        initControl()
        actionControl()

        if(tagedUser.isNotEmpty()){
            val stringBuilder=StringBuilder()
            for (model in tagedUser){
                stringBuilder.append("@${model.username} ")
            }
            binding.messageEdit.setText(stringBuilder.toString())
            binding.messageEdit.setSelection(binding.messageEdit.text!!.length)
        }
        return binding.getRoot()
    }

    override fun onStart() {
        super.onStart()
        showKeyboard()
    }
    private fun actionControl() {
        binding.messageEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, count: Int) {
                val message = binding.messageEdit.text.toString()
                if (message.length > 0) {
                    if(commentType!= commentSelectNumber) {
                        val lastChar = charSequence.toString().substring(charSequence.length - 1)
                        if (lastChar == "@") {
                            binding.tabTagFriends.visibility = View.GONE
                            openFriends()
                        } else {
                            binding.tabTagFriends.visibility = View.VISIBLE
                        }
                    }

                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        binding.tabTagFriends.setOnClickListener {
            var message = binding.messageEdit.text.toString()
            message = "$message@"
            binding.messageEdit.setText("" + message)
            binding.messageEdit.setSelection(binding.messageEdit.text!!.length)
        }
    }

    private fun openFriends() {
        val fragment = CommentTagedFriendsFragment(
            Functions.getSharedPreference(
                binding.root.context
            ).getString(Variables.U_ID, "")
        ) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                val arrayList = bundle.getSerializable("data") as ArrayList<UserModel>?
                for (i in arrayList!!.indices) {
                    val item = arrayList[i]
                    tagedUser.add(item)
                    var lastChar: String? = null
                    if (!TextUtils.isEmpty(binding.messageEdit.text.toString())) lastChar =
                        binding.messageEdit.text.toString().substring(
                            binding.messageEdit.text!!.length - 1
                        )
                    if (lastChar != null && lastChar.contains("@")) binding.messageEdit.setText(
                        binding.messageEdit.text.toString() + item.username + " "
                    ) else binding.messageEdit.setText(
                        binding.messageEdit.text.toString() + "@" + item.username + " "
                    )
                    binding.messageEdit.setSelection(binding.messageEdit.text!!.length)
                }
            }
        }
        fragment.show(requireActivity().supportFragmentManager, "CommentTagedFriendsF")
    }

    private fun initControl() {
        if (commentType == commentTypeOwn) {
            binding.messageEdit.hint = binding.root.context.getString(R.string.leave_a_comment)
        } else if (commentType == commentTypeReply) {
            binding.messageEdit.hint = "" + requireArguments().getString("replyStr")
        }
        else if (commentType == commentSelectNumber) {
            binding.messageEdit.hint = getString(R.string.enter_a_number)
            binding.messageEdit.inputType=InputType.TYPE_CLASS_NUMBER
            binding.tabTagFriends.visibility = View.GONE
            binding.sendBtn.setImageResource(R.drawable.ic_tick)
            binding.sendBtn.rotation =0f
        }
        binding.sendBtn.setOnClickListener(this)
        binding.messageEdit.setOnClickListener(this)

    }

    private fun showKeyboard() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(200)
            binding.messageEdit.requestFocus()
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.messageEdit, 0)
        }

    }



    override fun onClick(view: View) {
        when (view.id) {
            R.id.send_btn -> {
                if (binding.messageEdit.text.toString().length > 0) {
                    val bundle = Bundle()
                    bundle.putBoolean("isShow", true)
                    bundle.putString("action", "sendComment")
                    bundle.putString("message", "${binding.messageEdit.text.toString()}")
                    bundle.putSerializable("taggedUserList", tagedUser)
                    callBack?.onResponce(bundle)
                    dismiss()
                }
            }

            R.id.message_edit -> {
                view.clearFocus()
                view.requestFocus()
                Log.d(com.coheser.app.Constants.tag, "Focus True")
            }
        }
    }
}