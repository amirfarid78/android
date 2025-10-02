package com.coheser.app.simpleclasses

import android.os.Bundle

class DataHolder private constructor() {
    var data: Bundle? = null
    companion object {
        @get:Synchronized
        var instance: DataHolder? = null
            get() {
                if (field == null) {
                    field = DataHolder()
                }
                return field
            }
            private set
    }
}
