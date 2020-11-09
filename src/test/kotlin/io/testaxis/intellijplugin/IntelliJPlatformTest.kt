package io.testaxis.intellijplugin

import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class IntelliJPlatformTest {
    lateinit var fixture: IdeaProjectTestFixture

    @BeforeEach
    open fun setUp() {
        fixture = createFixture(createDescriptor { registerFakes(getFakes()) })
    }

    open fun getFakes() = Fakes()

    @AfterEach
    open fun tearDown() = fixture.tearDownInEdt()
}
