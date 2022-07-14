package Value;

import microservice.ser_des.SerDes;

public abstract class ValueWrapper<T> {
    private T value;
    private final SerDes<ValueWrapper<T>> ser_des;

    protected ValueWrapper(SerDes<ValueWrapper<T>> ser_des) {
        this.ser_des = ser_des;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
