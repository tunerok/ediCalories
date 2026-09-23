package com.example.edicalories.domain

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class JournalMeal(
    val calories: Int,
    val epochDay: Long,
    val minutesOfDay: Int?,
)

data class JournalWeight(
    val epochDay: Long,
    val tenthsOfKg: Int,
)

enum class JournalExportFormat {
    Json,
    Csv,
}

data class JournalDocument(
    val dailyGoal: Int,
    val groupingEnabled: Boolean,
    val breakfastStart: Int,
    val lunchStart: Int,
    val dinnerStart: Int,
    val meals: List<JournalMeal>,
    val weights: List<JournalWeight>,
) {
    fun toJson(): String {
        val builder = StringBuilder()
        builder.append("{\n")
        appendJsonField(builder, 1, "format", FORMAT_NAME, trailingComma = true)
        appendJsonField(builder, 1, "version", VERSION, trailingComma = true)
        appendJsonField(builder, 1, "dailyGoal", dailyGoal, trailingComma = true)
        appendJsonField(builder, 1, "groupingEnabled", groupingEnabled, trailingComma = true)
        appendJsonField(builder, 1, "breakfastStart", breakfastStart, trailingComma = true)
        appendJsonField(builder, 1, "lunchStart", lunchStart, trailingComma = true)
        appendJsonField(builder, 1, "dinnerStart", dinnerStart, trailingComma = true)
        appendJsonMealArray(builder, meals)
        builder.append(",\n")
        appendJsonWeightArray(builder, weights)
        builder.append("\n}\n")
        return builder.toString()
    }

    fun toCsv(): String {
        val builder = StringBuilder()
        builder.append(UTF8_BOM)
        builder.append(CSV_HEADER)
        builder.append('\n')
        appendCsvRow(
            builder = builder,
            type = CSV_TYPE_SETTINGS,
            date = "",
            time = "",
            calories = "",
            weightKg = "",
            dailyGoal = dailyGoal.toString(),
            mealGrouping = groupingEnabled.toString(),
            breakfastStart = MinutesOfDay.format(breakfastStart),
            lunchStart = MinutesOfDay.format(lunchStart),
            dinnerStart = MinutesOfDay.format(dinnerStart),
        )
        for (meal in meals) {
            val time = if (meal.minutesOfDay == null) {
                ""
            } else {
                MinutesOfDay.format(meal.minutesOfDay)
            }
            appendCsvRow(
                builder = builder,
                type = CSV_TYPE_MEAL,
                date = LocalDate.ofEpochDay(meal.epochDay).format(CSV_DATE),
                time = time,
                calories = meal.calories.toString(),
                weightKg = "",
                dailyGoal = "",
                mealGrouping = "",
                breakfastStart = "",
                lunchStart = "",
                dinnerStart = "",
            )
        }
        for (weight in weights) {
            appendCsvRow(
                builder = builder,
                type = CSV_TYPE_WEIGHT,
                date = LocalDate.ofEpochDay(weight.epochDay).format(CSV_DATE),
                time = "",
                calories = "",
                weightKg = BodyWeight.formatKg(weight.tenthsOfKg),
                dailyGoal = "",
                mealGrouping = "",
                breakfastStart = "",
                lunchStart = "",
                dinnerStart = "",
            )
        }
        return builder.toString()
    }

    companion object {
        const val FORMAT_NAME: String = "edicalories"
        const val VERSION: Int = 1
        const val MAX_BYTES: Int = 2 * 1024 * 1024
        const val MAX_MEALS: Int = 50_000
        const val MAX_WEIGHTS: Int = 50_000
        const val FILE_NAME_JSON: String = "edicalories.json"
        const val FILE_NAME_CSV: String = "edicalories.csv"

        private const val UTF8_BOM: String = "\uFEFF"
        private const val JSON_INDENT: String = "  "
        private const val CSV_TYPE_SETTINGS: String = "settings"
        private const val CSV_TYPE_MEAL: String = "meal"
        private const val CSV_TYPE_WEIGHT: String = "weight"
        private const val CSV_HEADER: String =
            "type,date,time,calories,weight_kg,daily_goal,meal_grouping,breakfast_start,lunch_start,dinner_start"
        private val CSV_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        private val ROOT_KEYS: Set<String> = setOf(
            "format",
            "version",
            "dailyGoal",
            "groupingEnabled",
            "breakfastStart",
            "lunchStart",
            "dinnerStart",
            "meals",
            "weights",
        )
        private val MEAL_KEYS: Set<String> = setOf("calories", "epochDay", "minutesOfDay")
        private val WEIGHT_KEYS: Set<String> = setOf("epochDay", "tenthsOfKg")

        fun readLimited(stream: InputStream): ByteArray? {
            val output = ByteArrayOutputStream()
            val chunk = ByteArray(8192)
            var total = 0
            while (true) {
                val count = stream.read(chunk)
                if (count < 0) {
                    break
                }
                total += count
                if (total > MAX_BYTES) {
                    return null
                }
                output.write(chunk, 0, count)
            }
            return output.toByteArray()
        }

        fun parseJson(bytes: ByteArray): JournalDocument? {
            if (bytes.size > MAX_BYTES) {
                return null
            }
            val text = decodeUtf8(bytes) ?: return null
            return parseJson(text)
        }

        fun parseJson(text: String): JournalDocument? {
            val source = if (text.startsWith(UTF8_BOM)) {
                text.substring(1)
            } else {
                text
            }
            if (source.isEmpty() || source.length > MAX_BYTES) {
                return null
            }
            return try {
                val root = JsonParser(source).parseObject()
                documentFromJson(root)
            } catch (_: ParseException) {
                null
            } catch (_: DateTimeException) {
                null
            }
        }

        private fun decodeUtf8(bytes: ByteArray): String? {
            val decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            return try {
                decoder.decode(ByteBuffer.wrap(bytes)).toString()
            } catch (_: CharacterCodingException) {
                null
            }
        }

        private fun documentFromJson(root: Map<String, JsonValue>): JournalDocument {
            if (root.keys != ROOT_KEYS) {
                throw ParseException()
            }
            val format = root.getValue("format").asString()
            if (format != FORMAT_NAME) {
                throw ParseException()
            }
            val version = root.getValue("version").asInt()
            if (version != VERSION) {
                throw ParseException()
            }
            val dailyGoal = root.getValue("dailyGoal").asInt()
            val groupingEnabled = root.getValue("groupingEnabled").asBoolean()
            val breakfastStart = root.getValue("breakfastStart").asInt()
            val lunchStart = root.getValue("lunchStart").asInt()
            val dinnerStart = root.getValue("dinnerStart").asInt()
            val meals = mealsFromJson(root.getValue("meals").asArray())
            val weights = weightsFromJson(root.getValue("weights").asArray())
            val schedule = MealSchedule(
                groupingEnabled = groupingEnabled,
                breakfastStart = breakfastStart,
                lunchStart = lunchStart,
                dinnerStart = dinnerStart,
            )
            if (dailyGoal !in CalorieBalance.MIN_DAILY_GOAL..CalorieBalance.MAX_DAILY_GOAL) {
                throw ParseException()
            }
            if (breakfastStart !in MinutesOfDay.MIN_INCLUSIVE..MinutesOfDay.MAX_INCLUSIVE ||
                lunchStart !in MinutesOfDay.MIN_INCLUSIVE..MinutesOfDay.MAX_INCLUSIVE ||
                dinnerStart !in MinutesOfDay.MIN_INCLUSIVE..MinutesOfDay.MAX_INCLUSIVE
            ) {
                throw ParseException()
            }
            if (groupingEnabled && !schedule.isValid()) {
                throw ParseException()
            }
            return JournalDocument(
                dailyGoal = dailyGoal,
                groupingEnabled = groupingEnabled,
                breakfastStart = breakfastStart,
                lunchStart = lunchStart,
                dinnerStart = dinnerStart,
                meals = meals,
                weights = weights,
            )
        }

        private fun mealsFromJson(values: List<JsonValue>): List<JournalMeal> {
            if (values.size > MAX_MEALS) {
                throw ParseException()
            }
            val meals = ArrayList<JournalMeal>(values.size)
            for (value in values) {
                val obj = value.asObject()
                if (obj.keys != MEAL_KEYS) {
                    throw ParseException()
                }
                val calories = obj.getValue("calories").asInt()
                val epochDay = obj.getValue("epochDay").asLong()
                val minutesValue = obj.getValue("minutesOfDay")
                val minutesOfDay = if (minutesValue is JsonValue.Null) {
                    null
                } else {
                    minutesValue.asInt()
                }
                if (calories !in CalorieBalance.MIN_CALORIES..CalorieBalance.MAX_CALORIES) {
                    throw ParseException()
                }
                if (minutesOfDay != null &&
                    minutesOfDay !in MinutesOfDay.MIN_INCLUSIVE..MinutesOfDay.MAX_INCLUSIVE
                ) {
                    throw ParseException()
                }
                LocalDate.ofEpochDay(epochDay)
                meals.add(
                    JournalMeal(
                        calories = calories,
                        epochDay = epochDay,
                        minutesOfDay = minutesOfDay,
                    ),
                )
            }
            return meals
        }

        private fun weightsFromJson(values: List<JsonValue>): List<JournalWeight> {
            if (values.size > MAX_WEIGHTS) {
                throw ParseException()
            }
            val seenDays = HashSet<Long>(values.size)
            val weights = ArrayList<JournalWeight>(values.size)
            for (value in values) {
                val obj = value.asObject()
                if (obj.keys != WEIGHT_KEYS) {
                    throw ParseException()
                }
                val epochDay = obj.getValue("epochDay").asLong()
                val tenthsOfKg = obj.getValue("tenthsOfKg").asInt()
                if (tenthsOfKg !in BodyWeight.MIN_TENTHS..BodyWeight.MAX_TENTHS) {
                    throw ParseException()
                }
                if (!seenDays.add(epochDay)) {
                    throw ParseException()
                }
                LocalDate.ofEpochDay(epochDay)
                weights.add(
                    JournalWeight(
                        epochDay = epochDay,
                        tenthsOfKg = tenthsOfKg,
                    ),
                )
            }
            return weights
        }

        private fun appendJsonField(
            builder: StringBuilder,
            level: Int,
            name: String,
            value: String,
            trailingComma: Boolean,
        ) {
            appendIndent(builder, level)
            builder.append('"').append(name).append("\": \"")
            builder.append(escapeJson(value)).append('"')
            if (trailingComma) {
                builder.append(",\n")
            }
        }

        private fun appendJsonField(
            builder: StringBuilder,
            level: Int,
            name: String,
            value: Long,
            trailingComma: Boolean,
        ) {
            appendIndent(builder, level)
            builder.append('"').append(name).append("\": ").append(value)
            if (trailingComma) {
                builder.append(",\n")
            }
        }

        private fun appendJsonField(
            builder: StringBuilder,
            level: Int,
            name: String,
            value: Int,
            trailingComma: Boolean,
        ) {
            appendJsonField(builder, level, name, value.toLong(), trailingComma)
        }

        private fun appendJsonField(
            builder: StringBuilder,
            level: Int,
            name: String,
            value: Boolean,
            trailingComma: Boolean,
        ) {
            appendIndent(builder, level)
            builder.append('"').append(name).append("\": ").append(value)
            if (trailingComma) {
                builder.append(",\n")
            }
        }

        private fun appendJsonMealArray(builder: StringBuilder, meals: List<JournalMeal>) {
            appendIndent(builder, 1)
            builder.append("\"meals\": ")
            if (meals.isEmpty()) {
                builder.append("[]")
                return
            }
            builder.append("[\n")
            meals.forEachIndexed { index, meal ->
                appendIndent(builder, 2)
                builder.append("{\n")
                appendJsonField(builder, 3, "calories", meal.calories, trailingComma = true)
                appendJsonField(builder, 3, "epochDay", meal.epochDay, trailingComma = true)
                appendIndent(builder, 3)
                builder.append("\"minutesOfDay\": ")
                if (meal.minutesOfDay == null) {
                    builder.append("null")
                } else {
                    builder.append(meal.minutesOfDay)
                }
                builder.append('\n')
                appendIndent(builder, 2)
                builder.append('}')
                if (index < meals.lastIndex) {
                    builder.append(',')
                }
                builder.append('\n')
            }
            appendIndent(builder, 1)
            builder.append(']')
        }

        private fun appendJsonWeightArray(builder: StringBuilder, weights: List<JournalWeight>) {
            appendIndent(builder, 1)
            builder.append("\"weights\": ")
            if (weights.isEmpty()) {
                builder.append("[]")
                return
            }
            builder.append("[\n")
            weights.forEachIndexed { index, weight ->
                appendIndent(builder, 2)
                builder.append("{\n")
                appendJsonField(builder, 3, "epochDay", weight.epochDay, trailingComma = true)
                appendJsonField(builder, 3, "tenthsOfKg", weight.tenthsOfKg, trailingComma = false)
                builder.append('\n')
                appendIndent(builder, 2)
                builder.append('}')
                if (index < weights.lastIndex) {
                    builder.append(',')
                }
                builder.append('\n')
            }
            appendIndent(builder, 1)
            builder.append(']')
        }

        private fun appendIndent(builder: StringBuilder, level: Int) {
            repeat(level) {
                builder.append(JSON_INDENT)
            }
        }

        private fun escapeJson(value: String): String {
            val builder = StringBuilder(value.length)
            for (char in value) {
                when (char) {
                    '\\' -> builder.append("\\\\")
                    '"' -> builder.append("\\\"")
                    '\n' -> builder.append("\\n")
                    '\r' -> builder.append("\\r")
                    '\t' -> builder.append("\\t")
                    else -> builder.append(char)
                }
            }
            return builder.toString()
        }

        private fun appendCsvRow(
            builder: StringBuilder,
            type: String,
            date: String,
            time: String,
            calories: String,
            weightKg: String,
            dailyGoal: String,
            mealGrouping: String,
            breakfastStart: String,
            lunchStart: String,
            dinnerStart: String,
        ) {
            appendCsvField(builder, type)
            builder.append(',')
            appendCsvField(builder, date)
            builder.append(',')
            appendCsvField(builder, time)
            builder.append(',')
            appendCsvField(builder, calories)
            builder.append(',')
            appendCsvField(builder, weightKg)
            builder.append(',')
            appendCsvField(builder, dailyGoal)
            builder.append(',')
            appendCsvField(builder, mealGrouping)
            builder.append(',')
            appendCsvField(builder, breakfastStart)
            builder.append(',')
            appendCsvField(builder, lunchStart)
            builder.append(',')
            appendCsvField(builder, dinnerStart)
            builder.append('\n')
        }

        private fun appendCsvField(builder: StringBuilder, value: String) {
            var needsQuotes = false
            for (char in value) {
                if (char == ',' || char == '"' || char == '\n' || char == '\r') {
                    needsQuotes = true
                    break
                }
            }
            if (!needsQuotes) {
                builder.append(value)
                return
            }
            builder.append('"')
            for (char in value) {
                if (char == '"') {
                    builder.append('"')
                }
                builder.append(char)
            }
            builder.append('"')
        }
    }
}

