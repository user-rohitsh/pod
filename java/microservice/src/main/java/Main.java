import microservice.MemoryMappedQueue;
import microservice.Microservice;
import microservice.Persitence.MmapPersistence;
import microservice.ser_des.SerDes;
import microservice.ser_des.StringSerDes;

public class Main {

    public static String data_dir = "/tmp/data/";

    public static void main(String[] args)
    {
        SerDes<String> ser_des = new StringSerDes();
        MemoryMappedQueue<String> inMemoryMappedQueue = new MemoryMappedQueue<String>(data_dir + "test_service_in");
        MemoryMappedQueue<String> outMemoryMappedQueue = new MemoryMappedQueue<String>(data_dir + "test_service_out");
        Microservice<String,String> microservice = new Microservice<>(inMemoryMappedQueue,outMemoryMappedQueue,new MmapPersistence());
        microservice.start(ser_des);
    }
}
