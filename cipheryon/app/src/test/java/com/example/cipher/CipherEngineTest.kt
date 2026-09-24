package com.example.cipher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CipherEngineTest {

    @Test
    fun testShiftChar() {
        // 'a' through 'x' shift forward 2
        assertEquals('c', CipherEngine.encryptShiftChar('a'))
        assertEquals('d', CipherEngine.encryptShiftChar('b'))
        assertEquals('y', CipherEngine.encryptShiftChar('w'))
        assertEquals('z', CipherEngine.encryptShiftChar('x'))

        // 'y' and 'z' step backward 2
        assertEquals('w', CipherEngine.encryptShiftChar('y'))
        assertEquals('x', CipherEngine.encryptShiftChar('z'))

        // Uppercase
        assertEquals('C', CipherEngine.encryptShiftChar('A'))
        assertEquals('Z', CipherEngine.encryptShiftChar('X'))
        assertEquals('W', CipherEngine.encryptShiftChar('Y'))
        assertEquals('X', CipherEngine.encryptShiftChar('Z'))

        // Non-alphabetic unaltered
        assertEquals('1', CipherEngine.encryptShiftChar('1'))
        assertEquals(' ', CipherEngine.encryptShiftChar(' '))
        assertEquals('!', CipherEngine.encryptShiftChar('!'))
    }

    @Test
    fun testUndoShift() {
        assertEquals('a', CipherEngine.decryptShiftChar('c'))
        assertEquals('b', CipherEngine.decryptShiftChar('d'))
        assertEquals('w', CipherEngine.decryptShiftChar('y'))
        assertEquals('x', CipherEngine.decryptShiftChar('z'))
        assertEquals('y', CipherEngine.decryptShiftChar('w'))
        assertEquals('z', CipherEngine.decryptShiftChar('x'))

        // Uppercase
        assertEquals('A', CipherEngine.decryptShiftChar('C'))
        assertEquals('W', CipherEngine.decryptShiftChar('Y'))
        assertEquals('X', CipherEngine.decryptShiftChar('Z'))
        assertEquals('Y', CipherEngine.decryptShiftChar('W'))
        assertEquals('Z', CipherEngine.decryptShiftChar('X'))
    }

    @Test
    fun testEncryptorModeCsprng() {
        val input = "Cipheryon 2026!"
        val encrypted1 = CipherEngine.encrypt(input, CipherMode.ENCRYPTOR)
        val encrypted2 = CipherEngine.encrypt(input, CipherMode.ENCRYPTOR)

        // Output should not be empty
        assertTrue(encrypted1.isNotEmpty())
        assertTrue(encrypted2.isNotEmpty())

        // CSRPNG random behavior: two encryptions of the same input should differ with overwhelming probability
        assertNotEquals(encrypted1, encrypted2)

        // Decrypt in Encryptor mode must return the exact error message
        val decryptResult = CipherEngine.decrypt(encrypted1, CipherMode.ENCRYPTOR)
        assertEquals(
            "Error: Encryptor permanently encrypts the input, and decryption is impossible as it uses a CSRPNG algorithm to perma-lock the output to something completely untraceable to the real input.",
            decryptResult
        )
    }

    @Test
    fun testHexadecimalEncryptDecrypt() {
        val original = "Hello World!"
        val encrypted = CipherEngine.encrypt(original, CipherMode.HEXADECIMAL)
        val segments = encrypted.split(" ")
        assertTrue(segments.isNotEmpty())
        segments.forEach { segment ->
            assertEquals(segment, segment.uppercase())
        }

        val decrypted = CipherEngine.decrypt(encrypted, CipherMode.HEXADECIMAL)
        assertEquals(original, decrypted)
    }

    @Test
    fun testBinaryEncryptDecrypt() {
        val original = "Code2"
        val encrypted = CipherEngine.encrypt(original, CipherMode.BINARY)
        val segments = encrypted.split(" ")
        assertEquals(5, segments.size)
        segments.forEach { segment ->
            assertEquals(8, segment.length)
            assertTrue(segment.all { it == '0' || it == '1' })
        }

        val decrypted = CipherEngine.decrypt(encrypted, CipherMode.BINARY)
        assertEquals(original, decrypted)
    }

    @Test
    fun testOctalEncryptDecrypt() {
        val original = "Cyber 42"
        val encrypted = CipherEngine.encrypt(original, CipherMode.OCTAL)
        val segments = encrypted.split(" ")
        assertTrue(segments.isNotEmpty())

        val decrypted = CipherEngine.decrypt(encrypted, CipherMode.OCTAL)
        assertEquals(original, decrypted)
    }

    @Test
    fun testRandomScrambleCharacteristics() {
        val input = "Hello World 123!"
        val scrambled = CipherEngine.encrypt(input, CipherMode.RANDOM_SCRAMBLE)

        assertEquals(input.length, scrambled.length)
        assertEquals(' ', scrambled[5])
        assertEquals(' ', scrambled[11])
        assertEquals('!', scrambled[15])
        assertNotEquals(input, scrambled)
    }

    @Test
    fun testRandomScrambleDecryptError() {
        val result = CipherEngine.decrypt("AnyText", CipherMode.RANDOM_SCRAMBLE)
        assertEquals(
            "Error: Random Scramble is a one-way cryptographic shredder. It cannot be decrypted!",
            result
        )
    }

    @Test
    fun testMalformedBaseDecryptionErrors() {
        // Malformed binary
        val badBinary = CipherEngine.decrypt("01000001 01000009", CipherMode.BINARY)
        assertEquals("Error: Invalid data format for selection!", badBinary)

        val nonBinary = CipherEngine.decrypt("hello world", CipherMode.BINARY)
        assertEquals("Error: Invalid data format for selection!", nonBinary)

        // Malformed octal
        val badOctal = CipherEngine.decrypt("77 88 99", CipherMode.OCTAL)
        assertEquals("Error: Invalid data format for selection!", badOctal)

        // Malformed hex
        val badHex = CipherEngine.decrypt("41 42 GG ZZ", CipherMode.HEXADECIMAL)
        assertEquals("Error: Invalid data format for selection!", badHex)
    }
}