private sealed interface JsonValue {
    data class Obj(val fields: Map<String, JsonValue>) : JsonValue
    data class Arr(val items: List<JsonValue>) : JsonValue
    data class Str(val value: String) : JsonValue
    data class Num(val value: Long) : JsonValue
    data class Bool(val value: Boolean) : JsonValue
    data object Null : JsonValue

    fun asObject(): Map<String, JsonValue> {
        return (this as? Obj)?.fields ?: throw ParseException()
    }

    fun asArray(): List<JsonValue> {
        return (this as? Arr)?.items ?: throw ParseException()
    }

    fun asString(): String {
        return (this as? Str)?.value ?: throw ParseException()
    }

    fun asBoolean(): Boolean {
        return (this as? Bool)?.value ?: throw ParseException()
    }

    fun asLong(): Long {
        return (this as? Num)?.value ?: throw ParseException()
    }

    fun asInt(): Int {
        val number = asLong()
        if (number < Int.MIN_VALUE.toLong() || number > Int.MAX_VALUE.toLong()) {
            throw ParseException()
        }
        return number.toInt()
    }
}

private class ParseException : RuntimeException()

private class JsonParser(private val source: String) {
    private var pos: Int = 0

    fun parseObject(): Map<String, JsonValue> {
        val value = parseValue()
        skipWs()
        if (pos != source.length) {
            throw ParseException()
        }
        return value.asObject()
    }

