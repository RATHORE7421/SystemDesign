package src.DesignPatterns;

interface FanState {
    void pull(FanContext context);
}

class OffState implements FanState {
    public void pull(FanContext context) {
        System.out.println("Turning fan to LOW");
        context.setState(new LowState());
    }
}

class LowState implements FanState {
    public void pull(FanContext context) {
        System.out.println("Turning fan to HIGH");
        context.setState(new HighState());
    }
}

class HighState implements FanState {
    public void pull(FanContext context) {
        System.out.println("Turning fan OFF");
        context.setState(new OffState());
    }
}

class FanContext {
    private FanState currentState;

    public FanContext() {
        currentState = new OffState();
    }

    public void setState(FanState state) {
        currentState = state;
    }

    public void pullChain() {
        currentState.pull(this);
    }
}

public class State {
    public static void main(String[] args) {
        FanContext fan = new FanContext();

        fan.pullChain();  // Off -> Low
        fan.pullChain();  // Low -> High
        fan.pullChain();  // High -> Off
        fan.pullChain();  // Off -> Low
    }
}

// Output
// Turning fan to LOW
// Turning fan to HIGH
// Turning fan OFF
// Turning fan to LOW


