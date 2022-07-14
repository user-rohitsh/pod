package microservice.ser_des;

import java.nio.ByteBuffer;

public class StringSerDes implements SerDes<String> {
    @Override
    public ByteBuffer toBytes(String message) {
        return null;
    }

    @Override
    public String fromBytes(ByteBuffer bytes) {
        return null;
    }
}
