package simplerace;

public class MyController implements Controller, Constants {

	public static final double THRESHOLD_REDUCE_SPEED_DISTANCE = 0.12;
	public static final double MAX_SPEED = 7.5;

	private SensorModel inputs;


    public void reset() {}

    public int control (SensorModel inputs) {
    	this.inputs = inputs;
		int command=neutral;
		
		command = defaultThink();
		


		// if(inputs.getDistanceToNextWaypoint() < THRESHOLD_REDUCE_SPEED_DISTANCE){
		// 	//スピードが1.0以下になるまで減速
		// 	if(inputs.getSpeed() > 1.0){
		// 		command = backward;
		// 	}else{
		// 		//減速済みなら次もしくは更に次の旗の方向へハンドルをきる
		// 		if(inputs.getDistanceToNextWaypoint() < 0.15){
		// 			command = goFowardNextFlagDirection();
		// 		}else{
		// 			command = goFowardNextNextFlagDirection();
		// 		}
		// 	}
		// }

		// if(inputs.getSpeed() > MAX_SPEED){
		// 	command = backward;
		// }


		// System.out.println(radian2Degree(inputs.getAngleToNextWaypoint()));


        return command;
    }

    /***
		思考系メソッド
    ***/
	private int defaultThink(){
		double currentAngle = radian2Degree(inputs.getAngleToNextWaypoint());
		double allowGoFowardAngle = calcAllowFowardAngle();

		if(Math.abs(currentAngle) <= allowGoFowardAngle){
			System.out.println("直進:"+currentAngle);
			return forward;
		}else{
			return goFowardNextFlagDirection();
		}
		
	}

	/***
		直進許容角度を計算して返す
	***/
	private double calcAllowFowardAngle(){
		double angle = 5.0;
		double distance = this.inputs.getDistanceToNextWaypoint();
		System.out.println("Distance:"+distance);
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
		次の旗を取得するときスピードを返す。
		更に、減速するタイミングを計算して返す。
	***/
	private double calcSpeedWhenGetNextFlag(){
		double distance = 10.0;

		return distance;
	}
	private double calcReduceSpeedDistance(){

	}


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

	protected double getTwoPointDistance(Vector2d v, Vector2d v2) {
    	double distance = Math.sqrt((v2.x - v.x) * (v2.x - v.x) + (v2.y - v.y) * (v2.y - v.y));
    	return distance;
	}
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
