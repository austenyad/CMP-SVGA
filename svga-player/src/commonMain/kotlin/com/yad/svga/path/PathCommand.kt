package com.yad.svga.path

/**
 * Represents a single SVG path command.
 *
 * @param type The command character: M, L, H, V, C, S, Q, A, Z (uppercase = absolute, lowercase = relative)
 * @param args The numeric arguments for this command
 */
data class PathCommand(
    val type: Char,
    val args: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is PathCommand) return false
        return type == other.type && args.contentEquals(other.args)
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + args.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "PathCommand(type='$type', args=${args.toList()})"
    }
}
