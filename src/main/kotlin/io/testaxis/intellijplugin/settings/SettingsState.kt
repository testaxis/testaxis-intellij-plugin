package io.testaxis.intellijplugin.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import io.testaxis.intellijplugin.config

@State(name = "io.testaxis.intellijplugin.settings.SettingsState", storages = [Storage("TestAxisSettings.xml")])
class SettingsState : PersistentStateComponent<SettingsState> {
    var authenticatonToken = ""
    var projectId = -1
    var serverHost = config(config.testaxis.defaultHost)

    override fun getState(): SettingsState {
        return this
    }

    override fun loadState(state: SettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun isInitialized() = authenticatonToken.isNotEmpty() && projectId != -1
}