    private fun parseValue(): JsonValue {
        skipWs()
        if (pos >= source.length) {
            throw ParseException()
        }
        return when (source[pos]) {
            '{' -> parseObjectValue()
            '[' -> parseArrayValue()
            '"' -> JsonValue.Str(parseString())
            't' -> {
                consume("true")
                JsonValue.Bool(true)
            }
            'f' -> {
                consume("false")
                JsonValue.Bool(false)
            }
            'n' -> {
                consume("null")
                JsonValue.Null
            }
            '-', in '0'..'9' -> parseNumber()
            else -> throw ParseException()
        }
    }

    private fun parseObjectValue(): JsonValue.Obj {
        consumeChar('{')
        skipWs()
        if (peekOrFail() == '}') {
            pos += 1
            return JsonValue.Obj(emptyMap())
        }
        val fields = LinkedHashMap<String, JsonValue>()
        while (true) {
            skipWs()
            val key = parseString()
            if (fields.containsKey(key)) {
                throw ParseException()
            }
            skipWs()
            consumeChar(':')
            fields[key] = parseValue()
            skipWs()
            when (peekOrFail()) {
                ',' -> pos += 1
                '}' -> {
                    pos += 1
                    return JsonValue.Obj(fields)
                }
                else -> throw ParseException()
            }
        }
    }

