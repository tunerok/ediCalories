package com.example.edicalories.domain

object BodyWeight {
    const val MIN_TENTHS: Int = 200
    const val MAX_TENTHS: Int = 4000
    const val TENTHS_PER_KG: Int = 10
    private const val MAX_WHOLE_DIGITS: Int = 3
    private const val MAX_FRACTION_DIGITS: Int = 1

    fun parseToTenths(raw: String): Int? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val normalized = trimmed.replace(',', '.')
        val separatorIndex = normalized.indexOf('.')
        if (separatorIndex != normalized.lastIndexOf('.')) {
            return null
        }
        val wholeRaw: String
        val fractionRaw: String
        if (separatorIndex < 0) {
            wholeRaw = normalized
            fractionRaw = ""
        } else {
            wholeRaw = normalized.substring(0, separatorIndex)
            fractionRaw = normalized.substring(separatorIndex + 1)
        }
        if (wholeRaw.isEmpty() || !wholeRaw.all { char -> char.isDigit() }) {
            return null
        }
        if (fractionRaw.isNotEmpty() &&
            (fractionRaw.length != MAX_FRACTION_DIGITS || !fractionRaw[0].isDigit())
        ) {
            return null
        }
        val whole = wholeRaw.toIntOrNull() ?: return null
        if (whole > MAX_TENTHS / TENTHS_PER_KG) {
            return null
        }
        val fraction = if (fractionRaw.isEmpty()) {
            0
        } else {
            fractionRaw[0] - '0'
        }
        val tenths = (whole * TENTHS_PER_KG) + fraction
        if (tenths < MIN_TENTHS || tenths > MAX_TENTHS) {
            return null
        }
        return tenths
    }

    fun formatKg(tenths: Int): String {
        val whole = tenths / TENTHS_PER_KG
        val fraction = tenths % TENTHS_PER_KG
        if (fraction == 0) {
            return whole.toString()
        }
        return "$whole.$fraction"
    }

    fun toKg(tenths: Int): Float {
        return tenths.toFloat() / TENTHS_PER_KG.toFloat()
    }

    fun sanitizeInput(raw: String): String {
        val builder = StringBuilder(raw.length)
        var separatorUsed = false
        var wholeDigits = 0
        var fractionDigits = 0
        for (char in raw) {
            val isSeparator = char == '.' || char == ','
            if (char.isDigit()) {
                if (separatorUsed) {
                    if (fractionDigits >= MAX_FRACTION_DIGITS) {
                        continue
                    }
                    fractionDigits += 1
                    builder.append(char)
                } else {
                    if (wholeDigits >= MAX_WHOLE_DIGITS) {
                        continue
                    }
                    wholeDigits += 1
                    builder.append(char)
                }
            } else if (isSeparator && !separatorUsed && wholeDigits > 0) {
                separatorUsed = true
                builder.append(char)
            }
        }
        return builder.toString()
    }
}
