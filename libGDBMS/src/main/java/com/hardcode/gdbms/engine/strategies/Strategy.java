package com.hardcode.gdbms.engine.strategies;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.instruction.CustomAdapter;
import com.hardcode.gdbms.engine.instruction.EvaluationException;
import com.hardcode.gdbms.engine.instruction.SelectAdapter;
import com.hardcode.gdbms.engine.instruction.SemanticException;
import com.hardcode.gdbms.engine.instruction.UnionAdapter;
import com.hardcode.gdbms.parser.ParseException;



/**
 * Interfaz que define las operaciones que se pueden realizar con los
 * DataSource. Las distintas implementaciones de esta interfaz ser�n las
 * encargadas del uso de los indices, del algoritmo usado para cada operaci�n,
 * ...
 */
public abstract class Strategy {
    /**
     * Realiza una select a partir de la instrucci�n que se pasa como par�metro
     *
     * @param instr Objeto con la informaci�n sobre las tablas que entran en
     *        juego en la instrucci�n, campos, expresiones condicionales, ...
     *
     * @return DataSource con el resultado de la instruccion
     * @throws SemanticException Si se produce alg�n error sem�ntico
     * @throws EvaluationException If the evaluation of any expression fails
     * @throws ReadDriverException TODO
     * @throws RuntimeException bug
     */
    public OperationDataSource select(SelectAdapter instr)
        throws SemanticException, EvaluationException, ReadDriverException {
        throw new RuntimeException(
            "This strategy does not support select execution");
    }

    /**
     * Realiza una union a partir de la instrucci�n que se pasa como par�metro
     *
     * @param instr Objeto con la informaci�n sobre las tablas que entran en
     *        juego en la instrucci�n
     *
     * @return DataSource con el resultado de la instruccion
     * @throws SemanticException Si se produce alg�n error sem�ntico
     * @throws DriverLoadException If cannot find the suitable driver to access the data
     * @throws ParseException If the union statement contains a select statement and its parse fails
     * @throws EvaluationException If there's any problem during expresion evaluation
     * @throws ReadDriverException TODO
     * @throws RuntimeException bug
     */
    public OperationDataSource union(UnionAdapter instr)
        throws SemanticException, DriverLoadException, ParseException, EvaluationException, ReadDriverException {
        throw new RuntimeException(
            "This strategy does not support union execution");
    }

    /**
     * Makes a custom query
     *
     * @param instr The instruction specifying the custom query
     *
     * @return The result DataSource
     *
     * @throws SemanticException If there is some semantic error in the
     *         expression
     * @throws RuntimeException bug
     */
    public OperationDataSource custom(CustomAdapter instr)
        throws SemanticException {
        throw new RuntimeException(
            "This strategy does not support custom queries execution");
    }
}
