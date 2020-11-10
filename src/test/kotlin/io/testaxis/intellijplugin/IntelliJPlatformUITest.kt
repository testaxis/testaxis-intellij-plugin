package io.testaxis.intellijplugin

import org.assertj.core.api.Assertions
import org.assertj.swing.core.GenericTypeMatcher
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.exception.ComponentLookupException
import org.assertj.swing.fixture.FrameFixture
import org.junit.jupiter.api.BeforeAll
import javax.swing.JLabel

abstract class IntelliJPlatformUITest : IntelliJPlatformTest() {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUpOnce() {
            FailOnThreadViolationRepaintManager.install()
        }
    }

    protected fun FrameFixture.requireContainsLabel(requiredText: String) {
        val textMatcher: GenericTypeMatcher<JLabel> = object : GenericTypeMatcher<JLabel>(JLabel::class.java) {
            override fun isMatching(label: JLabel) = requiredText == label.text
        }

        try {
            label(textMatcher).requireVisible()
        } catch (exception: ComponentLookupException) {
            Assertions.fail("Label with \"$requiredText\" is not present in the frame.")
        }
    }
}
