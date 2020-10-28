package io.testaxis.intellijplugin

import org.ocpsoft.prettytime.PrettyTime
import java.util.Date
import java.util.Locale

fun Date.diffForHumans(): String = PrettyTime(Locale.ENGLISH).format(this)
