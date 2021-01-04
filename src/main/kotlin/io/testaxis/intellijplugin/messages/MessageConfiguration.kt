package io.testaxis.intellijplugin.messages

import com.intellij.util.messages.Topic
import io.testaxis.intellijplugin.models.Build
import io.testaxis.intellijplugin.settings.SettingsState

class MessageConfiguration {
    companion object {
        val BUILD_SHOULD_BE_SELECTED_TOPIC = Topic.create("build-should-be-selected", BuildNotifier::class.java)
        val API_SETTINGS_UPDATED_TOPIC = Topic.create("api-settings-updated", ApiSettingsNotifier::class.java)
    }

    fun interface BuildNotifier {
        fun notify(build: Build)
    }

    fun interface ApiSettingsNotifier {
        fun notify(settings: SettingsState)
    }
}
