package src.LLDConcepts;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


interface ParkingStrategy{
    String park(ParkingFloor floors[], int vehicleType);
}

public class ParkingLot implements Q07ParkingLotInterface {
    private ParkingFloor floors[];
    private Helper07 helper;
    private ParkManager parkManager = new ParkManager();
    private int vehicleTypes[]={2, 4};
    private final SearchManager searchManager = new SearchManager();

    public void init(Helper07 helper, Integer [][][] parking) {
        this.helper=helper;
        floors=new ParkingFloor[parking.length];
        for (int i = 0; i < parking.length; i++) {
            floors[i]=new ParkingFloor(i, parking[i], vehicleTypes);
        }
    }
    // returns spotId, e.g. 2-0-11 which is
    // parking spot at parking[2][0][11]
    public String park(int vehicleType, String vehicleNumber,
                       String ticketId, int parkingStrategy) {
        String spotId = parkManager.park(
                floors, vehicleType, parkingStrategy);

        if (!spotId.isEmpty())
            searchManager.index(spotId, vehicleNumber, ticketId);
        return spotId;
    }

    // spotId : 2-0-11 --> parking spot at parking[2][0][11]
    public boolean removeVehicle(String spotId) {
        String[] d = spotId.split("-");
        int floorIndex = Integer.parseInt(d[0]);
        return floors[floorIndex].removeVehicle(
                Integer.parseInt(d[1]), Integer.parseInt(d[2]));
    }

    public int getFreeSpotsCount(
            int floor, int vehicleType) {
        return floors[floor].getFreeSpotsCount(
                vehicleType);
    }

    // query is either vehicleNumber or ticketId
    public String searchVehicle(String query) {
        return searchManager.search(query);
    }
    
}

class ParkingFloor {
    private final HashMap<Integer, AtomicInteger>
            freeSpotsCount = new HashMap<>();
    private final int floor, row, column;
    private final Integer [][] parking;
    private boolean reserved[][];
    
    public ParkingFloor(int floor,
            Integer [][] parking, int[]  vehicleTypes) {
        this.floor = floor;
        this.parking = parking;
        this.row = parking.length;
        this.column = parking[0].length;
        reserved=new boolean[row][column];
        for(int vehicleType: vehicleTypes)
            freeSpotsCount.put(vehicleType, new AtomicInteger(0));

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                int vehicleType= parking[i][j];
                if (vehicleType != 0)
                    freeSpotsCount.get(vehicleType).addAndGet(1);
            }
        }
    }

    public String park(int vehicleType) {
        for (int i = 0; i < row; i++)
            for (int j = 0; j < column; j++) {
                if (parking[i][j] == vehicleType && !reserved[i][j]) {
                    reserved[i][j] = true;
                    freeSpotsCount.get(vehicleType).addAndGet(-1);
                    return floor + "-" + i + "-" + j;
                }
            }
        return "";
    }
    
    public boolean removeVehicle(int row, int col) {
        int vehicleType=parking[row][col];
        if (!reserved[row][col] || vehicleType == 0) return false;
        reserved[row][col] = false;
        freeSpotsCount.get(vehicleType).addAndGet(1);
        return true;
    }

    public int getFreeSpotsCount(int vehicleType) {
        return freeSpotsCount.getOrDefault(
                vehicleType,
                new AtomicInteger(0)).get();
    }
    
}

class SearchManager {
    private final HashMap<String, String> cache
            = new HashMap<>();
    public String search(String query) {
        return cache.getOrDefault(
                query, "");
    }

    public void index(String spotId,
        String vehicleNumber, String ticketId) {
        cache.put(vehicleNumber, spotId);
        cache.put(ticketId, spotId);
    }
}

class ParkManager{
       private ParkingStrategy algorithms[];

       ParkManager(){
           algorithms=new ParkingStrategy[]{
                   new NearestParkingStrategy(),
                   new MostFreeSpotsParkingStrategy()};
       }
       
       String park(ParkingFloor floors[],
               int vehicleType, int parkingStrategy){

             if(parkingStrategy>=0 && parkingStrategy<algorithms.length)
                 return algorithms[parkingStrategy].park(
                         floors, vehicleType);

             return "";
       }
}

class NearestParkingStrategy implements ParkingStrategy{

    public String park(
            ParkingFloor floors[], int vehicleType) {

        for (ParkingFloor floor : floors) {
            String spotId = floor.park(vehicleType);
            if (!spotId.isEmpty()) return spotId;
        }

        return "";
    }
}

class MostFreeSpotsParkingStrategy implements ParkingStrategy{
    public String park(ParkingFloor floors[], int vehicleType) {
        int freeSpotsCount = 0;
        int floorIndex = -1;
        for (int i = 0; i < floors.length; i++) {
            int temp = floors[i].getFreeSpotsCount(vehicleType);
            if (temp <= freeSpotsCount) continue;
            freeSpotsCount = temp;
            floorIndex = i;
        }
        if (floorIndex >= 0) {
            String spotId= floors[floorIndex].park(vehicleType);
            return spotId;
        }
        return "";
    }
}



// uncomment below code in case you are using your local ide and
// comment it back again back when you are pasting completed solution in the online CodeZym editor
// this will help avoid unwanted compilation errors and get method autocomplete in your local code editor.

interface Q07ParkingLotInterface {
    void init(Helper07 helper, Integer [][][] parking);
    String park(int vehicleType, String vehicleNumber, String ticketId, int parkingStrategy);
    boolean removeVehicle(String spotId);
    String searchVehicle(String query);
    int getFreeSpotsCount(int floor, int vehicleType);
}

class Helper07{
    void print(String s){System.out.print(s);} void println(String s){print(s+"\n");}
}
