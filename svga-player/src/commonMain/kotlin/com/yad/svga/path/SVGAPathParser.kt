package com.yad.svga.path

/**
 * Parses SVG path data strings into a list of [PathCommand].
 *
 * Supports all standard SVG path commands:
 * - M/m (moveTo): 2 args (x, y), extra pairs become implicit L/l
 * - L/l (lineTo): 2 args (x, y)
 * - H/h (horizontal line): 1 arg (x)
 * - V/v (vertical line): 1 arg (y)
 * - C/c (cubic bezier): 6 args (x1, y1, x2, y2, x, y)
 * - S/s (smooth cubic): 4 args (x2, y2, x, y)
 * - Q/q (quadratic bezier): 4 args (x1, y1, x, y)
 * - A/a (arc): 7 args (rx, ry, xRotation, largeArcFlag, sweepFlag, x, y)
 * - Z/z (close path): 0 args
 *
 * Handles:
 * - Implicit commands (repeated coordinate groups)
 * - Negative numbers as implicit separators ("10-5" → 10, -5)
 * - Consecutive decimal points as separators ("1.5.3" → 1.5, 0.3)
 * - Commas and whitespace as separators
 */
object SVGAPathParser {

    private val COMMAND_CHARS = setOf(
        'M', 'm', 'L', 'l', 'H', 'h', 'V', 'v',
        'C', 'c', 'S', 's', 'Q', 'q', 'A', 'a',
        'Z', 'z'
    )

    /**
     * Returns the expected number of arguments for a given command character.
     */
    private fun argCountFor(cmd: Char): Int = when (cmd.uppercaseChar()) {
        'M', 'L' -> 2
        'H', 'V' -> 1
        'C' -> 6
        'S', 'Q' -> 4
        'A' -> 7
        'Z' -> 0
        else -> 0
    }

    /**
     * Parse an SVG path data string into a list of [PathCommand].
     *
     * Unknown command characters are silently ignored per the error handling spec.
     */
    fun parse(pathString: String): List<PathCommand> {
        val tokens = tokenize(pathString)
        return buildCommands(tokens)
    }

    /**
     * Tokenize the path string into a sequence of command characters and numbers.
     * Each token is either a single command character (String of length 1) or a numeric string.
     */
    private fun tokenize(pathString: String): List<String> {
        val tokens = mutableListOf<String>()
        val s = pathString
        var i = 0

        while (i < s.length) {
            val c = s[i]

            // Skip whitespace and commas
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == ',') {
                i++
                continue
            }

            // Command character
            if (c in COMMAND_CHARS) {
                tokens.add(c.toString())
                i++
                continue
            }

            // Number: optional sign, digits, optional decimal, digits, optional exponent
            if (c == '-' || c == '+' || c == '.' || c in '0'..'9') {
                i = readNumber(s, i, tokens)
                continue
            }

            // Unknown character — skip
            i++
        }

        return tokens
    }

    /**
     * Read a number starting at position [start] in [s], add it to [tokens],
     * and return the new position.
     *
     * Handles:
     * - Leading sign: -3.14, +2
     * - Decimal without leading digit: .5
     * - Consecutive decimals: "1.5.3" → "1.5", ".3"
     * - Exponent notation: 1e2, 1.5E-3
     */
    private fun readNumber(s: String, start: Int, tokens: MutableList<String>): Int {
        var i = start
        val buf = StringBuilder()

        // Optional sign
        if (i < s.length && (s[i] == '-' || s[i] == '+')) {
            buf.append(s[i])
            i++
        }

        // Integer part
        var hasDigits = false
        while (i < s.length && s[i] in '0'..'9') {
            buf.append(s[i])
            i++
            hasDigits = true
        }

        // Decimal part
        var hasDot = false
        if (i < s.length && s[i] == '.') {
            buf.append('.')
            i++
            hasDot = true
            while (i < s.length && s[i] in '0'..'9') {
                buf.append(s[i])
                i++
                hasDigits = true
            }
        }

        // Exponent part
        if (i < s.length && (s[i] == 'e' || s[i] == 'E') && hasDigits) {
            buf.append(s[i])
            i++
            if (i < s.length && (s[i] == '-' || s[i] == '+')) {
                buf.append(s[i])
                i++
            }
            while (i < s.length && s[i] in '0'..'9') {
                buf.append(s[i])
                i++
            }
        }

        if (hasDigits || hasDot) {
            tokens.add(buf.toString())
        }

        return i
    }

    /**
     * Build [PathCommand] list from tokens.
     *
     * Handles implicit commands: after M, extra coordinate pairs become L.
     * After m, extra pairs become l.
     */
    private fun buildCommands(tokens: List<String>): List<PathCommand> {
        val commands = mutableListOf<PathCommand>()
        var i = 0

        while (i < tokens.size) {
            val token = tokens[i]

            // Must be a command character
            if (token.length == 1 && token[0] in COMMAND_CHARS) {
                val cmd = token[0]
                i++

                if (cmd.uppercaseChar() == 'Z') {
                    commands.add(PathCommand(cmd, floatArrayOf()))
                    continue
                }

                val argCount = argCountFor(cmd)
                if (argCount == 0) continue

                // Consume first set of args
                val firstArgs = consumeArgs(tokens, i, argCount)
                if (firstArgs == null) {
                    // Not enough args — skip this command
                    break
                }
                commands.add(PathCommand(cmd, firstArgs.first))
                i = firstArgs.second

                // Implicit repeat: consume additional argument groups
                val implicitCmd = if (cmd == 'M') 'L' else if (cmd == 'm') 'l' else cmd
                val implicitArgCount = argCountFor(implicitCmd)

                while (true) {
                    val nextArgs = consumeArgs(tokens, i, implicitArgCount)
                    if (nextArgs == null) break
                    commands.add(PathCommand(implicitCmd, nextArgs.first))
                    i = nextArgs.second
                }
            } else {
                // Unexpected token (number without preceding command) — skip
                i++
            }
        }

        return commands
    }

    /**
     * Try to consume [count] numeric arguments from [tokens] starting at [start].
     * Returns the float array and the new index, or null if not enough numbers are available.
     */
    private fun consumeArgs(tokens: List<String>, start: Int, count: Int): Pair<FloatArray, Int>? {
        if (count == 0) return Pair(floatArrayOf(), start)

        val args = FloatArray(count)
        var pos = start

        for (j in 0 until count) {
            if (pos >= tokens.size) return null
            val t = tokens[pos]
            // If we hit a command character, we don't have enough args
            if (t.length == 1 && t[0] in COMMAND_CHARS) return null
            val value = t.toFloatOrNull() ?: return null
            args[j] = value
            pos++
        }

        return Pair(args, pos)
    }
}
