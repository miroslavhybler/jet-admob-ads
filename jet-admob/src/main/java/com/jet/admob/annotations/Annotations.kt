package com.jet.admob.annotations


/**
 * Marks declarations that are still **experimental** in the jet-admob library and can change in the
 * future.
 * @author Miroslav Hýbler <br>
 * created on 16.01.2026
 * @since 1.0.0
 */
@RequiresOptIn(
    message = "This API is experimental and may change in the future.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(value = AnnotationRetention.BINARY)
@Target(
    allowedTargets = [
        AnnotationTarget.CLASS,
        AnnotationTarget.FUNCTION,
    ]
)
annotation class JetAdMobAlpha
