package com.yutiantina.thin

import java.io.File

/**
 *
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019-04-28
 */
data class DuplicationEntry (
    var fileMd5: String,
    var file: File
)