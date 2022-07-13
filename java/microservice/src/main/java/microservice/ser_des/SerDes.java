package microservice.ser_des;

import sun.jvm.hotspot.runtime.Bytes;

public interface SerDes<M> {
    Bytes[] toBytes(M message);
    M fromBytes(Bytes[] bytes);
}
