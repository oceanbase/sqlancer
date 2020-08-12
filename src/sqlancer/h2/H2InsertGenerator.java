package sqlancer.h2;

import java.util.List;
import java.util.stream.Collectors;

import sqlancer.common.gen.AbstractInsertGenerator;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.h2.H2Provider.H2GlobalState;
import sqlancer.h2.H2Schema.H2Column;
import sqlancer.h2.H2Schema.H2Table;

public class H2InsertGenerator extends AbstractInsertGenerator<H2Column> {

    private final H2GlobalState globalState;
    private final ExpectedErrors errors = new ExpectedErrors();
    private final H2ExpressionGenerator gen;

    public H2InsertGenerator(H2GlobalState globalState) {
        this.globalState = globalState;
        gen = new H2ExpressionGenerator(globalState);
    }

    public static Query getQuery(H2GlobalState globalState) {
        return new H2InsertGenerator(globalState).generate();
    }

    private Query generate() {
        sb.append("INSERT INTO ");
        H2Table table = globalState.getSchema().getRandomTable(t -> !t.isView());
        List<H2Column> columns = table.getRandomNonEmptyColumnSubset();
        sb.append(table.getName());
        sb.append("(");
        sb.append(columns.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
        sb.append(")");
        sb.append(" VALUES ");
        insertColumns(columns);
        H2Errors.addInsertErrors(errors);
        return new QueryAdapter(sb.toString(), errors);
    }

    @Override
    protected void insertValue(H2Column tiDBColumn) {
        sb.append(H2ToStringVisitor.asString(gen.generateConstant()));
    }
}
