package com.coheser.app.activitesfragments.spaces.utils.RoomManager

import android.os.Bundle

interface RoomFirebaseListener {
    fun createRoom(bundle: Bundle?)
    fun JoinedRoom(bundle: Bundle?)
    fun onRoomLeave(bundle: Bundle?)
    fun onRoomDelete(bundle: Bundle?)
    fun onRoomUpdate(bundle: Bundle?)
    fun onRoomUsersUpdate(bundle: Bundle?)
    fun onMyUserUpdate(bundle: Bundle?)
    fun onSpeakInvitationReceived(bundle: Bundle?)
    fun onWaveUserUpdate(bundle: Bundle?)
}
