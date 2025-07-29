package src.LLDConcepts;

import java.util.*;

public class ElevatorSystem implements Q002SmartElevatorGroupInterface {
    int floors, liftsCount;
    Helper02 helper ;
    private Lift lifts[];

    public void init(int floors, int lifts, Helper02 helper) {
        this.floors = floors;
        this.liftsCount =lifts;
        this.helper = helper;
        this.lifts = new Lift[lifts];
        for(int i=0;i<lifts;i++) this.lifts[i]= new Lift();
        // helper.println("Lift system initialized ...");
    }

    /**
     * - This method should return index of lift which is assigned to user
     * 0 <= lift index < lifts.
     * - It will return -1 on failure to assign any lift to user.
     * - see Question details for cases when a lift can't be assigned to a user.
     */
    public int requestLift(int startFloor, int destinationFloor) {
        if(startFloor==destinationFloor) return -1;
        int liftId = -1;
        int timeToReachStart=1000*1000;
        char direction = startFloor<destinationFloor?'U':'D';
        for(int currentLiftIndex = 0; currentLiftIndex< liftsCount; currentLiftIndex++){
            Lift lift = lifts[currentLiftIndex];
            int reachStart = lift.getTimeToReachFloor(startFloor, direction);
            int reachDestination = lift.getTimeToReachFloor(destinationFloor, direction);
            if(reachStart<0 || reachDestination<0 || reachStart>timeToReachStart) continue;
            if(!lift.hasSpace(startFloor, destinationFloor)) continue;
            if(reachStart<timeToReachStart){
                liftId=currentLiftIndex;
                timeToReachStart=reachStart;
            }
        }
        if(liftId>=0 && liftId< liftsCount)
            lifts[liftId].addRequest(startFloor, destinationFloor);
        return liftId;
    }

    /**
     * This method is called every second
     * so that lift states can be appropriately updated.
     * we use this time rather than java.util.Date().time
     */
    public void tick() {
        for(int i = 0; i< liftsCount; i++)
            lifts[i].updateLiftState();
    }

    /**
     * returns list of lift indexes
     * which are going to stop on the given floor,
     * based on requests till now.
     * moveDirection : U for up, D for down, I for idle
     */
    public List<Integer> getLiftsStoppingOnFloor(
            int floor, char moveDirection) {
        List<Integer> liftIds = new ArrayList<Integer>();
        for(int i = 0; i< liftsCount; i++)
            if(lifts[i].hasStop(floor,moveDirection))
                liftIds.add(i);
        return liftIds;
    }

    // returns how many people are on a given lift right now.
    public int getNumberOfPeopleOnLift(int liftId){
        if(liftId<0||liftId>= liftsCount) return 0;
        return lifts[liftId].countPeople(
                lifts[liftId].getCurrentFloor(),
                lifts[liftId].getMoveDirection());
    }

    public String[] getLiftStates() {
        String liftStates[]=new String[liftsCount];
        for(int i = 0; i< liftsCount; i++){
            liftStates[i]=""+ lifts[i].getCurrentFloor()
                    +'-'+ lifts[i].getMoveDirection();
        }
        return liftStates;
    }

   }


class Lift {
    private int currentFloor;
    private ArrayList<LiftRequest> requests;
    private LiftState movingUpState,
            movingDownState, idleState, state;

    Lift(){
        requests = new ArrayList<LiftRequest>();
        movingUpState = new MovingUpState(this);
        movingDownState = new MovingDownState(this);
        idleState = new IdleState(this);
        state = idleState;
    }

    public boolean hasStopInOppositeDirection() {
        char direction = state.getDirection();
        if (direction == 'I') {
            return false;
        }
        for (LiftRequest request : requests) {
            if (request.getMoveDirection() != state.getDirection()) {
                return true;
            }
        }
        return false;
    }

    int getTimeToReachFloor(int floor, char direction){
        return state.getTimeToReachFloor(floor, direction);
    }

