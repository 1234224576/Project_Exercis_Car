package simplerace;

public class MyController implements Controller, Constants {

    public void reset() {}

    public int control (SensorModel inputs) {
		int command=neutral;

		if(inputs.getAngleToNextWaypoint() > 0) {
			command=forwardleft;
		}
		else{
			command=forwardright;
		}

//		System.out.print(command);
        return command;
    }
}
