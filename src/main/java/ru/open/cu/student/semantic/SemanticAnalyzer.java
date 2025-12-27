package ru.open.cu.student.semantic;

import ru.open.cu.student.ast.AstNode;
import ru.open.cu.student.ast.QueryTree;
import ru.open.cu.student.catalog.manager.CatalogManager;

public interface SemanticAnalyzer {
    QueryTree analyze(AstNode ast, CatalogManager catalog);
}