    boolean hasStop(int floor , char moveDirection){
        for(int i=0;i<requests.size();i++){
            LiftRequest request = requests.get(i);
            if(request.getStartFloor()==floor
                    || request.getDestinationFloor()==floor){
                if(moveDirection == request.getMoveDirection())
                    return true;
            }
        }
        return false;
    }

    /** count number of people who will be on given floor
     in given direction
     */
    public int countPeople(int floor, char direction){
        int people=0;
        for(LiftRequest request: requests)
            if(request.getMoveDirection()==direction){
                if(direction=='U' && floor>=request.getStartFloor()
                        && floor<request.getDestinationFloor()) people++;
                else if(direction=='D' && floor<=request.getStartFloor()
                        && floor>request.getDestinationFloor()) people++;
            }
        return people;
    }


    char getMoveDirection(){
        return state.getDirection();
    }
    int getCurrentFloor(){
        return currentFloor;
    }

    public boolean hasSpace(int startFloor, int destinationFloor) {
        if(startFloor==destinationFloor) return false;
        char direction = (startFloor < destinationFloor) ? 'U' : 'D';
        if(direction=='U'){
            for(int floor=startFloor;floor<destinationFloor;floor++)
                if (countPeople(floor, direction) >= 10) {
                    return false;
                }
        }
        else{
            for(int floor=startFloor;floor>destinationFloor;floor--)
                if (countPeople(floor, direction) >= 10) {
                    return false;
                }
        }
        return true;
    }

    void updateLiftState(){
        if(requests.size()==0||state.getDirection()=='I'){
            setState('I');
            return;
        }
        state.updateFloor();
        updateRequests();
        if (requests.size() == 0) state = idleState;
        else state.updateDirection();
    }

    // updates people moving out of lift
    public void updateRequests(){
        char direction = state.getDirection();
        if(direction=='I') return;
        ArrayList<LiftRequest> newRequests = new ArrayList<>();
        // removing old requests
        for(LiftRequest request: requests){
            if(direction==request.getMoveDirection()){
                boolean liftPassedDestinationGoingUp = direction=='U' &&
                        currentFloor>=request.getDestinationFloor();
                boolean liftPassedDestinationGoingDown = direction=='D' &&
                        currentFloor<=request.getDestinationFloor();
                if(liftPassedDestinationGoingUp
                    || liftPassedDestinationGoingDown)
                    continue;
            }
            newRequests.add(request);
        }
        requests= newRequests;
    }

    void addRequest(int start, int destination){
        requests.add(new LiftRequest(start, destination));
        if(requests.size()==1){
            char direction = requests.get(0).getMoveDirection();
            if(start>currentFloor) direction='U';
            if(start<currentFloor) direction='D';
            setState(direction);
        }
    }

    public void setState(char direction){
        if(direction=='U'){
            this.state=movingUpState;
            return;
        }
        if(direction=='D'){
            this.state=movingDownState;
            return;
        }
        this.state = idleState;
    }


    public List<LiftRequest> getRequests() {
        return requests;
    }

    public void setCurrentFloor(int currentFloor){
        this.currentFloor= currentFloor;
    }



}


class LiftRequest {
    private int startFloor, destinationFloor;
    LiftRequest(int start, int destination){
        this.startFloor=start;
        this.destinationFloor=destination;
    }
    public int getStartFloor() {
        return startFloor;
    }
    public int getDestinationFloor() {
        return destinationFloor;
    }
    public char getMoveDirection(){
        if(startFloor!=destinationFloor)
            return startFloor<destinationFloor ? 'U':'D';
        return 'I';
    }

    public String toString(){
        return "("+startFloor+", "+destinationFloor+")";
    }
}


abstract class LiftState {
    protected Lift lift;
    LiftState(Lift lift){
        this.lift=lift;
    }
    public void updateFloor(){}
    public void updateDirection(){}
    public abstract char getDirection();
    public abstract int getTimeToReachFloor(int floor, char direction);
}

class MovingUpState extends LiftState{
    MovingUpState(Lift lift) {
        super(lift);
    }

