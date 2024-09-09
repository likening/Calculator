import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

public class Calculator {
    /**
     * 初始值
     */
    private final BigDecimal initValue = BigDecimal.ZERO;

    /**
     * 当前值
     */
    private BigDecimal currentValue;

    /**
     * 记录历史操作
     */
    private List<Operation> history;

    /**
     *  每个操作都会记录在一个操作栈
     *  支持撤销功能
     */
    private LinkedList<Operation> undoStack;


    /**
     * 当执行undo操作时，最新的操作会被移除
     * 该操作会被存储在redoStack中，之后可以重做
     */
    private LinkedList<Operation> redoStack;

    /**
     * 保留小数点
     */
    private final Integer scaleNum = 6;

    public Calculator() {
        currentValue = initValue;
        history = new LinkedList<>();
        undoStack = new LinkedList<>();
        redoStack = new LinkedList<>();
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    /**
     * 加法
     * @param number
     */
    public void add(BigDecimal number) {
        performOperation(new Operation(OperationType.ADD, number));
    }

    /**
     * 减法
     * @param number
     */
    public void subtract(BigDecimal number) {
        performOperation(new Operation(OperationType.SUBTRACT, number));
    }

    /**
     * 乘法
     * @param number
     */
    public void multiply(BigDecimal number) {
        performOperation(new Operation(OperationType.MULTIPLY, number));
    }

    /**
     * 除法
     * @param number
     */
    public void divide(BigDecimal number) {
        if (number.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Cannot divide by zero.");
        }
        performOperation(new Operation(OperationType.DIVIDE, number));
    }

    /**
     * 撤销操作
     */
    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("No operation to undo.");
            return;
        }
        //撤销
        Operation lastOp = undoStack.removeLast();

        //重做
        redoStack.add(lastOp);

        if(history.size() > 0) {
            history.remove(history.size() - 1);
        }

        if(history.isEmpty()) {
            currentValue = initValue;
            return;
        }
        currentValue = initValue;
        for (int i = 0; i < history.size(); i++) {
            Operation operation = history.get(i);
            _performOperation(operation);
        }
    }

    /**
     *  从redoStack中恢复最近被撤销的操作
     */
    public void redo() {
        if (redoStack.isEmpty()) {
            System.out.println("No operation to redo.");
            return;
        }
        Operation lastOp = redoStack.removeLast();
        undoStack.add(lastOp);
        _performOperation(lastOp);
        history.add(new Operation(lastOp.type, lastOp.value, "REDO"));
    }

    public List<Operation> getHistory() {
        return history;
    }

    /**
     * 处理操作符
     * @param op
     */
    private void _performOperation(Operation op) {
        switch (op.type) {
            case ADD:
                currentValue = currentValue.add(op.value);
                break;
            case SUBTRACT:
                currentValue = currentValue.subtract(op.value);
                break;
            case MULTIPLY:
                currentValue = currentValue.multiply(op.value);
                break;
            case DIVIDE:
                currentValue = currentValue.divide(op.value, scaleNum, RoundingMode.HALF_UP);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + op.type);
        }
    }


    private void performOperation(Operation op) {
        /**
         * 运算
         */
        _performOperation(op);

        /**
         * 记录待撤销数据
         */
        undoStack.add(op);

        /**
         *  清除重做
         */
        redoStack.clear();

        /**
         * 加入历史记录
         */
        history.add(op);
    }

    static class Operation {
        OperationType type;
        BigDecimal value;
        String note;

        public Operation(OperationType type, BigDecimal value) {
            this.type = type;
            this.value = value;
        }

        public Operation(OperationType type, BigDecimal value, String note) {
            this.type = type;
            this.value = value;
            this.note = note;
        }
    }

    enum OperationType {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.add(new BigDecimal(3));
        calculator.subtract(new BigDecimal(1));
        calculator.multiply(new BigDecimal(5));
        calculator.divide(new BigDecimal(3));
        calculator.undo();
        calculator.undo();
        calculator.redo();
        calculator.redo();
        BigDecimal currentValue = calculator.getCurrentValue();
        System.out.println(currentValue);
    }
}
