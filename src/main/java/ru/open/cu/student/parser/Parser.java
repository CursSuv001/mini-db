package ru.open.cu.student.parser;

import ru.open.cu.student.ast.AstNode;
import ru.open.cu.student.lexer.Token;

import java.util.List;

public interface Parser {
    AstNode parse(List<Token> tokens);

}