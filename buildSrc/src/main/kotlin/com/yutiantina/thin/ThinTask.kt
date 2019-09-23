package com.yutiantina.thin

import com.android.utils.FileUtils
import org.apache.commons.io.IOUtils
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import java.io.*
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 *
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019-04-26
 */
class ThinTask(val out: File, val variantName: String){

    companion object{
        const val ARSC_NAME = "resources.arsc"
        const val UNZIP_FILE_NAME = "unzipresource"
    }

    /**
     * key md5
     * value 对应保留的重复资源路径
     */
    var duplicateMap = HashMap<String, String>()
    /**
     * 被删除的重复资源
     */
    val delDuplicateMap = HashMap<String, String>()
    var unzipBak: File? = null
    var apFile: File? = null

    fun thin(){
        apFile = FileUtils.join(out, "resources-$variantName.ap_")
        unzipBak = unzip(apFile!!)
        computeMd5(unzipBak)
        modifyArsc()
        replaceResource()
    }

    /**
     * bak压缩并替换
     */
    private fun replaceResource() {
        val bakZipFile = File(out, "bak.zip")
        val zos = ZipOutputStream(bakZipFile.outputStream())
        unzipBak?.walkTopDown()
            ?.filter { it.absolutePath != unzipBak?.absolutePath }
            ?.forEach {
//                println("压缩entry ${it.absolutePath}, entryName ${FileUtils.relativePath(it, unzipBak)}")
                zos.putNextEntry(ZipEntry(FileUtils.relativePath(it, unzipBak)))
                if(!it.isDirectory){
                    zos.write(it.readBytes())
                }
            }
        bakZipFile.renameTo(apFile!!)
        unzipBak?.delete()
        zos.flush()
        zos.close()
        bakZipFile.delete()
    }

    /**
     * 修改arsc, 进行重定向处理
     */
    private fun modifyArsc() {
        val arscFile = File(unzipBak, ARSC_NAME)
        if(!arscFile.exists()){
            return
        }
        val arscStream = arscFile.inputStream()
        val resourceFile = ResourceFile.fromInputStream(arscStream)
        val chunks = resourceFile.chunks
        chunks
            .forEach {
              if(it is ResourceTableChunk){
                  val stringPoolChunk = it.stringPool
                  for (i in 0 until stringPoolChunk.stringCount){
                      val key = stringPoolChunk.getString(i)
//                      println("资源项名称字符串池包括key $key")
                      if(delDuplicateMap.containsKey(key)){
                          // arsc的资源池内找到对应重复已被删除的字段
                          println("arsc的资源池内找到对应重复已被删除的字段$key")
                          stringPoolChunk.setString(i, delDuplicateMap[key]!!)
                      }
                  }
              }
            }
        arscFile.delete()
        arscFile.writeBytes(resourceFile.toByteArray())
    }

    fun computeMd5(file: File?){
        if(null == file){
            return
        }
        for (itemFile in file.walkTopDown().iterator()) {
            if(itemFile.isDirectory){
                // nothing
            }else{
                val msgDigest = MessageDigest.getInstance("MD5")
                val inputStream = BufferedInputStream(FileInputStream(itemFile))
                val buffer = ByteArray(512)
                var readSize = inputStream.read(buffer)
                var totalRead: Long = 0
                while (readSize > 0) {
                    msgDigest.update(buffer, 0, readSize)
                    totalRead += readSize.toLong()
                    readSize = inputStream.read(buffer)
                }
                inputStream.close()
                if (totalRead > 0) {
                    val md5 = Util.byteArrayToHex(msgDigest.digest())
                    val fileName = FileUtils.relativePath(itemFile, unzipBak)
                    if(duplicateMap.containsKey(md5)){
                        println("拥有重复资源, 删除 $fileName 保留 ${duplicateMap[md5]}")
                        itemFile.delete()
                        delDuplicateMap[fileName] = duplicateMap[md5]!!
                    }else{
                        duplicateMap[md5] = fileName
                    }
                }
            }
        }
    }

    private fun unzip(oriFile: File): File{
        val zipFile = ZipFile(oriFile)
        val bak = File(oriFile.parent, UNZIP_FILE_NAME)
        if(bak.exists()){
            bak.delete()
        }
        bak.mkdir()
        for (entry in zipFile.entries()) {
            if(entry.isDirectory){
                val directory = FileUtils.join(bak, entry.name.split(File.separator))
                directory.mkdirs()
            }else{
                val unzipFile = FileUtils.join(bak, entry.name.split(File.separator))
                if(!unzipFile.parentFile.exists()){
                    unzipFile.parentFile.mkdirs()
                }
                unzipFile.writeBytes(IOUtils.toByteArray(zipFile.getInputStream(entry)))
            }
        }
        return bak
    }

}