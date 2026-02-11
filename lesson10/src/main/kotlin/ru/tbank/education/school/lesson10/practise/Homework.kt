package ru.tbank.education.school.lesson10.practise

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


/*
### Задание - проверка расписания

**Сюжет.** Диспетчер космостанции получает журнал событий от робота-курьера. После обновления прошивки логи начали приходить в разных форматах. Нужно привести журнал к единому виду, посчитать время доставки каждой посылки и найти нарушения.

**Входные данные.** Дан список строк `logs`. Каждая строка — одно событие по посылке (ID) со статусом `sent` или `delivered`. Время — с точностью до минут. Строки могут быть в любом порядке.

Логи приходят в одном из трёх форматов.

**Формат A:**
`"2026-01-22 09:14 | ID:042 | STATUS:sent"`

**Формат B:**
`"TS=22/01/2026-09:27; status=delivered; #042"`

**Формат C:**
`"[22.01.2026 09:40] delivered (id:044)"`

Также возможны:

* лишние пробелы (в начале/конце, вокруг разделителей),
* разный регистр (`STATUS`, `status`; `Sent`, `SENT`).

**Требование:** для распознавания форматов и извлечения полей использовать регулярные выражения.

#### Часть 1. Нормализация логов

1. Написать функцию `normalize(line)`, которая принимает строку `line` и возвращает нормализованную запись с полями:

* `dt`: дата-время в виде `"YYYY-MM-DD HH:MM"`
* `id`: целое число (например `42`)
* `status`: строка `"sent"` или `"delivered"` в нижнем регистре

Пример результата:
`{"dt": "2026-01-22 09:27", "id": 42, "status": "delivered"}`

2. Если строка не подходит ни под один формат, функция должна пометить её как «битую» (например вернуть `None`/ошибку/код состояния — как решите) и не ломать программу.

3. Применить `normalize` ко всем строкам из `logs` и получить:

* список нормализованных записей,
* список битых строк (если есть).

#### Часть 2. Расчёт времени доставки

4. Сгруппировать события по `id`. Для каждого `id` найти:

* время отправки (`status = sent`)
* время доставки (`status = delivered`)

5. Для каждого `id` посчитать длительность доставки в минутах: `delivered - sent`.

Условия:

* Если для `id` не хватает одного из событий (`sent` или `delivered`), такой `id` считать «неполным» и вывести отдельно.
* Если `delivered` раньше `sent`, считать это ошибкой времени и вывести отдельно.

#### Часть 3. Отчёт

6. Вывести:

* список всех `id` с длительностью доставки (`id`, минуты), отсортированный по убыванию длительности,
* самый долгий заказ (`id` и минуты),
* список нарушителей правила: доставка должна быть не дольше 20 минут.

#### Дополнительно

A) Устойчивость к «грязным» данным: сделать так, чтобы `normalize` принимал строки вида
`" ts=22/01/2026-09:05; STATUS=Sent; #044  "`
`" [22.01.2026 09:33]   DELIVERED   (ID:045) "`

B) Сводка по часам: посчитать, в какой час суток (00–23) произошло больше всего событий `delivered`.

C) Детектор дублей: если для одного `id` встречается несколько `sent` или несколько `delivered`, вывести такие `id` и количество дублей по каждому статусу.

**Тестовые данные (смешанные форматы):**

```text
logs = [
"2026-01-22 09:14 | ID:042 | STATUS:sent",
"TS=22/01/2026-09:27; status=delivered; #042",
"2026-01-22 09:10 | ID:043 | STATUS:sent",
"2026-01-22 09:18 | ID:043 | STATUS:delivered",
"TS=22/01/2026-09:05; status=sent; #044",
"[22.01.2026 09:40] delivered (id:044)",
"2026-01-22 09:20 | ID:045 | STATUS:sent",
"[22.01.2026 09:33] delivered (id:045)",
"   ts=22/01/2026-09:50; STATUS=Sent; #046   ",
" [22.01.2026 10:05]   DELIVERED   (ID:046) "
]
```
*/

data class LogResponse(   // Для ответов после нормализации
    val dt: String,
    val id: Int,
    val status: String
)

data class DeliveryResult(   // Итоговый ответ
    val id: Int,
    val durationMinutes: Int,
    val isLate: Boolean
)

fun createResponse(year: String, month: String, day: String, hours: String, minutes: String, id: String, status: String): LogResponse {
    val currentDate: String = "${year}-${month}-${day} ${hours}:${minutes}"
    val currentId: Int = id.toInt()
    val currentStatus: String = status.lowercase()
    return LogResponse(currentDate, currentId, currentStatus)
}

