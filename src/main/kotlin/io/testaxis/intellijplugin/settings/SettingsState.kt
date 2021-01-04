package io.testaxis.intellijplugin.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "io.testaxis.intellijplugin.settings.SettingsState", storages = [Storage("TestAxisSettings.xml")])
class SettingsState : PersistentStateComponent<SettingsState> {
    var authenticatonToken = ""
    var projectId = -1

    override fun getState(): SettingsState {
        return this
    }

    override fun loadState(state: SettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun isInitialized() = authenticatonToken.isNotEmpty() && projectId != -1
}
