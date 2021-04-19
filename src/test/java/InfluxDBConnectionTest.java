import com.influxdb.model.Migration;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.*;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class InfluxDBConnectionTest {


    @Test
    void shouldReturnTrueIfConnectionIsSuccessful() {
        InfluxDB connection = connectDB();
        assertTrue(pingServer(connection));
    }

    private boolean pingServer(InfluxDB connection) {
        try {
            Pong response = connection.ping();
            if (response.getVersion().equalsIgnoreCase("unknown")) {
                System.out.println("Error pinging server.");
                return false;
            } else {
                System.out.println("Connection successful");
                return true;
            }
        } catch (InfluxDBIOException influxDBIOException) {
            System.out.println("Error while pinging database");
            return false;
        }
    }

    private InfluxDB connectDB() {
        return InfluxDBFactory.connect("http://localhost:8086", "test", "password");
    }

    @Test
    public void shouldReturnTrueIfDatabaseIsCreatedSuccessfully() {
        InfluxDB connection = connectDB();

        Query query = new Query("CREATE DATABASE test1");
        QueryResult result = connection.query(query);

        assertNotNull(result);
    }

    @Test
    void shouldBeAbleToAddDataToTheDatabase() {
        InfluxDB connection = connectDB();
        Migration migration = new Migration();
        migration.setId("91752A");
        migration.setS2CellId("17b4854");
        migration.setLat(7.883);
        migration.setLon(38.7975);
        migration.setTimeStamp(Long.parseLong("1553065200000000000"));
        Point point = Point.measurement("migration")
                .time(migration.getTimeStamp(), TimeUnit.NANOSECONDS)
                .tag("id", migration.getId())
                .tag("s2_cell_id", migration.getS2CellId())
                .addField("lat", migration.getLat())
                .addField("lon", migration.getLon())
                .build();

        connection.write("test1", "autogen",point);
    }

    @Test
    void shouldBeAbleToShowDataFromTheDatabse() {
        InfluxDB influxDB = connectDB();
        influxDB.setDatabase("test1");
        
        Query query = new Query("SELECT * FROM migration");
        QueryResult queryResult = influxDB.query(query);
        
        List<QueryResult.Result> results = queryResult.getResults();
        
        for (QueryResult.Result result : results) {
            List<QueryResult.Series> series = result.getSeries();
            
            for(QueryResult.Series oneSeries: series) {
                System.out.println(oneSeries.getName());
                System.out.println(oneSeries.getColumns());
                List<List<Object>> values = oneSeries.getValues();
                
                for(List<Object> value: values) {
                    for(Object obj: value) {
                        System.out.print(obj+" ");
                    }
                    System.out.println();
                }
            }
        }
    }

    @Test
    void shouldBeAbleToDropTheTable() {

    }
}