    private fun parseArrayValue(): JsonValue.Arr {
        consumeChar('[')
        skipWs()
        if (peekOrFail() == ']') {
            pos += 1
            return JsonValue.Arr(emptyList())
        }
        val items = ArrayList<JsonValue>()
        while (true) {
            items.add(parseValue())
            skipWs()
            when (peekOrFail()) {
                ',' -> pos += 1
                ']' -> {
                    pos += 1
                    return JsonValue.Arr(items)
                }
                else -> throw ParseException()
            }
        }
    }

    private fun parseString(): String {
        consumeChar('"')
        val builder = StringBuilder()
        while (pos < source.length) {
            val char = source[pos]
            pos += 1
            when (char) {
                '"' -> return builder.toString()
                '\\' -> builder.append(parseEscape())
                else -> {
                    if (char.code < 0x20) {
                        throw ParseException()
                    }
                    builder.append(char)
                }
            }
        }
        throw ParseException()
    }

    private fun parseEscape(): Char {
        if (pos >= source.length) {
            throw ParseException()
        }
        val char = source[pos]
        pos += 1
        return when (char) {
            '"', '\\', '/' -> char
            'b' -> '\b'
            'f' -> '\u000C'
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'u' -> parseHexChar()
            else -> throw ParseException()
        }
    }

