package simplerace;

public class MyController implements Controller, Constants {

	public static final double THRESHOLD_REDUCE_SPEED_DISTANCE = 0.12;
	public static final double MAX_SPEED = 7.5;

	private SensorModel inputs;


    public void reset() {}

    public int control (SensorModel inputs) {
    	this.inputs = inputs;

		int command=neutral;
		
		command = goFowardNextFlagDirection(); //デフォルトは旗の方向へ向かうようにしておく

		if(inputs.getDistanceToNextWaypoint() < THRESHOLD_REDUCE_SPEED_DISTANCE){
			//スピードが1.0以下になるまで減速
			if(inputs.getSpeed() > 1.0){
				command = backward;
			}else{
				//減速済みなら次もしくは更に次の旗の方向へハンドルをきる
				if(inputs.getDistanceToNextWaypoint() < 0.15){
					command = goFowardNextFlagDirection();
				}else{
					command = goFowardNextNextFlagDirection();
				}
			}
		}

		if(inputs.getSpeed() > MAX_SPEED){
			command = backward;
		}


		System.out.println(radian2Degree(inputs.getAngleToNextWaypoint()));


        return command;
    }

    /***
		思考系メソッド
    ***/

    /***
		コマンド取得系メソッド
    ***/
    private int goFowardNextFlagDirection(){
    	if(this.inputs.getAngleToNextWaypoint() > 0) {
			return forwardleft;
		}
		return forwardright;
    }

    private int goFowardNextNextFlagDirection(){
    	if(this.inputs.getAngleToNextNextWaypoint() > 0) {
			return forwardleft;
		}
		return forwardright;
    }

    /***
		ユーティリティ系メソッド
    ***/
    private double getDegree(Vector2d v1,Vector2d v2) {
	    double radian = Math.atan2(v2.x - v1.x, v2.y - v1.y);
	    double degree = radian2Degree(radian);
	    return degree;
	}

	private double radian2Degree(double rad){
		double degree = rad * 180d / Math.PI;
	    return degree;
	}
}
