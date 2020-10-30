package io.testaxis.intellijplugin

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun createObjectMapper() = jacksonObjectMapper().apply {
    enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
}
