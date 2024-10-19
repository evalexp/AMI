import org.w3c.dom.Element
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "top.evalexp.ami"
version = "1.0.0"
var mainClass = "top.evalexp.ami.Main"

repositories {
    mavenCentral()
}

dependencies {
//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(files("libs/mytools.jar"))
    implementation("org.javassist:javassist:3.30.2-GA")
    implementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    implementation("javax.servlet:javax.servlet-api:4.0.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = mainClass
        attributes["Agent-Class"] = mainClass
        attributes["Can-Redefine-Classes"] = "true"
        attributes["Can-Retransform-Classes"] = "true"
        attributes["Implementation-Version"] = archiveVersion
    }
}

tasks.shadowJar {
    dependencies {
        exclude(dependency("jakarta.servlet:jakarta.servlet-api:5.0.0"))
        exclude(dependency("javax.servlet:javax.servlet-api:4.0.1"))
    }
}

tasks.register("generateObfConfig") {
    group = "obf"
    description = "Generate Allatori OBF configuration"
    dependsOn(tasks.shadowJar)
    val configFile = file("${project.rootDir}/allatori/config/allatori.xml")
    outputs.file(configFile)

    doLast {
        val outFile = tasks.shadowJar.get().archiveFile.get().asFile.path
        val obfOutFile = tasks.shadowJar.get().archiveFile.get().asFile.parentFile.path + File.separator + "obf-" + tasks.shadowJar.get().archiveFile.get().asFile.name
        if (!configFile.exists()) {
            println("Config file not created, create it from template...")
            val obfTemplate = file("${project.rootDir}/allatori/config/allatori_template.xml").readText()
            val obfConfig = obfTemplate.replace("\${infile}", outFile).replace("\${outfile}", obfOutFile)
            file("${project.rootDir}/allatori/config/allatori.xml").writeText(obfConfig)
        }
    }
}

tasks.register("obf-shadowJar") {
    group = "obf"
    description = "Obfuscate built jar"
    dependsOn(tasks.getByName("generateObfConfig"))
    doLast {
        javaexec {
            mainClass.set("-jar")
            args = listOf("${project.rootDir}/allatori/allatori.jar", "${project.rootDir}/allatori/config/allatori.xml")
        }

    }
}

tasks.register("obf") {
    group = "obf"
    description = "Update obfuscated jar manifest"
    dependsOn(tasks.getByName("obf-shadowJar"))
    doLast {
        val xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file("${project.rootDir}/allatori/config/allatori-log.xml"))
        xmlDocument.documentElement.normalize()

        val nodeList = xmlDocument.getElementsByTagName("class")
        var mainClassNewName: String = ""
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node.nodeType == Element.ELEMENT_NODE) {
                val element = node as Element
                val old = element.getAttribute("old")
                if (old == mainClass) {
                    mainClassNewName = element.getAttribute("new")
                    break
                }
            }
        }
        if (mainClassNewName.isNotEmpty()) {
            val jarFile = file(tasks.shadowJar.get().archiveFile.get().asFile.parentFile.path + File.separator + "obf-" + tasks.shadowJar.get().archiveFile.get().asFile.name)
            val tempFile = file("${project.rootDir}/build/.obf.temp.jar")

            JarFile(jarFile).use { jar ->
                val manifest = jar.manifest
                println("Updating manifest...")
                println("Main Class change to $mainClassNewName")
                manifest.mainAttributes.putValue("Main-Class", mainClassNewName)
                manifest.mainAttributes.putValue("Agent-Class", mainClassNewName)

                JarOutputStream(FileOutputStream(tempFile), manifest).use { out ->
                    for (jarEntry in jar.entries().iterator()) {
                        if (jarEntry.name != "META-INF/MANIFEST.MF") {
                            val outEntry = JarEntry(jarEntry.name)
                            outEntry.time = jarEntry.time
                            outEntry.extra = jarEntry.extra
                            outEntry.comment = jarEntry.comment
                            out.putNextEntry(outEntry)
                            jar.getInputStream(jarEntry).copyTo(out)
                            out.closeEntry()
                        }
                    }
                }

                if (jarFile.delete()) {
                    tempFile.renameTo(jarFile)
                    println("Update manifest successfully.")
                } else {
                    error("Failed to update manifest.")
                }
            }
        } else {
            error("Allatori log can't not found main class renaming")
        }
    }
}
