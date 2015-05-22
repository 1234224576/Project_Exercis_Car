package simplerace;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyboardControllerOne extends KeyAdapter implements Controller, Constants {

    private int drivingAction = 1;
    private int steeringAction = 1;

    public void reset() {}

    public int control (SensorModel inputs) {
        int action = drivingAction * 3 + steeringAction;

// (1)
//		System.out.println("getSpeed(): " + inputs.getSpeed());

// (2a)
//		System.out.println("getAngleToNextWaypoint(): " + inputs.getAngleToNextWaypoint());

// (2b)
// 		System.out.println("getAngleToNextNextWaypoint(): " + inputs.getAngleToNextNextWaypoint());

// (3a)
// 		System.out.println("getDistanceToNextWaypoint(): " + inputs.getDistanceToNextWaypoint());

// (3b)
// 		System.out.println("getDistanceToNextWaypoint(): " + inputs.getDistanceToNextWaypoint());

// (4a)
// 		System.out.println("getAngleToOtherVehicle(): " + inputs.getAngleToOtherVehicle());

// (4b)
// 		System.out.println("getDistanceToOtherVehicle(): " + inputs.getDistanceToOtherVehicle());

// (5)
// 		System.out.println("otherVehicleIsPresent(): " + inputs.otherVehicleIsPresent());

// (6a)
// 		System.out.println("getPosition().x: " + inputs.getPosition().x);

// (6b)
// 		System.out.println("getPosition().y: " + inputs.getPosition().y);

// (7a)
// 		System.out.println("getVelocity().x: " + inputs.getVelocity().x);

// (7b)
// 		System.out.println("getVelocity().y: " + inputs.getVelocity().y);

// (8)
// 		System.out.println("getOrientation(): " + inputs.getOrientation());

// (9a)
// 		System.out.println("getOtherVehiclePosition().x: " + inputs.getOtherVehiclePosition().x);

// (9b)
// 		System.out.println("getOtherVehiclePosition().y: " + inputs.getOtherVehiclePosition().y);

// (10a)
// 		System.out.println("getOtherVehicleVelocity().x: " + inputs.getOtherVehicleVelocity().x);

// (10b)
// 		System.out.println("getOtherVehicleVelocity().y: " + inputs.getOtherVehicleVelocity().y);

// (11)
// 		System.out.println("getOtherVehicleOrientation(): " + inputs.getOtherVehicleOrientation());

// (12a)
// 		System.out.println("getNextWaypointPosition().x: " + inputs.getNextWaypointPosition().x);

// (12b)
// 		System.out.println("getNextWaypointPosition().y: " + inputs.getNextWaypointPosition().y);

// (13a)
// 		System.out.println("getNextNextWaypointPosition().x: " + inputs.getNextNextWaypointPosition().x);

// (13b)
// 		System.out.println("getNextNextWaypointPosition().y: " + inputs.getNextNextWaypointPosition().y);


        return action;
    }

    public void keyPressed (KeyEvent e) {
        int key = e.getKeyCode ();
        switch (key) {
            case KeyEvent.VK_DOWN:
                drivingAction = 0;
                break;
            case KeyEvent.VK_UP:
                drivingAction = 2;
                break;
            case KeyEvent.VK_LEFT:
                steeringAction = 0;
                break;
            case KeyEvent.VK_RIGHT:
                steeringAction = 2;
                break;
        }

    }

    public void keyReleased (KeyEvent e) {
        int key = e.getKeyCode ();
        switch (key) {
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_UP:
                drivingAction = 1;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
                steeringAction = 1;
                break;
        }

    }

}
