package io.testaxis.intellijplugin.models

import com.intellij.icons.AllIcons
import javax.swing.Icon

enum class BuildStatus {
    SUCCESS {
        override val icon get() = AllIcons.General.InspectionsOK
    },
    BUILD_FAILED {
        override val icon get() = AllIcons.General.Warning
    },
    TESTS_FAILED {
        override val icon get() = AllIcons.General.Error
    },
    UNKNOWN {
        override val icon get() = AllIcons.RunConfigurations.TestUnknown
    };

    abstract val icon: Icon
}