    private fun parseHexChar(): Char {
        if (pos + 4 > source.length) {
            throw ParseException()
        }
        var value = 0
        repeat(4) {
            val digit = hexValue(source[pos])
            value = (value shl 4) or digit
            pos += 1
        }
        return value.toChar()
    }

    private fun hexValue(char: Char): Int {
        return when (char) {
            in '0'..'9' -> char - '0'
            in 'a'..'f' -> char - 'a' + 10
            in 'A'..'F' -> char - 'A' + 10
            else -> throw ParseException()
        }
    }

    private fun parseNumber(): JsonValue.Num {
        val start = pos
        if (peekOrFail() == '-') {
            pos += 1
        }
        if (pos >= source.length) {
            throw ParseException()
        }
        if (source[pos] == '0') {
            pos += 1
            if (pos < source.length && source[pos].isDigit()) {
                throw ParseException()
            }
        } else if (source[pos] in '1'..'9') {
            pos += 1
            while (pos < source.length && source[pos].isDigit()) {
                pos += 1
            }
        } else {
            throw ParseException()
        }
        if (pos < source.length) {
            val next = source[pos]
            if (next == '.' || next == 'e' || next == 'E') {
                throw ParseException()
            }
        }
        val raw = source.substring(start, pos)
        val value = raw.toLongOrNull() ?: throw ParseException()
        return JsonValue.Num(value)
    }

    private fun consume(expected: String) {
        if (pos + expected.length > source.length) {
            throw ParseException()
        }
        if (!source.regionMatches(pos, expected, 0, expected.length)) {
            throw ParseException()
        }
        pos += expected.length
    }

    private fun consumeChar(expected: Char) {
        if (peekOrFail() != expected) {
            throw ParseException()
        }
        pos += 1
    }

    private fun peekOrFail(): Char {
        if (pos >= source.length) {
            throw ParseException()
        }
        return source[pos]
    }

    private fun skipWs() {
        while (pos < source.length) {
            val char = source[pos]
            val whitespace = char == ' ' || char == '\n' || char == '\r' || char == '\t'
            if (!whitespace) {
                break
            }
            pos += 1
        }
    }
}
