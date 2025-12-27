package ru.open.cu.student.lexer;

import java.util.ArrayList;
import java.util.List;

public class DefaultLexer implements Lexer{

    @Override
    public List<Token> tokenize(String sql) {
        List<Token> tokens = new ArrayList<>();
        int position = 0;
        int length = sql.length();

        while (position < length) {
            char currentChar = sql.charAt(position);

            // Пропускаем пробелы
            if (Character.isWhitespace(currentChar)) {
                position++;
                continue;
            }

            // Строковые литералы '...' или "..."
            if (currentChar == '\'' || currentChar == '"') {
                char quote = currentChar;
                StringBuilder sb = new StringBuilder();
                position++; // пропускаем открывающую кавычку
                boolean closed = false;
                while (position < length) {
                    char c = sql.charAt(position);
                    if (c == '\\') {
                        // Экранирование: берём следующий символ (если есть)
                        if (position + 1 < length) {
                            char next = sql.charAt(position + 1);
                            sb.append(next);
                            position += 2;
                        } else {
                            // Нечего экранировать — добавим обратный слэш и выйдем
                            sb.append(c);
                            position++;
                        }
                        continue;
                    }
                    if (c == quote) {
                        // закрывающая кавычка
                        closed = true;
                        position++;
                        break;
                    }
                    sb.append(c);
                    position++;
                }
                if (!closed) {
                    // Некорректный литерал — вернём UNKNOWN с содержимым
                    tokens.add(new Token("UNKNOWN", sb.toString()));
                } else {
                    tokens.add(new Token("STRING", sb.toString()));
                }
                continue;
            }

            // Обрабатываем ключевые слова и идентификаторы
            if (Character.isLetter(currentChar)) {
                StringBuilder word = new StringBuilder();
                while (position < length &&
                        (Character.isLetterOrDigit(sql.charAt(position)) ||
                                sql.charAt(position) == '_')) {
                    word.append(sql.charAt(position));
                    position++;
                }
                String wordStr = word.toString();
                String type = getKeywordType(wordStr);
                tokens.add(new Token(type, wordStr));
                continue;
            }

            // Обрабатываем числа
            if (Character.isDigit(currentChar)) {
                StringBuilder number = new StringBuilder();
                while (position < length && Character.isDigit(sql.charAt(position))) {
                    number.append(sql.charAt(position));
                    position++;
                }
                tokens.add(new Token("NUMBER", number.toString()));
                continue;
            }

            // Обрабатываем операторы и разделители
            switch (currentChar) {
                case ',':
                    tokens.add(new Token("COMMA", ","));
                    position++;
                    break;
                case ';':
                    tokens.add(new Token("SEMICOLON", ";"));
                    position++;
                    break;
                case '>':
                    tokens.add(new Token("GT", ">"));
                    position++;
                    break;
                case '<':
                    tokens.add(new Token("LT", "<"));
                    position++;
                    break;
                case '=':
                    tokens.add(new Token("EQ", "="));
                    position++;
                    break;
                case '!':
                    if (position + 1 < length && sql.charAt(position + 1) == '=') {
                        tokens.add(new Token("NEQ", "!="));
                        position += 2;
                    } else {
                        tokens.add(new Token("UNKNOWN", "!"));
                        position++;
                    }
                    break;
                case '(':
                    tokens.add(new Token("LPAREN", "("));
                    position++;
                    break;
                case ')':
                    tokens.add(new Token("RPAREN", ")"));
                    position++;
                    break;
                case '*':
                    tokens.add(new Token("ASTERISK", "*"));
                    position++;
                    break;
                case '.':
                    tokens.add(new Token("DOT", "."));
                    position++;
                    break;
                default:
                    // Неизвестный символ
                    tokens.add(new Token("UNKNOWN", String.valueOf(currentChar)));
                    position++;
                    break;
            }
        }

        return tokens;
    }

    private String getKeywordType(String word) {
        return switch (word.toUpperCase()) {
            case "SELECT" -> "SELECT";
            case "FROM" -> "FROM";
            case "WHERE" -> "WHERE";
            case "INSERT" -> "INSERT";
            case "UPDATE" -> "UPDATE";
            case "DELETE" -> "DELETE";
            case "CREATE" -> "CREATE";
            case "TABLE" -> "TABLE";
            case "DROP" -> "DROP";
            case "ALTER" -> "ALTER";
            case "AND" -> "AND";
            case "OR" -> "OR";
            case "NOT" -> "NOT";
            case "INTO" -> "INTO";
            case "VALUES" -> "VALUES";
            case "SET" -> "SET";
            default -> "IDENT";
        };
    }
}