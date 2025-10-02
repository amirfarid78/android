package com.coheser.app.activitesfragments.accounts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.databinding.FragmentDobFragmentBinding
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.ycuwq.datepicker.date.DatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateOfBirthFragment : Fragment() {
    lateinit var binding:FragmentDobFragmentBinding
    var currentDate = ""
    var stYear  = ""
    var fromWhere = ""
    var userRegisterModel: UserRegisterModel? = UserRegisterModel()


    companion object {
        fun newInstance(fromWhere: String,userRegisterModel: UserRegisterModel?): DateOfBirthFragment {
            val fragment = DateOfBirthFragment()
            val args = Bundle()
            args.putString("fromWhere",fromWhere)
            args.putSerializable("user_model",userRegisterModel)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_dob_fragment, container, false)
        initControl()
        actionControl()
        return binding.root
    }

    private fun initControl() {
        arguments?.let {
            fromWhere = it.getString("fromWhere","")
            userRegisterModel = it.getSerializable("user_model") as UserRegisterModel?
        }
        Log.d(Constants.tag, "referalCode : " + userRegisterModel?.referalCode)
        binding.datePicker.setMaxDate(System.currentTimeMillis() - 1000)

        binding.datePicker.setOnDateSelectedListener(DatePicker.OnDateSelectedListener { year, month, day -> // select the date from datepicker

            hideError()
            binding.btnDobNext.setEnabled(true)
            binding.btnDobNext.setClickable(true)
            stYear = year.toString()
            currentDate = "$year-$month-$day"
            userRegisterModel?.dateOfBirth = currentDate
        })
        binding.datePicker.getYearPicker().setEndYear(2020)

    }
    private fun actionControl() {
        binding.goBack.setOnClickListener(DebounceClickHandler{
            activity?.onBackPressed()
        })
        binding.btnDobNext.setOnClickListener(DebounceClickHandler{
            checkDobDate()
        })
    }

    fun checkDobDate() {
        val df = SimpleDateFormat("yyy", Locale.ENGLISH)
        val formattedDate = df.format(Calendar.getInstance().time)
        var dob: Date? = null
        var currentdate: Date? = null
        try {
            dob = df.parse(formattedDate)
            currentdate = df.parse(currentDate)
        } catch (e: Exception) {
            Log.d(Constants.tag,"DateParsingException: $e")
        }
        val value = getDiffYears(currentdate, dob)
        if (value > 17) {
            moveToUsernameScreen()

        } else {
            showError( binding.root.context.getString(R.string.age_must_be_over_eighteen))

        }
    }


    private fun moveToUsernameScreen() {
        val nextf = CreateUsernameFragment.newInstance(fromWhere,userRegisterModel)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.in_from_right,
            R.anim.out_to_left,
            R.anim.in_from_left,
            R.anim.out_to_right
        )
        transaction.addToBackStack(null)
        transaction.replace(R.id.dob_fragment, nextf).commit()
    }

    // this method will return the years difference
    fun getDiffYears(first: Date?, last: Date?): Int {
        val a = getCalendar(first)
        val b = getCalendar(last)
        var diff = b[Calendar.YEAR] - a[Calendar.YEAR]
        if (a[Calendar.MONTH] > b[Calendar.MONTH] || a[Calendar.MONTH] == b[Calendar.MONTH] && a[Calendar.DATE] > b[Calendar.DATE]) {
            diff--
        }
        return diff
    }

    fun getCalendar(date: Date?): Calendar {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.time = date
        return cal
    }

    fun showError(error:String){
        binding.errorMsgTxt.text= Constants.alertUniCode+ error
        binding.errorMsgTxt.visibility=View.VISIBLE

    }
    fun hideError(){
        binding.errorMsgTxt.visibility=View.GONE

    }
}