    public int getTimeToReachFloor(int floor, char direction) {
        int currentFloor = lift.getCurrentFloor();
        boolean hasStopOpposite = lift.hasStopInOppositeDirection();
        int maxUpFloor = getMaxUpFloor();
        // NOT ELIGIBLE: there are requests in opposite direction
        if (floor > maxUpFloor && hasStopOpposite) return -1;
        if (direction == 'U') {
            if (floor == currentFloor) return 0;
            // NOT ELIGIBLE : lift has already passed floor
            if (floor < currentFloor) return -1;
            int timeTaken = floor - currentFloor;
            return timeTaken;
        }
        if (floor >= maxUpFloor) {
            int timeTaken = floor - currentFloor;
            return timeTaken;
        }
        int time = maxUpFloor - currentFloor + maxUpFloor - floor;
        return time;
    }


    private int getMaxUpFloor(){
        int floor = -1;
        for(LiftRequest request: lift.getRequests()){
            if(floor<request.getStartFloor())
                floor= request.getStartFloor();
            if(floor<request.getDestinationFloor())
                floor = request.getDestinationFloor();
        }
        return floor;
    }

    public void updateFloor() {
        int maxUpFloor = getMaxUpFloor();
        if(lift.getCurrentFloor()<maxUpFloor) {
            lift.setCurrentFloor(lift.getCurrentFloor() + 1);
        }
    }

    public void updateDirection(){
        if(lift.getCurrentFloor()>=getMaxUpFloor())
            lift.setState('D');
    }

    public char getDirection() {
        return 'U';
    }

}

class MovingDownState extends LiftState{
    MovingDownState(Lift lift) {
        super(lift);
    }

    public int getTimeToReachFloor(int floor, char direction) {
        int currentFloor = lift.getCurrentFloor();
        boolean hasStopOpposite = lift.hasStopInOppositeDirection();
        int minDownFloor = getMinDownFloor();
        // NOT ELIGIBLE: there are requests in opposite direction
        if (floor < minDownFloor && hasStopOpposite) return -1;
        if (direction == 'D') {
            if (floor == currentFloor) return 0;
            // NOT ELIGIBLE : lift has already passed floor
            if (floor > currentFloor) return -1;
            return currentFloor - floor ;
        }
        if (floor <= minDownFloor) return currentFloor - floor;
        return currentFloor - minDownFloor + floor - minDownFloor;
    }


    public void updateFloor() {
        int minDownFloor = getMinDownFloor();
        if(lift.getCurrentFloor()>minDownFloor) {
            lift.setCurrentFloor(lift.getCurrentFloor() - 1);
        }
    }

    public void updateDirection(){
        if(lift.getCurrentFloor()<=getMinDownFloor()) {
            lift.setState('U');
        }
    }

    public char getDirection() {
        return 'D';
    }

   private int getMinDownFloor(){
        int floor = 1000*1000;
        for(LiftRequest request: lift.getRequests()){
            if(floor>request.getStartFloor()) floor= request.getStartFloor();
            if(floor>request.getDestinationFloor()) floor = request.getDestinationFloor();
        }
        return floor;
    }
}

class IdleState extends LiftState{
    IdleState(Lift lift) {
        super(lift);
    }

    public char getDirection() {
        return 'I';
    }

    public int getTimeToReachFloor(int floor, char direction) {
        return Math.abs(floor-lift.getCurrentFloor());
    }
}


// uncomment below code in case you are using your local ide and
// comment it back again back when you are pasting completed solution in the online codezym editor
// this will help avoid unwanted compilation errors and get method autocomplete in your local code editor.

 interface Q002SmartElevatorGroupInterface {
 void init(int floors, int lifts, Helper02 helper);
 int requestLift(int currentFloor, int destinationFloor);
 String[] getLiftStates();
 int getNumberOfPeopleOnLift(int liftId);
 List<Integer> getLiftsStoppingOnFloor(int floor, char moveDirection);
 // This method is called every second so that lift states can be appropriately updated.
 // use this rather than using new java.util.Date().time
 void tick();
 }

 class Helper02 {
 void print(String s){System.out.print(s);}
 void println(String s){System.out.println(s);}
 }
