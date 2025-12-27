package ru.open.cu.student.cli.impl;

import ru.open.cu.student.SqlProcessor;
import ru.open.cu.student.cli.api.Engine;
import ru.open.cu.student.catalog.manager.CatalogManager;
import ru.open.cu.student.catalog.manager.DefaultCatalogManager;
import ru.open.cu.student.catalog.model.TableDefinition;
import ru.open.cu.student.catalog.operation.DefaultOperationManager;
import ru.open.cu.student.catalog.operation.OperationManager;
import ru.open.cu.student.execution.ExecutorFactory;
import ru.open.cu.student.execution.ExecutorFactoryImpl;
import ru.open.cu.student.execution.QueryExecutionEngineImpl;
import ru.open.cu.student.execution.executors.Executor;
import ru.open.cu.student.lexer.DefaultLexer;
import ru.open.cu.student.lexer.Lexer;
import ru.open.cu.student.lexer.Token;
import ru.open.cu.student.memory.buffer.BufferPoolManager;
import ru.open.cu.student.memory.buffer.DefaultBufferPoolManager;
import ru.open.cu.student.memory.manager.HeapPageFileManager;
import ru.open.cu.student.memory.manager.PageFileManager;
import ru.open.cu.student.memory.replacer.ClockReplacer;
import ru.open.cu.student.optimizer.Optimizer;
import ru.open.cu.student.optimizer.OptimizerImpl;
import ru.open.cu.student.optimizer.node.PhysicalPlanNode;
import ru.open.cu.student.parser.DefaultParser;
import ru.open.cu.student.parser.Parser;
import ru.open.cu.student.ast.AstNode;
import ru.open.cu.student.planner.Planner;
import ru.open.cu.student.planner.PlannerImpl;
import ru.open.cu.student.planner.node.LogicalPlanNode;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultEngine implements Engine {

    private final CatalogManager catalog = new DefaultCatalogManager();

    private final Lexer lexer = new DefaultLexer();
    private final Parser parser = new DefaultParser();

    // SqlProcessor: использует lexer/parser и переводит в HW5 QueryTree (ru.open.cu.student.ast.*)
    private final SqlProcessor sqlProcessor = new SqlProcessor(lexer, parser, catalog);

    private final Planner planner = new PlannerImpl(catalog);
    private final Optimizer optimizer = new OptimizerImpl();

    // Storage/manager (не привязан к конкретному файлу)
    private final PageFileManager pfm = new HeapPageFileManager();

    private final OperationManager opManager = new DefaultOperationManager(catalog);
    private final QueryExecutionEngineImpl execEngine = new QueryExecutionEngineImpl();

    @Override
    public String executeSql(String sql) {
        try {
            // 1) Lexer
            List<Token> tokens = lexer.tokenize(sql);
            log("TOKENS", tokens);

            // 2) Parser -> AST
            AstNode ast = parser.parse(tokens);
            log("AST", ast);

            // 3) SQL -> QueryTree
            var queryTree = sqlProcessor.process(sql);
            log("QUERY_TREE", queryTree);

            // 4) Planner -> Logical plan
            LogicalPlanNode logical = planner.plan(queryTree);
            log("LOGICAL_PLAN", logical);

            // 5) Optimizer -> Physical plan
            PhysicalPlanNode physical = optimizer.optimize(logical);
            log("PHYSICAL_PLAN", physical);

            // Определяем путь к файлу таблицы (и создаём BufferPool для этого файла)
            Path tableFile = resolveTableFile(queryTree);

            BufferPoolManager bufferPool = new DefaultBufferPoolManager(
                    16,
                    pfm,
                    new ClockReplacer(),
                    new ClockReplacer(),
                    tableFile
            );

            ExecutorFactory executorFactory = new ExecutorFactoryImpl(catalog, opManager, bufferPool);

            // 6) ExecutorFactory -> executor
            Executor executor = executorFactory.createExecutor(physical);
            log("EXECUTOR", executor.getClass().getSimpleName());

            // 7) execute
            List<Object> rows = execEngine.execute(executor);

            // flush, чтобы персистилось
            bufferPool.flushAllPages();

            if (rows.isEmpty()) return "OK";
            return rows.stream().map(String::valueOf).collect(Collectors.joining("\n"));

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private Path resolveTableFile(ru.open.cu.student.ast.QueryTree qt) {
        String tableName = (qt.rangeTable != null && !qt.rangeTable.isEmpty())
                ? qt.rangeTable.get(0).relname
                : null;

        if (tableName == null) {
            return Path.of("1.dat").toAbsolutePath();
        }

        // CREATE: таблицы ещё может не быть, поэтому используем временный файл
        if (qt.commandType != null && qt.commandType.name().equals("CREATE")) {
            return Path.of("create_tmp.dat").toAbsolutePath();
        }

        TableDefinition table = catalog.getTable(tableName);
        if (table == null) {
            // Если таблицы нет в каталоге — используем имя как файл
            return Path.of(tableName + ".dat").toAbsolutePath();
        }

        // Если в TableDefinition есть fileNode — используем его
        String fileNode = table.getFileNode();
        if (fileNode != null && !fileNode.isBlank()) {
            return Path.of(fileNode).toAbsolutePath();
        }

        // По умолчанию — oid.dat
        return Path.of(table.getOid() + ".dat").toAbsolutePath();
    }

    // (логирование)
    private void log(String tag, Object o) {
        // System.out.println(tag + ": " + o);
    }
}