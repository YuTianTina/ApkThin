package com.yutiantina.thin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.tasks.PackageApplication
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019-04-26
 */
class ThinPlugin: Plugin<Project> {
    override fun apply(p: Project) {
        p.afterEvaluate {
            val appPlugin = p.plugins.findPlugin(AppPlugin::class.java)
            appPlugin?.variantManager?.variantScopes?.forEach { variantScope ->
                val packageTaskName = variantScope.getTaskName("package")
                val pkgTask = p.tasks.findByName(packageTaskName)
                pkgTask?.doFirst {
                    with(it as PackageApplication){
                        val resourceFiles = resourceFiles.files
                        println(resourceFiles.joinToString { it -> it.absolutePath })
                        println(variantScope.fullVariantName)
                        ThinTask(resourceFiles.first(), variantScope.fullVariantName).thin()
                    }

                }
            }
        }
    }
}