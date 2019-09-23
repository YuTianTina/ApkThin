package com.yutiantina.thin

import pink.madis.apk.arsc.StringPoolChunk

/**
 *
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019-04-29
 */
fun StringPoolChunk.setString(index: Int, value: String){
    val stringsField = ShareReflectUtil.findField(this, "strings")
    val strings = stringsField.get(this) as ArrayList<String>
    strings[index] = value
    stringsField.set(this, strings)
    println("修改后的为${this.getString(index)}")
}