package simplerace;

public class MyController implements Controller, Constants {

	public static final double THRESHOLD_REDUCE_SPEED_DISTANCE = 0.12;
	public static final double MAX_SPEED = 7.5;

	private SensorModel inputs;

	private boolean backMode = false;
	private double reduceSpeedDistance = 0;

    public void reset() {}

    public int control (SensorModel inputs) {
    	this.inputs = inputs;
		int command = forward;
		
		command = defaultThink();
		
		//理想スピードを計算
		double idealSpeed = calcSpeedWhenGetNextFlag();
		//減速開始位置を算出
		reduceSpeedDistance = calcReduceSpeedDistance(Math.abs(inputs.getSpeed()),idealSpeed);

		if(inputs.getDistanceToNextWaypoint() < 0.05){
			//リセット
			reduceSpeedDistance = 0;
		}


		//減速開始判定処理
		if(Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) <= 5.0|| Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) >= 175.0){
			if(reduceSpeedDistance >= inputs.getDistanceToNextWaypoint()){
				//ブレーキを踏む
				command = (backMode) ? forward : backward;
			}
		}

		//バックモード/フロントモードへの切り替えの決定
		backMode = decisionBackMode();


		//旗取り逃し処理。バックする
		// if(inputs.getDistanceToNextWaypoint() < 0.1 && Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) >= 7.0){
		// 	backMode = true;
		// 	command = backward;
		// }

        return command;
    }

    /***
		直進するか、次の旗の方向に向かってハンドルを切るかを決定する
    ***/
	private int defaultThink(){
		double currentAngle = radian2Degree(inputs.getAngleToNextWaypoint());
		double allowGoFowardAngle = calcAllowFowardAngle(); //旗との距離から許容角度を算出

		if(Math.abs(currentAngle) <= allowGoFowardAngle){
			return backMode ? backward : forward; //バックモード時で分岐
		}else{
			return goFowardNextFlagDirection();
		}
		
	}

	/***
		バックモード/フロントモードの切り替えを判断する
	***/
	private boolean decisionBackMode(){
		boolean result = this.backMode;

		double distance = getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()); //次の旗と次と次の旗との距離
			
		if(inputs.getSpeed() > 1.5) return result;
		if(distance > 0.5) return result;

		double angle = radian2Degree(inputs.getAngleToNextWaypoint());
		

		if((!(angle >170) && !(angle < -170)) || !(angle < 5 && angle > -5)){
			// if(angle < -5.0){
			// 	result = true;
			// }else{
			// 	result = false;
			// }
			System.out.println("ここがよばれた");
			result = !result;
		}
	
		return result;
	}

	/***
		直進許容角度を計算して返す
	***/
	private double calcAllowFowardAngle(){
		double angle = 0;
		double distance = this.inputs.getDistanceToNextWaypoint();
		// System.out.println("Distance:"+distance);
		if(distance < 0.2){
			angle = 5.0;
		}else if(distance < 0.5){
			angle = 2.0;
		}else if(distance < 1){
			angle = 0.5;
		}else{
			angle = 0.1;
		}
		return angle;
	}

	/***
		次の旗を取得時の突入スピードを返す
	***/
	private double calcSpeedWhenGetNextFlag(){
		double idealSpeed = 5.0; //理想突入スピード

		double distance = getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()); //次の旗と次と次の旗との距離
		double angle = getTwoPointDegree(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()); //次の旗と次の次の旗との角度

		//それっぽい計算をする

		return idealSpeed;
	}
	/***
		今のスピードから指定スピードに減速するにはどれくらいの距離を要するかを計算する
	***/
	private double calcReduceSpeedDistance(double currentSpeed,double targetSpeed){

		double cx = Math.pow(2.7,2.2 * (Math.log(currentSpeed) - Math.log(6.06)));
		double tx = Math.pow(2.7,2.2 * (Math.log(targetSpeed) - Math.log(6.06)));

		// System.out.println("0.4希望" + cx);
		// System.out.println("今のスピード"+currentSpeed);
		double result = cx - tx;
		// System.out.println("距離" + result);
		return result;
	}


    /***
		コマンド取得系メソッド
    ***/
    private int goFowardNextFlagDirection(){
    	if(this.inputs.getAngleToNextWaypoint() >= 0) {
			return  (backMode) ? backwardleft :forwardleft;
		}
		return (backMode) ? backwardright :forwardright;
    }

    private int goFowardNextNextFlagDirection(){
    	if(this.inputs.getAngleToNextNextWaypoint() >= 0) {
			return (backMode) ? backwardleft : forwardleft;
		}
		return (backMode) ? backwardright : forwardright;
    }

    /***
		ユーティリティ系メソッド
    ***/

	protected double getTwoPointDistance(Vector2d v, Vector2d v2) {
    	double distance = Math.sqrt((v2.x - v.x) * (v2.x - v.x) + (v2.y - v.y) * (v2.y - v.y));
    	return distance;
	}
    private double getTwoPointDegree(Vector2d v1,Vector2d v2) {
	    double radian = Math.atan2(v2.x - v1.x, v2.y - v1.y);
	    double degree = radian2Degree(radian);
	    return degree;
	}

	private double radian2Degree(double rad){
		double degree = rad * 180d / Math.PI;
	    return degree;
	}

}
