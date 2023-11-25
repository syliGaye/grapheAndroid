package com.istic.tp.networkgt.factory

import java.io.File

interface IMailSender {
    fun send()
    fun send(to: String)
    fun send(attach: File?)
    fun send(to: String, text: String)
    fun send(to: String, attach: File?)
}