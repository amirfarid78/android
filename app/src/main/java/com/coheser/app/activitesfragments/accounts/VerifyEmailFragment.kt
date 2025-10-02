package com.coheser.app.activitesfragments.accounts

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.coheser.app.R
import com.coheser.app.databinding.FragmentVerifyEmailBinding
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.google.firebase.auth.FirebaseAuth

class VerifyEmailFragment : Fragment() {

    lateinit var binding: FragmentVerifyEmailBinding

    var fromWhere = ""
    var userRegisterModel: UserRegisterModel? = UserRegisterModel()
    lateinit var mAuth: FirebaseAuth

    companion object {
        fun newInstance(
            fromWhere: String,
            userRegisterModel: UserRegisterModel?
        ): VerifyEmailFragment {
            val fragment = VerifyEmailFragment()
            val args = Bundle()
            args.putString("fromWhere", fromWhere)
            args.putSerializable("user_model", userRegisterModel)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_verify_email, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initControl()
        actionControl()
    }

    private fun initControl() {
        arguments?.let {
            fromWhere = it.getString("fromWhere", "")
            userRegisterModel = it.getSerializable("user_model") as UserRegisterModel?
        }
        mAuth = FirebaseAuth.getInstance()

    }

    private fun actionControl() {
        binding.goBack.setOnClickListener(DebounceClickHandler {
            activity?.onBackPressed()
        })

        // Set the email text in the TextView
        val verificationMessage = getString(R.string.verification_email_message, userRegisterModel?.email)
        binding.verificationDescription.text = verificationMessage

        // Disable the button initially

        // Enable the button after 5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            binding.btnLoginContinue.isEnabled = true
            binding.btnLoginContinue.text = getString(R.string.log_in)
            binding.btnLoginContinue.setBackgroundColor(resources.getColor(R.color.red_bg))
        }, 5000)

        // Set a click listener for the "Log in and Continue" button
        binding.btnLoginContinue.setOnClickListener {
            // Handle the login action here

            userRegisterModel?.let {
                reloadUser()
            }
        }
    }

    private fun reloadUser() {
        val user = mAuth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (user.isEmailVerified) {

                    openGetStartActivity(fromWhere)
                } else {
                    // Email is still not verified
                    showEmailVerificationDialog()

                }
            } else {
                Toast.makeText(requireContext(), "Failed to reload user data.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun openGetStartActivity(type: String) {
        val DOBF = DateOfBirthFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.in_from_right,
            R.anim.out_to_left,
            R.anim.in_from_left,
            R.anim.out_to_right
        )
        val bundle = Bundle()
        bundle.putSerializable("user_model", userRegisterModel)
        if (type.equals(AccountUtils.typeSocial)) {
            bundle.putString("fromWhere", AccountUtils.typeSocial)
        } else {
            bundle.putString("fromWhere", AccountUtils.typeSignUp)
        }
        DOBF.arguments = bundle
        transaction.addToBackStack(null)
        transaction.replace(R.id.login_f, DOBF).commit()

    }


    private fun showEmailVerificationDialog() {
        val builder = AlertDialog.Builder(requireContext())

        // Set the custom layout
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_verify_email, null)
        builder.setView(dialogLayout)

        val dialog = builder.create()
        dialog.show()

        // Handle the OK button click
        dialogLayout.findViewById<View>(R.id.dialog_ok_button).setOnClickListener {
            dialog.dismiss()
        }

        // Handle close button click
        dialogLayout.findViewById<View>(R.id.dialog_close_icon).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun emailLoginActionPerform() {
        val nextF = EmailPhoneFragment.newInstance(AccountUtils.typeLogin, false, userRegisterModel)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right
        )
        transaction.addToBackStack(null)
        transaction.replace(R.id.verify_email_fragment, nextF, AccountUtils.typeLogin)
            .addToBackStack(null)
            .commit()
    }
}