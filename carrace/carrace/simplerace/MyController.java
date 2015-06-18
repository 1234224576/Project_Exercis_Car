package simplerace;

public class MyController implements Controller, Constants {
	private SensorModel inputs;

	private boolean backMode = false;//デフォルトは前向き走行
	private double reduceSpeedDistance = 0;
	private boolean isMiss = false;

    public void reset() {}

    public int control (SensorModel inputs) {
    	this.inputs = inputs;
		int command = forward;
		
		//特別な事象がない限りこのメソッドから帰ってくるコマンドが用いられる
		command = defaultThink();
		
		//理想スピードを計算
		double idealSpeed = calcSpeedWhenGetNextFlag();
		//減速開始位置を算出
		// double currentReduce = calcReduceSpeedDistance(Math.abs(inputs.getSpeed()),idealSpeed);
		// if(reduceSpeedDistance < currentReduce) reduceSpeedDistance = currentReduce;
		reduceSpeedDistance = calcReduceSpeedDistance(Math.abs(inputs.getSpeed()),idealSpeed);
		// reduceSpeedDistance = calcReduceSpeedDistance(Math.abs(inputs.getSpeed()),idealSpeed);
		if(inputs.getDistanceToNextWaypoint() < 0.05){
			//理想スピードをリセット
			reduceSpeedDistance = 0;
		}


		//減速開始判定処理
		// if( Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) <= 5.0|| Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) >= 175.0){
			if(reduceSpeedDistance >= inputs.getDistanceToNextWaypoint()){
				//ブレーキを踏む
				System.out.println("break"+inputs.getSpeed());
				System.out.println("distance"+inputs.getDistanceToNextWaypoint());
				System.out.println("reduce"+reduceSpeedDistance);


				if(Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) <= 5.0|| Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) >= 175.0){
					command = (backMode) ? forward : backward;
				}else if(this.inputs.getAngleToNextWaypoint() >= 0){
					command = (backMode) ? forwardleft : backwardleft;
				}else{
					command = (backMode) ? forwardright : backwardright;
				}
			}
		// }

		//バックモード/フロントモードへの切り替えの決定（未完成の為コメントアウト）
		// backMode = decisionBackMode();

		//旗取り逃し処理。バックする
		int c = missCatchFlag();
		// if(c != -1) command = c;
		if(inputs.getSpeed()<0.8) command = defaultThink();
		//旗を取る直前に次の旗へ向かってハンドルを切る
		if(inputs.getDistanceToNextWaypoint() <= 0.05 && inputs.getSpeed() > 0.05){
			command = goFowardNextNextFlagDirection();

		}
		
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
		バックモード/フロントモードの切り替えを判断する（未完成）
	***/
	private boolean decisionBackMode(){
		boolean result = this.backMode;
		double distance = getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()); //次の旗と次と次の旗との距離	
		double angle = Math.abs(radian2Degree(inputs.getAngleToNextWaypoint()));

		if(Math.abs(inputs.getSpeed()) > 3.0) return result;
		// if(distance > 0.5) return result;
		if(angle >= 100 && angle <= 170.0 && !backMode){
			return true;
		}

		if(angle <= 50.0 && backMode){
			return false;
		}
	
		return result;
	}
	/***
		旗を取り逃した時の処理（未完成）
	***/
	private int missCatchFlag(){
		if(inputs.getDistanceToNextWaypoint() < 0.08){
			double angle = Math.abs(radian2Degree(inputs.getAngleToNextWaypoint()));

			if(inputs.getSpeed()>=1.0 && angle >=5.0 && angle <= 175.0){
				isMiss = true;
			}
			else if(inputs.getSpeed()<1.0 && angle >=8.0 && angle <= 171.0){
				isMiss = true;
			}
			else if(inputs.getSpeed()<0.05 && angle >=10.0 && angle <= 170.0){
				isMiss = true;
			}
		}

		if(isMiss && inputs.getDistanceToNextWaypoint() < 0.007){
			return goFowardNextFlagReverseDirection();
		}else{
			isMiss = false;
		}

		return -1;
	}

	/***
		直進許容角度を計算して返す
	***/
	private double calcAllowFowardAngle(){
		double angle = 0;
		double distance = this.inputs.getDistanceToNextWaypoint();
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
		double idealSpeed = 0; //理想突入スピード
		double distance = getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()); //次の旗と次と次の旗との距離
		double flag_angle = getTwoPointDegreeTwo(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()); //次の旗と次の次の旗との角度
		double my_angle = getTwoPointDegreeTwo(inputs.getPosition(),inputs.getNextWaypointPosition());

		double gap = Math.abs(flag_angle - my_angle);
		if(gap > 180) gap -= 360;
		gap = Math.abs(gap);
		
		if(gap > 130){
			//角度がかなり急（真反対）
			if(distance > 250){
				idealSpeed = 2.0;
			}else if(distance > 200){
				idealSpeed = 1.8;
			}else if(distance > 150){
				idealSpeed = 1.5;
			}else if(distance > 100){
				idealSpeed = 1.3;
			}else if(distance > 50){
				idealSpeed = 1.0;
			}
		}else if(gap > 80){
			idealSpeed = 3.0;

			//そこそこ急、直角より曲がる
			if(distance > 250){
				idealSpeed = 5;
			}else if(distance > 200){
				idealSpeed = 4;
			}else if(distance > 150){
				idealSpeed = 4;
			}else if(distance > 100){
				idealSpeed = 3;
			}else if(distance > 50){
				idealSpeed = 2.0;
			}else if(distance > 30){
				idealSpeed = 1.5;
			}else{
				idealSpeed = 1.0;
			}

		}else if (gap > 45){
			if(distance > 250){
				idealSpeed = 5.0;
			}else if(distance > 200){
				idealSpeed = 4.5;
			}else if(distance > 150){
				idealSpeed = 4.2;
			}else if(distance > 100){
				idealSpeed = 4.0;
			}else if(distance > 50){
				idealSpeed = 3.5;
			}else if(distance > 30){
				idealSpeed = 2.2;
			}else{
				idealSpeed = 2.0;
			}
		}else{
			//全然まがらない
			idealSpeed = 5.0;
		}
		return idealSpeed=2.0;
	}
	/***
		今のスピードから指定スピードに減速するにはどれくらいの距離を要するかを計算する
	***/
	private double calcReduceSpeedDistance(double currentSpeed,double targetSpeed){

		double cx = 0;
		double tx = 0;
		if(!backMode){
			//フロントモード
			// cx = Math.pow(2.7,2.2*(Math.log(currentSpeed) - Math.log(6.06)));
			// tx = Math.pow(2.7,2.2*(Math.log(targetSpeed) - Math.log(6.06)));
			cx = Math.pow(2.7,2.00*(Math.log(currentSpeed) - Math.log(19.0)));
			tx = Math.pow(2.7,2.00*(Math.log(targetSpeed) - Math.log(19.0)));

		}else{
			//バックモードこれ再測定しないといけない
			cx = Math.pow(2.7,2.3*(Math.log(currentSpeed) - Math.log(4.4)));
			tx = Math.pow(2.7,2.3*(Math.log(targetSpeed) - Math.log(4.4)));
		}
		//0.1は補正値
		double correctionValue = (0.08 - tx*0.02);
		if(correctionValue<=0) correctionValue = 0;
		double result = cx - tx + correctionValue;
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

    private int goFowardNextFlagReverseDirection(){
    	if(this.inputs.getAngleToNextWaypoint() >= 0) {
			return  (backMode) ? backwardright :forwardright;
		}
		return (backMode) ? backwardleft :forwardleft;
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

	private double getCarDegree() {
	    double radian = inputs.getOrientation();
	    double degree = radian2Degree(radian);
	    return degree;
	}

	private double getTwoPointDegreeTwo(Vector2d v1,Vector2d v2) {
	    double radian = Math.atan2(v2.y - v1.y, v2.x - v1.x);
	    double degree = radian2Degree(radian);
	    return degree;
	}

}
