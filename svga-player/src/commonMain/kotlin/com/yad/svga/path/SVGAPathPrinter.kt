package com.yad.svga.path

/**
 * Formats a list of [PathCommand] back into a valid SVG path data string.
 *
 * The output is designed to be round-trip consistent with [SVGAPathParser]:
 * `SVGAPathParser.parse(SVGAPathPrinter.print(commands))` should produce
 * an equivalent list of PathCommand.
 *
 * Formatting rules:
 * - Each command is its type character followed by space-separated arguments
 * - Commands are separated by spaces
 * - Float values are cleaned: trailing zeros removed (e.g., "10.0" → "10", "1.5" stays "1.5")
 * - Z/z commands have no arguments
 */
object SVGAPathPrinter {

    /**
     * Format a list of [PathCommand] into an SVG path data string.
     */
    fun print(commands: List<PathCommand>): String {
        return commands.joinToString(" ") { cmd ->
            if (cmd.args.isEmpty()) {
                cmd.type.toString()
            } else {
                cmd.type + " " + cmd.args.joinToString(" ") { formatFloat(it) }
            }
        }
    }

    /**
     * Format a float value cleanly:
     * - Remove trailing zeros after decimal point ("10.0" → "10", "1.50" → "1.5")
     * - Keep integer values without decimal point
     * - Preserve necessary decimal digits
     */
    internal fun formatFloat(value: Float): String {
        // Check if value is a whole number within safe integer range for Long
        if (value % 1.0f == 0.0f && !value.isNaN() && !value.isInfinite()
            && value >= Long.MIN_VALUE.toFloat() && value <= Long.MAX_VALUE.toFloat()
        ) {
            return value.toLong().toString()
        }

        val str = value.toString()

        // Strip trailing zeros after decimal point (but not in scientific notation)
        if ('.' in str && 'E' !in str && 'e' !in str) {
            return str.trimEnd('0').trimEnd('.')
        }

        return str
    }
}
