package com.jcaa.notifier.application.service;

/** Asunto y cuerpo HTML ya armados para un correo. */
public record EmailMessage(String subject, String body) {}