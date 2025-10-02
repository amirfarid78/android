package com.coheser.app.activitesfragments.spaces.models

import java.io.Serializable


class TopicModel : Serializable {
    @JvmField
    var id: String? = null
    @JvmField
    var title: String? = null
    @JvmField
    var image: String? = null
    var created: String? = null
    var follow: String? = null
    var isSelected: Boolean = false
}
