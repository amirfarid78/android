package com.coheser.app.activitesfragments.spaces.utils.RoomManager

import android.os.Bundle

interface RoomApisListener {
    fun roomCreated(bundle: Bundle?)

    //invite members into room
    fun roomInvitationsSended(bundle: Bundle?)

    //    invite members into room
    fun goAheadForRoomGenrate(bundle: Bundle?)

    //    invite members into room
    fun goAheadForRoomJoin(bundle: Bundle?)

    fun onRoomJoined(bundle: Bundle?)

    fun onRoomReJoin(bundle: Bundle?)

    fun onRoomMemberUpdate(bundle: Bundle?)

    //    leave room
    fun doRoomLeave(bundle: Bundle?)

    //    delete room
    fun doRoomDelete(bundle: Bundle?)


    fun onRoomLeave(bundle: Bundle?)

    fun onRoomDelete(bundle: Bundle?)

    //    open room with detail
    fun showRoomDetailAfterJoin(bundle: Bundle?)
}