fun normalize(log: String): LogResponse? {
    val currentLog = log.lowercase().replace(" ", "")

    // Формат A: "2026-01-22 09:14 | ID:042 | STATUS:sent"
    val patternA = Regex("""(\d{4})-(\d{2})-(\d{2})(\d{2}):(\d{2})\|id:(\d+)\|status:(sent|delivered)""")

    // Формат B: "TS=22/01/2026-09:27; status=delivered; #042"
    val patternB = Regex("""ts=(\d{2})/(\d{2})/(\d{4})-(\d{2}):(\d{2});status=(sent|delivered);#(\d+)""")

    // Формат C: "[22.01.2026 09:40] delivered (id:044)"
    val patternC = Regex("""\[(\d{2})\.(\d{2})\.(\d{4})(\d{2}):(\d{2})\](sent|delivered)\(id:(\d+)\)""")

    var m: MatchResult? = patternA.matchEntire(currentLog)
    if (m != null) {
        val year = m.groupValues[1]
        val month = m.groupValues[2]
        val day = m.groupValues[3]
        val hours = m.groupValues[4]
        val minutes = m.groupValues[5]
        val id = m.groupValues[6]
        val status = m.groupValues[7]
        return createResponse(year, month, day, hours, minutes, id, status)
    }

    m = patternB.matchEntire(currentLog)
    if (m != null) {
        val year = m.groupValues[3]
        val month = m.groupValues[2]
        val day = m.groupValues[1]
        val hours = m.groupValues[4]
        val minutes = m.groupValues[5]
        val id = m.groupValues[7]
        val status = m.groupValues[6]
        return createResponse(year, month, day, hours, minutes, id, status)
    }

    m = patternC.matchEntire(currentLog)
    if (m != null) {
        val year = m.groupValues[3]
        val month = m.groupValues[2]
        val day = m.groupValues[1]
        val hours = m.groupValues[4]
        val minutes = m.groupValues[5]
        val id = m.groupValues[7]
        val status = m.groupValues[6]
        return createResponse(year, month, day, hours, minutes, id, status)
    }

    return null
}

fun main() {
    val logs = mutableListOf<String>()
    while (true) {
        val inputLine: String? = readlnOrNull()
        if (inputLine == null) {
            break
        } else {
            logs.add(inputLine)
        }
    }

    val normalized = logs.map { normalize(it) }
    // normalized.forEach { println(it) }

    val actions = mutableMapOf<Int, MutableList<LogResponse>>()
    normalized.forEach {
        if (it != null) {
            if (actions.contains(it.id)) {
                actions[it.id]!!.add(it)
            } else {
                actions[it.id] = mutableListOf<LogResponse>(it)
            }
        }
    }

    val result = mutableListOf<DeliveryResult>()
    val formatError = mutableListOf<Int>()
    val logicError = mutableListOf<Int>()  // Если "отправка" позже "доставлено"

    for (action in actions) {
        if (action.value.size != 2) {
            formatError.add(action.key)
        } else {
            val sentTime = action.value.find {it.status == "sent"}
            val deliveredTime = action.value.find {it.status == "delivered"}

            if (sentTime == null || deliveredTime == null) {
                formatError.add(action.key)
                continue
            }

            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val sentTimeFormatted = LocalDateTime.parse(sentTime.dt, dateFormatter)
            val deliveredTimeFormatted = LocalDateTime.parse(deliveredTime.dt, dateFormatter)

            val finalTime = ChronoUnit.MINUTES.between(sentTimeFormatted, deliveredTimeFormatted)
            if (finalTime < 0) {
                logicError.add(action.key)
                continue
            }

            val push = DeliveryResult(action.key, finalTime.toInt(), finalTime > 20)
            result.add(push)
        }
    }

    if (result.size > 0) {
        val sortedResult = result.sortedByDescending { it.durationMinutes }
        println("Список всех доставок: (id | delivery time):")
        sortedResult.forEach { println("id: ${it.id}, duration: ${it.durationMinutes}m") }

        println("-----------------------")

        println("Самый долгий заказ - id: ${sortedResult[0].id}, duration: ${sortedResult[0].durationMinutes}m")

        println("-----------------------")
    }

    var shouldShow: Boolean = false
    for (i in result) {
        if (i.isLate) {
            shouldShow = true
            break
        }
    }
    if (shouldShow) {
        println("Нарушено условие доставки:")
        result.forEach { if (it.isLate) println("id: ${it.id}, duration: ${it.durationMinutes}m") }
    }

    if (formatError.isNotEmpty()) {
        println("Ошибки формата (неполные данные): ${formatError.sorted().joinToString(", ")}")
    }

    if (logicError.isNotEmpty()) {
        println("Ошибки логики (доставка раньше отправки): ${logicError.sorted().joinToString(", ")}")
    }

    val counter = mutableMapOf<Int, Int>()
    for (action in actions) {
        action.value.forEach {
            if (it.status == "delivered") {
                val time = it.dt
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val m = LocalDateTime.parse(time, formatter).hour
                if (counter.contains(m)) {
                    counter[m] = counter[m]!! + 1
                } else {
                    counter[m] = 1
                }
            }
        }
    }


    var bestCount = 0
    var ans = 0
    counter.forEach {
        if (it.value > bestCount) {
            ans = it.key
            bestCount = it.value
        }
    }

    println("Больше всего доставок в: ${ans}h")
}