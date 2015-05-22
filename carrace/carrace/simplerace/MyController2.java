package simplerace;

public class MyController2 implements Controller, Constants {

    public void reset() {}

    public int control (SensorModel inputs) {
		int command=neutral;
		command=forwardleft;
        return command;
    }
}
