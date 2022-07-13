package microservice.ser_des;

import sun.jvm.hotspot.runtime.Bytes;

public class StringSerDes implements SerDes<String>{
    @Override
    public Bytes[] toBytes(String message) {
        return new Bytes[0];
    }

    @Override
    public String fromBytes(Bytes[] bytes) {
        return null;
    }
}
