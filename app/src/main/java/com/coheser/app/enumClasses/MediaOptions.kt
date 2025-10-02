package com.coheser.app.enumClasses
import android.content.Context
import androidx.annotation.StringRes
import com.coheser.app.R

enum class MediaOptions(val value: String, @StringRes val stringResId: Int) {
    TakePhoto("1", R.string.take_photo),
    SelectGallery("2", R.string.select_from_gallery),
    ViewPhoto("3", R.string.view_photo),
    RemovePhoto("4", R.string.remove_photo),

    ChangeVideo("5", R.string.change_video),
    RemoveVideo("6", R.string.remove_video),
    WatchVideo("7", R.string.watch_video);


    fun getValue(context: Context): String {
        return context.getString(stringResId)
    }
}