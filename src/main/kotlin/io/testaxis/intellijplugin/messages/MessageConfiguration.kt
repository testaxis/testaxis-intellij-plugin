package io.testaxis.intellijplugin.messages

import com.intellij.util.messages.Topic
import io.testaxis.intellijplugin.models.Build

class MessageConfiguration {
    companion object {
        val BUILD_SHOULD_BE_SELECTED_TOPIC = Topic.create("build-should-be-selected", BuildNotifier::class.java)
    }

    fun interface BuildNotifier {
        fun notify(build: Build)
    }
}
