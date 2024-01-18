package org.looko.picview.controller

import org.looko.mycloudenhance.libcommon.annotation.NoGlobalCatch
import org.looko.mycloudenhance.libcommon.enumeration.CommonResponseStatus.BUSINESS_EXCEPTION
import org.looko.mycloudenhance.libcommon.enumeration.CommonResponseStatus.OK
import org.looko.mycloudenhance.libcommon.exception.BusinessException
import org.looko.mycloudenhance.libcommon.model.CommonResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File

@RestController
@RequestMapping("/picview")
class PicViewController {

    @Value("\${custom.library-directory}")
    private lateinit var libraryDirectory: String

    @GetMapping("/directory")
    fun getDirectory(): ResponseEntity<CommonResponse<*>> {
        return ResponseEntity.ok(CommonResponse(OK, libraryDirectory))
    }

    @PostMapping("/directory")
    fun changeDirectory(@RequestParam(required = true) newDirectory: String): ResponseEntity<CommonResponse<*>> {
        val newDirectoryFile = File(newDirectory)
        if (newDirectoryFile.exists() && newDirectoryFile.canRead()) {

            libraryDirectory = newDirectory
            return ResponseEntity.ok(CommonResponse(OK, newDirectory))
        }
        throw BusinessException("路径不存在或不可读!")
    }

    @GetMapping("/books")
    fun getBooks(): ResponseEntity<CommonResponse<*>> {
        val library = File(libraryDirectory)
        return ResponseEntity.ok(
            if (library.exists())
                CommonResponse(OK, library.list { _, name -> !name.startsWith(".") })
            else
                CommonResponse(BUSINESS_EXCEPTION, "无图书")
        )
    }

    @GetMapping("/chapters")
    fun getChapters(@RequestParam(required = true) book: String): ResponseEntity<CommonResponse<*>> {
        val bookFile = File("$libraryDirectory/$book/original")
        return ResponseEntity.ok(
            if (bookFile.exists())
                CommonResponse(OK, bookFile.list { _, name -> !name.startsWith(".") })
            else
                CommonResponse(BUSINESS_EXCEPTION, "无章节")
        )
    }

    @GetMapping("/chapters2x")
    fun getChapters2x(@RequestParam(required = true) book: String): ResponseEntity<CommonResponse<*>> {
        val bookFile = File("$libraryDirectory/$book/waifu2x")
        return ResponseEntity.ok(
            if (bookFile.exists())
                CommonResponse(OK, bookFile.list { _, name -> !name.startsWith(".") })
            else
                CommonResponse(BUSINESS_EXCEPTION, "无章节")
        )
    }

    @GetMapping("/pages")
    fun getPages(@RequestParam(required = true) book: String,
                 @RequestParam(required = false) chapter: String?): ResponseEntity<CommonResponse<*>> {
        val chapterFile = File("$libraryDirectory/$book/original${chapter?.let { "/$it" } ?: ""}")
        return ResponseEntity.ok(
            if (chapterFile.exists())
                chapterFile.list { _, name -> !name.startsWith(".") }?.let { CommonResponse(OK, it.sorted()) }
            else
                CommonResponse(BUSINESS_EXCEPTION, "无页面")
        )
    }

    @GetMapping("/pages2x")
    fun getPages2x(@RequestParam(required = true) book: String,
                   @RequestParam(required = false) chapter: String?): ResponseEntity<CommonResponse<*>> {
        val chapterFile = File("$libraryDirectory/$book/waifu2x${chapter?.let { "/$it" } ?: ""}")
        return ResponseEntity.ok(
            if (chapterFile.exists())
                CommonResponse(OK, chapterFile.list { _, name -> !name.startsWith(".") })
            else
                CommonResponse(BUSINESS_EXCEPTION, "无页面")
        )
    }

    @NoGlobalCatch
    @GetMapping("/cover")
    fun getCoverFile(@RequestParam(value = "book", required = true) book: String): ResponseEntity<FileSystemResource> {
        val bookFile = File("$libraryDirectory/$book/original")
        if (bookFile.exists()) {
            bookFile
                .listFiles { _, name -> !name.startsWith(".") }
                ?.firstOrNull()
                ?.listFiles()
                ?.minOrNull()
                ?.let {
                    return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(FileSystemResource(it))
                }
        }
        return ResponseEntity.notFound().build()
    }

    @NoGlobalCatch
    @GetMapping("/page")
    fun getPageFile(@RequestParam(required = true) book: String,
                    @RequestParam(required = false) chapter: String?,
                    @RequestParam(required = true) page: String): ResponseEntity<FileSystemResource> {
        val imageFile = File("$libraryDirectory/$book/original/${chapter?.let { "/$it" } ?: ""}/$page")
        if (imageFile.exists()) {
            val resource = FileSystemResource(imageFile)
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource)
        } else {
            return ResponseEntity.notFound().build()
        }
    }

    @NoGlobalCatch
    @GetMapping("/page2x")
    fun getPageFile2x(@RequestParam(required = true) book: String,
                      @RequestParam(required = false) chapter: String?,
                      @RequestParam(required = true) page: String): ResponseEntity<FileSystemResource> {
        val imageFile = File("$libraryDirectory/$book/waifu2x/${chapter?.let { "/$it" } ?: ""}/$page")
        if (imageFile.exists()) {
            val resource = FileSystemResource(imageFile)
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource)
        } else {
            return ResponseEntity.notFound().build()
        }
    }

}