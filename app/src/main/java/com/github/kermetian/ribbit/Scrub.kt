package com.github.kermetian.ribbit

import com.github.kermetian.ribbit.domains.TrackingCatalog
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.util.Locale

/**
 * Deletes known tracking/attribution query fields from HTTP(S) whitespace tokens.
 * Retained URL components are never re-encoded. Opaque IDs, fragments, nested URLs and
 * recognized signed URLs are preserved; see docs/url-tracking-research.md.
 */
fun scrub(uri: String): String {
    // Shares are usually one URL; only use token replacement when text contains whitespace.
    if (uri.none { it.isWhitespace() }) return scrubToken(uri)
    return nonWhitespaceToken.replace(uri) { scrubToken(it.value) }
}

// Kotlin Char.isWhitespace() uses both of these Java character classifications.
private val nonWhitespaceToken = Regex("[^\\p{javaWhitespace}\\p{javaSpaceChar}]+")

private fun scrubToken(token: String): String {
    var url = token.trimEnd { it.isSentencePunctuation() }
    var prefixLength = 0
    while (url.isNotEmpty()) {
        val closing = closingWrapper(url.first()) ?: break
        if (url.length < 2 || url.last() != closing) return token
        prefixLength++
        url = url.substring(1, url.lastIndex).trimEnd { it.isSentencePunctuation() }
    }
    if (!url.startsWith("http://", ignoreCase = true) &&
        !url.startsWith("https://", ignoreCase = true)
    ) return token

    val cleaned = scrubUrl(url)
    return if (cleaned == url) token
    else token.replaceRange(prefixLength, prefixLength + url.length, cleaned)
}

private fun scrubUrl(url: String): String {
    val question = url.indexOf('?')
    val queryEnd = url.indexOf('#').let { if (it < 0) url.length else it }
    if (question < 0 || question > queryEnd || !hasUnambiguousBrackets(url)) return url

    val parsed = try {
        URI(url)
    } catch (_: URISyntaxException) {
        return url
    }
    if (parsed.isOpaque || parsed.rawAuthority.isNullOrEmpty()) return url
    val host = parsed.host?.lowercase(Locale.ROOT)?.removeSuffix(".")
    val family = host?.let { TrackingCatalog.familyFor(it, parsed.rawPath) }

    // Kotlin split retains empty entries, including the one after a trailing '&'.
    // Filter raw entries: never decode/re-encode their values or retained names.
    val entries = url.substring(question + 1, queryEnd).split('&')
    val retained = entries.filter { entry ->
        val name = comparisonName(entry)
        // A signature covers the entire query, so abandon even earlier removals.
        if (name == "X-Amz-Signature" || name == "X-Goog-Signature") return url
        name == null || (!TrackingCatalog.isGlobal(name, host) && family?.removes(name) != true)
    }
    if (retained.size == entries.size) return url

    val query = if (retained.isEmpty()) "" else "?" + retained.joinToString("&")
    return url.substring(0, question) + query + url.substring(queryEnd)
}

private fun Char.isSentencePunctuation(): Boolean =
    this == '.' || this == ',' || this == '!' || this == ';' || this == ':'

private fun closingWrapper(open: Char): Char? = when (open) {
    '(' -> ')'
    '[' -> ']'
    '{' -> '}'
    '<' -> '>'
    '"' -> '"'
    '\'' -> '\''
    else -> null
}

/** Bare balanced path parentheses and IPv6/query brackets are not prose wrappers. */
private fun hasUnambiguousBrackets(text: String): Boolean {
    if (text.isEmpty() || text.last() == '"' || text.last() == '\'') return false
    var parentheses = 0
    var brackets = 0
    for (char in text) {
        when (char) {
            '(' -> parentheses++
            ')' -> if (--parentheses < 0) return false
            '[' -> brackets++
            ']' -> if (--brackets < 0) return false
        }
    }
    return parentheses == 0 && brackets == 0
}

private fun comparisonName(entry: String): String? {
    val name = entry.substringBefore('=')
    // Catalog names are ASCII. A literal '+' is not a form-encoded space.
    if (name.any { it == '+' || it.code > 127 }) return null
    if ('%' !in name) return name
    // URI validated escapes; decode once, for comparison only.
    return URLDecoder.decode(name, "UTF-8").takeIf { it.all { char -> char.code <= 127 } }
}