package com.coheser.app.simpleclasses

import android.content.Context
import android.util.Log
import com.coheser.app.Constants
import com.coheser.app.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateOprations {

    //use to get fomated time
    fun getTimeInMilli(dateFormat: String?, date: String?): Double {
        val calendarDate = Calendar.getInstance()
        val f = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        var d: Date? = null
        try {
            d = f.parse(date)
            calendarDate.time = d
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return calendarDate.time.time.toDouble()
    }


    @JvmStatic
    fun millisecondsToMMSS(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }


    @JvmStatic
    fun getDurationInDays(format: String?, start: String?, end: String?): String {
        return try {
            val startDateCal = Calendar.getInstance()
            val endDateCal = Calendar.getInstance()
            val f = SimpleDateFormat(format, Locale.ENGLISH)
            var startDate: Date? = null
            try {
                startDate = f.parse(start)
                startDateCal.time = startDate
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception startDate: $e")
            }
            var endDate: Date? = null
            try {
                endDate = f.parse(end)
                endDateCal.time = endDate
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception endDate: $e")
            }
            val difference = (endDateCal.timeInMillis - startDateCal.timeInMillis) / 1000
            val days = difference / 86400
            "" + days
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception days: $e")
            "0"
        }
    }

    @JvmStatic
   inline fun getDurationInPoints(format: String?, start: String?, end: String?): String {
        return try {
            val startDateCal = Calendar.getInstance()
            val endDateCal = Calendar.getInstance()
            val f = SimpleDateFormat(format, Locale.ENGLISH)
            var startDate: Date? = null
            try {
                startDate = f.parse(start)
                startDateCal.time = startDate
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception startDate: $e")
            }
            var endDate: Date? = null
            try {
                endDate = f.parse(end)
                endDateCal.time = endDate
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception endDate: $e")
            }
            val difference = (endDateCal.timeInMillis - startDateCal.timeInMillis) / 1000
            val days = difference.toDouble() / 86400
            "" + days
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception days: $e")
            "0"
        }
    }

    @JvmStatic
   inline fun changeDateLatterFormat(format: String?, context: Context, date: String): String {
        return try {
            val current_cal = Calendar.getInstance()
            val date_cal = Calendar.getInstance()
            val f = SimpleDateFormat(format, Locale.ENGLISH)
            var d: Date? = null
            try {
                d = f.parse(date)
                date_cal.time = d
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val difference = (current_cal.timeInMillis - date_cal.timeInMillis) / 1000
            if (difference < 60) {
                difference.toString() + context.getString(R.string.s_ago)
            } else if (difference < 3600) {
                (0 + difference / 60).toString() + context.getString(R.string.m_ago)
            } else if (difference < 86400) {
                (0 + difference / 3600).toString() + context.getString(R.string.h_ago)
            } else if (difference < 604800) {
                (0 + difference / 86400).toString() + context.getString(R.string.d_ago)
            } else {
                if (difference < 2592000) {
                    (0 + difference / 604800).toString() + context.getString(R.string.week_ago)
                } else {
                    if (difference < 31536000) {
                        (0 + difference / 2592000).toString() + context.getString(R.string.month_ago)
                    } else {
                        (0 + difference / 31536000).toString() + context.getString(R.string.year_ago)
                    }
                }
            }
        } catch (e: Exception) {
            date
        }
    }

    @JvmStatic
   inline fun changeDateTodayYesterday(context: Context, date: String): String {
        return try {
            val current_cal = Calendar.getInstance()
            val date_cal = Calendar.getInstance()
            val f = SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ", Locale.ENGLISH)
            var d: Date? = null
            try {
                d = f.parse(date)
                date_cal.time = d
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val difference = (current_cal.timeInMillis - date_cal.timeInMillis) / 1000
            if (difference < 86400) {
                if (current_cal[Calendar.DAY_OF_YEAR] - date_cal[Calendar.DAY_OF_YEAR] == 0) {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                    sdf.format(d)
                } else context.getString(R.string.yesterday)
            } else if (difference < 172800) {
                context.getString(R.string.yesterday)
            } else (difference / 86400).toString() + context.getString(R.string.day_ago)
        } catch (e: Exception) {
            date
        }
    }

    @JvmStatic
   inline fun checkTimeDiffernce(current_cal: Calendar, date: String): Boolean {
        return try {
            val date_cal = Calendar.getInstance()
            val f = SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ", Locale.ENGLISH)
            var d: Date? = null
            try {
                d = f.parse(date)
                date_cal.time = d
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val difference = (current_cal.timeInMillis - date_cal.timeInMillis) / 1000
            if (difference < 0) {
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


    // getCurrent Date
    @JvmStatic
    fun getCurrentDate(dateFormat: String?): String {
        val format = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        val date = Calendar.getInstance()
        return format.format(date.time)
    }

    // getCurrent Date
    @JvmStatic
    fun getCurrentDate(dateFormat: String?, days: Int): String {
        val format = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        val date = Calendar.getInstance()
        date.add(Calendar.DAY_OF_MONTH, days)
        return format.format(date.time)
    }

    //use to get fomated time
    @JvmStatic
    fun getTimeWithAdditionalSecond(dateFormat: String?, second: Int): String {
        val calendarDate = Calendar.getInstance()
        val date = "00:00:00"
        val f = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        var d: Date? = null
        try {
            d = f.parse(date)
            calendarDate.time = d
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val format = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        calendarDate.add(Calendar.SECOND, second)
        return format.format(calendarDate.time)
    }

    @JvmStatic
    fun getTimeAgoOrg(date_time: String?): String {
        val timeAgo2 = TimeAgo2()
        return timeAgo2.covertTimeToText(date_time)
    }





    //This method will change the date format
    fun changeDateFormat(fromFormat: String, toFormat: String, date: String): String {
        val dateFormat = SimpleDateFormat(fromFormat, Locale.ENGLISH)
        var sourceDate: Date? = null
        return try {
            sourceDate = dateFormat.parse(date)
            val targetFormat = SimpleDateFormat(toFormat, Locale.ENGLISH)
            targetFormat.format(sourceDate)
        } catch (e: ParseException) {
            e.printStackTrace()
            Functions.printLog(Constants.tag,"e at date : $e")
            ""
        }
    }

}