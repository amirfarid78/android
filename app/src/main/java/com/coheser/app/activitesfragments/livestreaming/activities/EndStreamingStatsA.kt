package com.coheser.app.activitesfragments.livestreaming.activities

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.Constants
import com.coheser.app.activitesfragments.livestreaming.adapter.TopGifterAdapter
import com.coheser.app.activitesfragments.livestreaming.model.GiftUsers
import com.coheser.app.activitesfragments.livestreaming.model.LiveCoinsModel
import com.coheser.app.databinding.ActivityEndStreamingStatsBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DateOprations
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Variables


class EndStreamingStatsA : AppCompatLocaleActivity() {

    lateinit var binding: ActivityEndStreamingStatsBinding
    var senderCoinsList: ArrayList<LiveCoinsModel?> = ArrayList()
    var likeCount="0"
    var viewersCount="0"
    var joinTime:Long=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(
                this
            ).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this, javaClass, false
        )
        binding = ActivityEndStreamingStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let {
            senderCoinsList = it.getParcelableArrayList("senderCoinsList")!!
            likeCount = it.getString("likeCount")!!
            viewersCount = it.getString("viewersCount")!!
            joinTime = it.getLong("joinTime")!!
        }

        binding.likeCountTxt.text=likeCount
        binding.viewersCountTxt.text=viewersCount
        var maxCoins = 0
        for (item: LiveCoinsModel? in senderCoinsList) {
            maxCoins = maxCoins + (item!!.sendedCoins!!.toDouble()).toInt()
        }
        binding.coinsCountTxt.text="🪙 ${maxCoins}"
        binding.joinTimeTxt.text= DateOprations.millisecondsToMMSS((System.currentTimeMillis()-joinTime))

        if(senderCoinsList?.isNotEmpty() == true) {
            setTopGifterAdapter()
        }
     }



    fun setTopGifterAdapter(){
        Functions.printLog(Constants.tag,"senderCoinsList"+senderCoinsList.size)
        val top5Users = senderCoinsList
            .sortedByDescending { it?.sendedCoins }
            .take(5)

        val mergedList = ArrayList<GiftUsers>()
        for(item in top5Users){
            val giftUsers = GiftUsers()
            giftUsers.userId=item?.userId.toString()
            giftUsers.userName=item?.userName.toString()
            giftUsers.userPicture=item?.userPicture.toString()
            giftUsers.count= (item?.sendedCoins?:0).toInt()
            mergedList.add(giftUsers)
        }

            binding.topGifterLayout.visibility= View.VISIBLE
            binding.topRecylerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            val adapter = TopGifterAdapter(this,mergedList,object : AdapterClickListener {
                override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                }
            })
            binding.topRecylerView.adapter = adapter

        }


}