package io.testaxis.intellijplugin

import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class IntelliJPlatformTest {
    private lateinit var fixture: IdeaProjectTestFixture

    @BeforeEach
    fun setUp() {
        fixture = createFixture(createDescriptor { registerFakes(Fakes()) })
    }

    @AfterEach
    fun tearDown() = fixture.tearDownInEdt()
}
