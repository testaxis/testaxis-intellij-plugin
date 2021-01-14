@file:Suppress("MatchingDeclarationName", "ClassNaming")

package io.testaxis.intellijplugin

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.Location
import com.natpryce.konfig.Misconfiguration
import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.getValue
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import java.io.File
import java.util.Properties

/**
 * Type-safe parallel structure of the configuration file.
 *
 * Usage example:
 * <code>
 *     config(config.testaxis.api.url)
 * </code>
 */
object config {
    object testaxis : PropertyGroup() {
        val defaultHost by stringType

        object api : PropertyGroup() {
            val url by stringType
        }

        object ws : PropertyGroup() {
            val host by stringType
            val port by intType
            val endpoint by stringType

            object topics : PropertyGroup() {
                val builds by stringType
            }
        }
    }
}

/**
 * Exposes configuration value retrieval as a function.
 */
fun <T> config(key: Key<T>): T = configuration[key]

private val configuration = systemProperties() overriding
    EnvironmentVariables() overriding
    ConfigurationProperties.fromOptionalFile(File("testaxis.properties")) overriding
    ConfigurationProperties.fromResourceWithCustomClassLoader("application.properties")

/**
 * Extension function to allow using a custom class loader.
 *
 * The custom class loader is needed to support the class loading infrastructure of the IntelliJ platform.
 * Since the default implementation is made private, there is some copy/pasted code here from the library.
 */
fun ConfigurationProperties.Companion.fromResourceWithCustomClassLoader(resourceName: String): ConfigurationProperties {
    val resourceUrl = this::class.java.classLoader.getResource(resourceName)

    return (resourceUrl?.openStream() ?: throw Misconfiguration("resource $resourceName not found")).use {
        ConfigurationProperties(
            Properties().apply { load(it) },
            Location("resource $resourceName", resourceUrl.toURI())
        )
    }
}
