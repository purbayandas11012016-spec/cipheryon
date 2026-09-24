package com.example.cipher

import java.security.SecureRandom

/**
 * Core mathematical engine for "Cipheryon" programmer's cipher and encryption utility.
 */
enum class CipherMode(val displayName: String) {
    HEXADECIMAL("Hexadecimal"),
    BINARY("Binary"),
    OCTAL("Octal"),
    ENCRYPTOR("Encryptor"),
    RANDOM_SCRAMBLE("Random Scramble");

    companion object {
        fun fromDisplayName(name: String): CipherMode {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: HEXADECIMAL
        }
    }
}

object CipherEngine {

    const val ERROR_RANDOM_SCRAMBLE_DECRYPT =
        "Error: Random Scramble is a one-way cryptographic shredder. It cannot be decrypted!"
    const val ERROR_INVALID_DATA_FORMAT =
        "Error: Invalid data format for selection!"
    const val ENCRYPTOR_PERMA_LOCK_MESSAGE =
        "Encryptor permanently encrypts the input, and decryption is impossible as it uses a CSRPNG algorithm to perma-lock the output to something completely untraceable to the real input."

    private const val SCRAMBLE_POOL =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private const val SCRAMBLE_MULTIPLIER = 987654321L

    // Cryptographically Secure Pseudo-Random Number Generator (CSRPNG)
    private val csprng = SecureRandom()

    // Dynamic pool constructed without hardcoded strings from character, symbol, emoji and number ranges
    private val DYNAMIC_CSPRNG_POOL: List<String> = buildList {
        // Uppercase & Lowercase Characters
        for (c in 'A'..'Z') add(c.toString())
        for (c in 'a'..'z') add(c.toString())
        // Numbers
        for (c in '0'..'9') add(c.toString())
        // ASCII Symbols & Punctuation
        for (code in 33..126) {
            val c = code.toChar()
            if (!c.isLetterOrDigit()) add(c.toString())
        }
        // Mathematical and Technical Symbols
        for (code in 0x2200..0x2260 step 2) {
            add(String(Character.toChars(code)))
        }
        // Cryptographic, security, tech and cosmic Emojis
        val emojiRanges = listOf(
            0x1F300..0x1F320, // Cosmos, stars, nature
            0x1F50F..0x1F530, // Locks, keys, shields, cyber
            0x1F680..0x1F698, // Tech, rockets
            0x1F910..0x1F92F  // Tech, cybernetic symbols
        )
        for (range in emojiRanges) {
            for (code in range) {
                add(String(Character.toChars(code)))
            }
        }
    }

    /**
     * Character shifting layer for base encodings:
     * - Letters 'a' through 'x' (case-insensitive) shift forward exactly 2 character positions.
     * - Letters 'y' and 'z' step backward exactly 2 positions (mapping y ⇄ w and z ⇄ x seamlessly).
     * - Non-alphanumeric text (spaces, syntax punctuation) remains unaltered.
     */
    fun encryptShiftChar(c: Char): Char {
        return when (c) {
            in 'a'..'x' -> c + 2
            'y' -> 'w'
            'z' -> 'x'
            in 'A'..'X' -> c + 2
            'Y' -> 'W'
            'Z' -> 'X'
            else -> c
        }
    }

    /**
     * Undoes the character shifting layer:
     */
    fun decryptShiftChar(c: Char): Char {
        return when (c) {
            'w' -> 'y'
            'x' -> 'z'
            'y' -> 'w'
            'z' -> 'x'
            in 'c'..'v' -> c - 2
            'a' -> 'y'
            'b' -> 'z'
            'W' -> 'Y'
            'X' -> 'Z'
            'Y' -> 'W'
            'Z' -> 'X'
            in 'C'..'V' -> c - 2
            'A' -> 'Y'
            'B' -> 'Z'
            else -> c
        }
    }

