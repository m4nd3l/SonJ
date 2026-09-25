package dev.m4nd3l.sonj.deserialization.lexer;

import dev.m4nd3l.sonj.exceptions.StringEndingMissingException;
import dev.m4nd3l.sonj.exceptions.UnexpectedSymbolException;
import dev.m4nd3l.sonj.exceptions.UnknownJsonTypeException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JsonLexer {
    private int currentPosition;
    private List<Token> tokens;
    private String json;

    public JsonLexer(String json) {
        this.json = json;
        this.tokens = new ArrayList<>();
        currentPosition = 0;
    }

    public List<Token> tokenize() {
        while (currentPosition < json.length()) tokens.add(getToken());
        return tokens;
    }

    public Token getToken() {
        if (currentPosition >= json.length())
            return new Token("\0", "\0", TokenType.END_OF_FILE, json.length() - 1);

        char currentChar = getCurrentChar();

        // WHITESPACE
        if (Character.isWhitespace(currentChar)) {
            int start = currentPosition;
            do advance(); while (Character.isWhitespace(getCurrentChar()));
            String spaces = json.substring(start, currentPosition);
            return new Token(spaces, null, TokenType.WHITESPACE, start);
        }

        // BRACKETS
        if (currentChar == '{') return advanceAndReturn("{", null, TokenType.OPEN_CURLY_BRACKET, currentPosition);
        if (currentChar == '}') return advanceAndReturn("}", null, TokenType.CLOSED_CURLY_BRACKET, currentPosition);
        if (currentChar == '[') return advanceAndReturn("[", null, TokenType.OPEN_BRACKET, currentPosition);
        if (currentChar == ']') return advanceAndReturn("]", null, TokenType.CLOSED_BRACKET, currentPosition);

        // COMMA
        if (currentChar == ',') return advanceAndReturn(",", null, TokenType.COMMA, currentPosition);

        // COLON
        if (currentChar == ':') return advanceAndReturn(":", null, TokenType.COLON, currentPosition);

        // TRUE, FALSE, NULL
        if (Character.isLetter(currentChar)) {
            int start = currentPosition;
            do advance(); while (Character.isLetter(getCurrentChar()));
            String code = json.substring(start, currentPosition);
            TokenType type = switch (code) {
                case "true"  -> TokenType.TRUE;
                case "false" -> TokenType.FALSE;
                case "null"  -> TokenType.NULL;
                default -> throw new UnknownJsonTypeException(code, start);
            };

            return new Token(code, type.getValue(), type, start);
        }

        // NUMBERS
        if (Character.isDigit(currentChar) || currentChar == '-') {
            int start = currentPosition;
            boolean isDecimal = false;

            if (getCurrentChar() == '-') advance();
            while (currentPosition < json.length()) {
                char c = getCurrentChar();
                if (Character.isDigit(c)) advance();
                else if (c == '.' || c == 'e' || c == 'E' || c == '+') {
                    isDecimal = true;
                    advance();
                } else break;
            }

            String code = json.substring(start, currentPosition);
            try {
                if (isDecimal) {
                    double value = Double.parseDouble(code);
                    return new Token(code, value, TokenType.DECIMAL, start);
                } else {
                    long value = Long.parseLong(code);
                    return new Token(code, value, TokenType.INTEGER, start);
                }
            } catch (NumberFormatException e) { throw new UnknownJsonTypeException("Invalid number: " + code, start); }
        }

        // STRING
        if (currentChar == '"') {
            int start = currentPosition;
            advance(); // Skips first "
            StringBuilder sb = new StringBuilder();
            boolean closed = false;
            while (currentPosition < json.length()) {
                char c = getCurrentChar();
                if (c == '\\') {
                    advance(); // Skips \
                    char escapeChar = getCurrentChar();
                    switch (escapeChar) { // Decode char after \
                        case 'n':  sb.append('\n'); break;
                        case 't':  sb.append('\t'); break;
                        case 'r':  sb.append('\r'); break;
                        case '\\': sb.append('\\'); break;
                        case '"':  sb.append('"'); break;
                        case '0':  sb.append('\0'); break;
                        case 'u': {
                            advance(); // Skip 'u'
                            if (currentPosition + 4 > json.length())
                                throw new StringEndingMissingException("Incomplete Unicode escape", currentPosition);

                            // Extract the 4 hex digits
                            String hex = json.substring(currentPosition, currentPosition + 4);
                            try {
                                // Convert hex string to integer, then to a char
                                int codePoint = Integer.parseInt(hex, 16);
                                sb.append((char) codePoint);

                                // Move position forward by the 4 digits processed
                                for(int i = 0; i < 3; i++) advance();
                            } catch (NumberFormatException e) {
                                throw new UnknownJsonTypeException("Invalid Unicode hex: " + hex, currentPosition);
                            }
                            break;
                        }
                        default:
                            sb.append('\\').append(escapeChar);
                            break;
                    }
                    advance(); // Skip decoded char or last Unicode identifier
                    continue;
                }
                if (c == '"') { advance(); closed = true; break; } // Skip last "
                sb.append(c); // Adds char
                advance();
            }

            String code = json.substring(start, currentPosition);
            if (!closed) throw new StringEndingMissingException(sb.toString(), currentPosition);
            String value = sb.toString();

            return new Token(code, value, TokenType.STRING, start);
        }
        throw new UnknownJsonTypeException(currentPosition);
    }

    private char advance() { currentPosition++; return getPreviousChar(); }
    private char getCurrentChar() { try { return json.charAt(currentPosition); } catch (Exception ignored) { return '\0'; } }
    private char getPreviousChar() { try { return json.charAt(currentPosition - 1); } catch (Exception ignored) { return '\0'; } }
    private Token advanceAndReturn(String code, Object value, TokenType type, int start)
        { advance(); return new Token(code, value, type, start); }
    public List<Token> getTokens() { return tokens; }
}