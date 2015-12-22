package droute;

/**
 * Parses HTTP requests
 * <p/>
 * Usage: repeatedly feed data to parse() until isError() or isFinished() are true. Call reset() before starting
 * the next request.
 * <p/>
 * This parser deviates from RFC7230 in the following ways:
 * <ul>
 *     <li>requests using the deprecated header line folding are rejected</li>
 *     <li>the HTTP version number is optional and not validated by the parser</li>
 *     <li>target URI validity is not enforced beyond checking the allowed characters</li>
 * </ul>
 */
class HttpRequestParser {
    private static int SYMBOL_COUNT = 10;
    private static final byte[] SYMBOLS = buildSymbolTable();
    private static final byte[] TRANSITIONS = {
            /* v   tok   url   com   ':'   ' '   '?'  '\r'  '\n'  non-printable */
            0x7e, 0x1a, 0x7e, 0x1a, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, /* state 0: start of method */
            0x7e, 0x7e, 0x11, 0x11, 0x11, 0x32, 0x3b, 0x33, 0x7e, 0x7e, /* state 1: request-path */
            0x7e, 0x7e, 0x12, 0x12, 0x12, 0x7e, 0x7e, 0x43, 0x7e, 0x7e, /* state 2: http-version */
            0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x04, 0x7e, /* state 3: request-line newline */
            0x7e, 0x15, 0x7e, 0x15, 0x7e, 0x7e, 0x7e, 0x09, 0x7e, 0x7e, /* state 4: start of field-name */
            0x7e, 0x15, 0x7e, 0x15, 0x56, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, /* state 5: field-name */
            0x17, 0x17, 0x17, 0x17, 0x17, 0x06, 0x68, 0x68, 0x7e, 0x7e, /* state 6: field-value leading spaces */
            0x17, 0x17, 0x17, 0x17, 0x17, 0x17, 0x68, 0x68, 0x7e, 0x7e, /* state 7: field-value */
            0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x04, 0x7e, /* state 8: field newline */
            0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7f, 0x7e, /* state 9: final newline */
            0x7e, 0x1a, 0x7e, 0x1a, 0x7e, 0x21, 0x7e, 0x7e, 0x7e, 0x7e, /* state a: method */
            0x7e, 0x7e, 0x1b, 0x1b, 0x1b, (byte)0x82, 0x1b, 0x33, 0x7e, 0x7e, /* state b: query-string */
            /*
             * entry form 0xAS: A - action, S - next state
             * final states: e - error, f - finished
             */
    };

    int state;
    StringBuilder buffer;
    String method;
    String path;
    String query;
    String version;
    String fieldName;
    MultiMap<String, String> fields;

    HttpRequestParser() {
        reset();
    }

    private static byte[] buildSymbolTable() {
        byte[] symbols = new byte[256];

        for (int i = 0; i < 32; i++) {
            symbols[i] = 9; // non-printable
        }
        symbols[127] = 9; // DEL (non-printable)

        symbols['\t'] = 0; // tab is allowed in field values

        fill(symbols, "^`|", 1); // token only
        fill(symbols, "(),/:;=@[]", 2); // url only
        fill(symbols, "!#$%&'*+-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~", 3); // common

        symbols[':'] = 4;
        symbols[' '] = 5;
        symbols['?'] = 6;
        symbols['\r'] = 7;
        symbols['\n'] = 8;

        return symbols;
    }

    private static void fill(byte[] array, String chars, int value) {
        for (int i = 0; i < chars.length(); i++) {
            array[chars.charAt(i)] = (byte) value;
        }
    }

    public void reset() {
        state = 0;
        buffer = new StringBuilder();
        method = null;
        path = null;
        query = null;
        version = null;
        fieldName = null;
        fields = new LinkedTreeMultiMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public int parse(byte[] data, int offset, int length) {
        int end = offset + length;

        for (int i = offset; i < end; i++) {
            int b = data[i] & 0xff;
            int symbol = SYMBOLS[b];
            int opcode = TRANSITIONS[state * SYMBOL_COUNT + symbol] & 0xFF;
            int action = opcode >> 4;

            switch (action) {
                case 0: // no action
                    break;
                case 1: // push current byte
                    buffer.append((char) b); // iso-8859-1 -> utf-16
                    break;
                case 2: // end of method
                    method = buffer.toString();
                    buffer.setLength(0);
                    break;
                case 3: // end of request path
                    path = buffer.toString();
                    buffer.setLength(0);
                    break;
                case 4: // end of http-version
                    version = buffer.toString();
                    buffer.setLength(0);
                    break;
                case 5: // end of field-name
                    fieldName = buffer.toString();
                    buffer.setLength(0);
                    break;
                case 6: // end of field-value
                    fields.put(fieldName, buffer.toString());
                    fieldName = null;
                    buffer.setLength(0);
                    break;
                case 7: // halt parsing
                    state = opcode & 0xf;
                    return i - offset + 1;
                case 8: // end of query string
                    query = buffer.toString();
                    buffer.setLength(0);
                    break;
                default:
                    throw new IllegalStateException("illegal parse action: " + action);
            }

            state = opcode & 0xf;
        }
        return length;
    }

    public boolean isError() {
        return state == 0xe;
    }

    public boolean isFinished() {
        return state == 0xf;
    }
}