    fun applyShiftLayer(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(encryptShiftChar(c))
        }
        return sb.toString()
    }

    fun undoShiftLayer(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(decryptShiftChar(c))
        }
        return sb.toString()
    }

    /**
     * CSRPNG-driven Encryptor:
     * Uses a Cryptographically Secure Pseudo-Random Number Generator (CSRPNG)
     * to randomly select what each character will be transformed into among
     * characters, symbols, emojis, and numbers.
     */
    fun encryptWithCsprng(input: String): String {
        val sb = StringBuilder()
        for (char in input) {
            val randomIndex = csprng.nextInt(DYNAMIC_CSPRNG_POOL.size)
            sb.append(DYNAMIC_CSPRNG_POOL[randomIndex])
        }
        return sb.toString()
    }

    /**
     * Random Scramble Module:
     * A high-entropy mathematical shredder processing alphanumeric text against a constant
     * lookup pool array containing 62 characters (abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789).
     * val dynamicSeed = (char.code * (i + 1) * 987654321L) % 62.
     */
    fun encryptRandomScramble(input: String): String {
        val sb = StringBuilder(input.length)
        input.forEachIndexed { i, char ->
            if (char.isLetterOrDigit()) {
                val dynamicSeed = (char.code.toLong() * (i + 1) * SCRAMBLE_MULTIPLIER) % 62
                val index = ((dynamicSeed % 62 + 62) % 62).toInt()
                sb.append(SCRAMBLE_POOL[index])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    /**
     * Primary Encryption dispatcher based on selected mode.
     */
    fun encrypt(input: String, mode: CipherMode): String {
        if (input.isEmpty()) return ""

        return when (mode) {
            CipherMode.ENCRYPTOR -> {
                encryptWithCsprng(input)
            }
            CipherMode.BINARY -> {
                val shifted = applyShiftLayer(input)
                shifted.map { char ->
                    char.code.toString(2).padStart(8, '0')
                }.joinToString(" ")
            }
            CipherMode.OCTAL -> {
                val shifted = applyShiftLayer(input)
                shifted.map { char ->
                    char.code.toString(8)
                }.joinToString(" ")
            }
            CipherMode.HEXADECIMAL -> {
                val shifted = applyShiftLayer(input)
                shifted.map { char ->
                    char.code.toString(16).uppercase().padStart(2, '0')
                }.joinToString(" ")
            }
            CipherMode.RANDOM_SCRAMBLE -> {
                encryptRandomScramble(input)
            }
        }
    }

    /**
     * Primary Decryption dispatcher with exception handling and defense rules.
     */
    fun decrypt(input: String, mode: CipherMode): String {
        if (input.isEmpty()) return ""

        if (mode == CipherMode.ENCRYPTOR) {
            return "Error: $ENCRYPTOR_PERMA_LOCK_MESSAGE"
        }

        if (mode == CipherMode.RANDOM_SCRAMBLE) {
            return ERROR_RANDOM_SCRAMBLE_DECRYPT
        }

        return try {
            when (mode) {
                CipherMode.ENCRYPTOR -> {
                    "Error: $ENCRYPTOR_PERMA_LOCK_MESSAGE"
                }
                CipherMode.BINARY -> {
                    val segments = input.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                    if (segments.isEmpty()) return ""
                    val parsedChars = segments.map { segment ->
                        if (segment.any { it != '0' && it != '1' }) {
                            throw IllegalArgumentException("Malformed binary segment: $segment")
                        }
                        segment.toInt(2).toChar()
                    }.joinToString("")
                    undoShiftLayer(parsedChars)
                }
                CipherMode.OCTAL -> {
                    val segments = input.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                    if (segments.isEmpty()) return ""
                    val parsedChars = segments.map { segment ->
                        if (segment.any { it !in '0'..'7' }) {
                            throw IllegalArgumentException("Malformed octal segment: $segment")
                        }
                        segment.toInt(8).toChar()
                    }.joinToString("")
                    undoShiftLayer(parsedChars)
                }
                CipherMode.HEXADECIMAL -> {
                    val segments = input.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                    if (segments.isEmpty()) return ""
                    val parsedChars = segments.map { segment ->
                        if (segment.any { !it.isDigit() && it !in 'a'..'f' && it !in 'A'..'F' }) {
                            throw IllegalArgumentException("Malformed hex segment: $segment")
                        }
                        segment.toInt(16).toChar()
                    }.joinToString("")
                    undoShiftLayer(parsedChars)
                }
                CipherMode.RANDOM_SCRAMBLE -> {
                    ERROR_RANDOM_SCRAMBLE_DECRYPT
                }
            }
        } catch (e: Exception) {
            ERROR_INVALID_DATA_FORMAT
        }
    }
}
