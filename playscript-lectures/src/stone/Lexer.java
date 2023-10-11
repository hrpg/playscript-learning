package stone;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    public static final String regex = "\\s*((//.*)|([0-9]+)|(\"(\\\\\"|\\\\\\\\|\\\\n|[^\"])*\")"
            + "|[A-Za-z][A-Za-z0-9_]*|==|<=|>=|&&|\\|\\||\\p{Punct})";

    private Pattern pattern = Pattern.compile(regex);

    private ArrayList<Token> queue = new ArrayList<Token>();
    private boolean hasMore;

    private LineNumberReader reader;

    public Lexer(Reader reader) {
        this.hasMore = true;
        this.reader = new LineNumberReader(reader);
    }


    public Token read() throws ParseException {
        if (fillQueue(0)) {
            return queue.remove(0);
        } else {
            return Token.EOF;
        }
    }

    public Token peek(int i) throws ParseException {
        if (fillQueue(i)) {
            return queue.get(i);
        } else {
            return Token.EOF;
        }
    }

    private boolean fillQueue(int i) throws ParseException {
        while (i >= queue.size()) {
            if (hasMore) {
                readLine();
            } else {
                return false;
            }
        }

        return true;
    }

    protected void readLine() throws ParseException {
        String line;

        try {
            line = reader.readLine();
        } catch (IOException e) {
            throw new ParseException(e);
        }

        if (line == null) {
            hasMore = false;
            return;
        }

        int lineNo = reader.getLineNumber();
        Matcher matcher = pattern.matcher(line);

        // 设置这个参数的目的是为了严格匹配，确保匹配到的token都是单个的单词“test 123”
        // 匹配到的应该是test, 123, 而不是当字符串是“test123”时，匹配到test, 123
        matcher.useTransparentBounds(true).useAnchoringBounds(false);
        int pos = 0;
        int endPos = line.length();

        while (pos < endPos) {
            // 标定范围
            matcher.region(pos, endPos);
            // 匹配每次划好的开头部分
            if (matcher.lookingAt()) {
                addToken(lineNo, matcher);
                pos = matcher.end();
            } else {
                throw new ParseException("bad token at line " + lineNo);
            }
        }

        // 为每行画上结束符
        queue.add(new IdToken(lineNo, Token.EOL));
    }

    protected void addToken(int lineNo, Matcher matcher) {
        String matched = matcher.group(1);

        //
        if (matched != null) {
            // if not a commentv
            if (matcher.group(2) == null) {
                Token token;
                if (matcher.group(3) != null)
                    token = new NumToken(lineNo, Integer.parseInt(matched));
                else if (matcher.group(4) != null)
                    token = new StrToken(lineNo, toStringLiteral(matched));
                else
                    token = new IdToken(lineNo, matched);

                queue.add(token);
            }
        }
    }

    protected String toStringLiteral(String str) {
        StringBuilder sb = new StringBuilder();
        int len = str.length() - 1;

        // 定义的转义字符有 \", \\, \n
        for (int i = 1; i < len; ++i) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char c2 = str.charAt(i + 1);
                if (c2 == '"' || c2 == '\\') {
                    ++i;
                    c = str.charAt(i);
                } else {
                    ++i;
                    c = '\n';
                }
            }

            sb.append(c);
        }

        return sb.toString();
    }

    protected static class NumToken extends Token {

        private int value;

        protected NumToken(int line, int val) {
            super(line);
            value = val;
        }

        public boolean isNumber() {
            return true;
        }

        @Override
        public String getText() {
            return Integer.toString(value);
        }

        @Override
        public int getNumber() {
            return value;
        }
    }

    protected static class IdToken extends Token {

        private String text;

        protected IdToken(int line, String id) {
            super(line);
            text = id;
        }

        @Override
        public boolean isIdentifier() {
            return true;
        }

        @Override
        public String getText() {
            return text;
        }

    }

    protected static class StrToken extends Token {

        private String literal;

        protected StrToken(int line, String str) {
            super(line);
            literal = str;
        }

        @Override
        public boolean isString() {
            return true;
        }

        @Override
        public String getText() {
            return literal;
        }

    }

}
