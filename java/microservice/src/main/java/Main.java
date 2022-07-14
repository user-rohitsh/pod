import microservice.Microservice;
import microservice.state_management.MemoryMappedLog;
import microservice.ser_des.SerDes;
import microservice.ser_des.StringSerDes;

public class Main {

    public static String data_dir = "/tmp/data/";

    public static void main(String[] args)
    {
        SerDes<String> ser_des = new StringSerDes();
        MemoryMappedLog<String> inMemoryMappedQueue = new MemoryMappedLog<String>(data_dir + "test_service_in",ser_des);
        MemoryMappedLog<String> outMemoryMappedQueue = new MemoryMappedLog<String>(data_dir + "test_service_out",ser_des);
        Microservice<String,String> microservice = new Microservice<>(inMemoryMappedQueue,outMemoryMappedQueue);
        microservice.start(ser_des);
    }
}
