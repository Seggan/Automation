package io.github.seggan.automation.computing.metis

import com.google.gson.Gson
import io.github.seggan.automation.pluginInstance
import io.github.seggan.metis.runtime.Value
import io.github.seggan.metis.runtime.intrinsics.NativeLibrary
import io.github.seggan.metis.runtime.intrinsics.oneArgFunction
import io.github.seggan.metis.runtime.intrinsics.threeArgFunction
import io.github.seggan.metis.runtime.metisValue
import io.github.seggan.metis.runtime.stringValue
import java.net.URI
import java.net.URL

object LibApm : NativeLibrary("libapm") {

    private val gson = Gson()

    private val projectCache = mutableMapOf<String, URL>()

    override fun init(lib: MutableMap<String, Value>) {
        lib["getVersions"] = oneArgFunction { p ->
            val project = p.stringValue()
            val url = findProject(project) ?: return@oneArgFunction Value.Null
            val versions = URL(url, "versions.json").readText()
            val list = gson.fromJson(versions, Array<String>::class.java)
            list.map(String::metisValue).metisValue()
        }
        lib["getFile"] = threeArgFunction { p, v, f ->
            val project = p.stringValue()
            val version = v.stringValue()
            val file = f.stringValue()

            val proj = findProject(project) ?: return@threeArgFunction Value.Null
            val url = URL(proj, "$version/$file/")
            val conn = url.openConnection()
            if (conn.contentLengthLong > 0) {
                conn.inputStream.readAllBytes().metisValue()
            } else {
                Value.Null
            }
        }
    }

    private fun findProject(name: String): URL? {
        if (projectCache.containsKey(name)) {
            return projectCache[name]
        }
        for (repo in pluginInstance.apmRepos) {
            val url = URL(repo.toURL(), "$name/")
            val file = URL(url, "versions.json")
            val conn = file.openConnection()
            conn.connectTimeout = 1000
            conn.readTimeout = 1000
            if (conn.contentLengthLong > 0) {
                projectCache[name] = url
                return url
            }
        }
        return null
    }
}