package io.testaxis.intellijplugin.messages

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.Topic
import io.testaxis.intellijplugin.models.Build

private const val BUILD_SHOULD_BE_SELECTED_TOPIC = "build-should-be-selected"

interface MessageBusService {
    val bus: MessageBus

    val buildShouldBeSelectedTopic: Topic<BuildNotifier>
}

class DefaultMessageBusService : MessageBusService {
    override val bus = ApplicationManager.getApplication().messageBus

    override val buildShouldBeSelectedTopic = Topic.create(BUILD_SHOULD_BE_SELECTED_TOPIC, BuildNotifier::class.java)
}

fun interface BuildNotifier {
    fun notify(build: Build)
}
