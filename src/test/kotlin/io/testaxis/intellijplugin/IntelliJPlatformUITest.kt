package io.testaxis.intellijplugin

import org.assertj.core.api.Assertions
import org.assertj.swing.core.GenericTypeMatcher
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.exception.ComponentLookupException
import org.assertj.swing.fixture.FrameFixture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import javax.swing.JLabel
import javax.swing.text.JTextComponent

abstract class IntelliJPlatformUITest : IntelliJPlatformTest() {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUpOnce() {
            FailOnThreadViolationRepaintManager.install()
        }
    }

    protected lateinit var frame: FrameFixture

    @AfterEach
    override fun tearDown() {
        if (this::frame.isInitialized) {
            frame.cleanUp()
        }

        // Add a sleep to allow the swing timers to be disposed
        Thread.sleep(2500)

        super.tearDown()
    }

    protected fun <T> executeGuiAction(query: () -> T): T {
        return GuiActionRunner.execute<T> { query() }
    }

    protected fun FrameFixture.requireContainsLabel(requiredText: String) {
        var found = false

        val textMatcher: GenericTypeMatcher<JLabel> = object : GenericTypeMatcher<JLabel>(JLabel::class.java) {
            override fun isMatching(label: JLabel): Boolean {
                if (found) {
                    return false
                }

                if (requiredText == label.text) {
                    found = true
                }

                return requiredText == label.text
            }
        }

        try {
            label(textMatcher).requireVisible()
        } catch (exception: ComponentLookupException) {
            Assertions.fail("Label with \"$requiredText\" is not present in the frame.", exception)
        }
    }

    protected fun FrameFixture.requireContainsTextBox(requiredText: String) {
        val textMatcher: GenericTypeMatcher<JTextComponent> =
            object : GenericTypeMatcher<JTextComponent>(JTextComponent::class.java) {
                override fun isMatching(label: JTextComponent) = requiredText == label.text
            }

        try {
            textBox(textMatcher).requireVisible()
        } catch (exception: ComponentLookupException) {
            Assertions.fail("Text box with \"$requiredText\" is not present in the frame.")
        }
    }
}
