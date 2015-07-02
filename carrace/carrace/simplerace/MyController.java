package simplerace;

public class MyController implements Controller, Constants {
	private SensorModel inputs;

	private boolean backMode = false;//デフォルトは前向き走行
	private double reduceSpeedDistance = 0;
	private boolean isMiss = false;
	private int timeCount=0; //1〜1000までの値を取る

	private double nextMaxSpeed = 10;//次の速度制限
	private double currentMaxSpeed = -1; //速度制限
	private double startAngle =0; //旗を取った時のアングル

	private boolean isLine = false;
	private boolean isSpeed = false;

    public void reset() {}

    public int control (SensorModel inputs) {
		if(this.timeCount == 1000){
			//ゲームリセット
			currentMaxSpeed = -1;
			startAngle = 0;
			this.timeCount=0;
		}
    	timeCount++;

    	this.inputs = inputs;
		int command = forward;
		
		//特別な事象がない限りこのメソッドから帰ってくるコマンドが用いられる
		command = defaultThink();

		//初回スタート時
		if(currentMaxSpeed == -1){
			currentMaxSpeed = calcNextMaxSpeed(getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getPosition()));
		}

		//nextMaxSpeedを計算
		if(nextMaxSpeed != calcNextMaxSpeed(getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()))){
			nextMaxSpeed = calcNextMaxSpeed(getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()));
		}
		
		//理想スピードを計算
		double idealSpeed = calcSpeedWhenGetNextFlag();
		//減速開始位置を算出
		double currentReduce = calcReduceSpeedDistance(Math.abs(inputs.getSpeed()),idealSpeed);
		if(reduceSpeedDistance < currentReduce) reduceSpeedDistance = currentReduce;
		reduceSpeedDistance = calcReduceSpeedDistance(Math.abs(inputs.getSpeed()),idealSpeed);
		// reduceSpeedDistance = calcReduceSpeedDistance(Math.abs(inputs.getSpeed()),idealSpeed);
		if(inputs.getDistanceToNextWaypoint() < 0.05){
			//理想スピードをリセット
			reduceSpeedDistance = 0;
			//２点を通る直線をリセット
			isLine = false;
			//次の制限速度をセット
			currentMaxSpeed = nextMaxSpeed;
			//Angleを計算
			startAngle = Math.abs(radian2Degree(inputs.getAngleToNextNextWaypoint()));
		}
		//減速開始判定処理
		// if( Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) <= 4.0|| Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) >= 176.0){
			if(reduceSpeedDistance >= inputs.getDistanceToNextWaypoint()){
				//ブレーキを踏む
				if(Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) <= 4.0|| Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) >= 176.0){
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

		//速度制限にひっかかっていないかを判定
		if(inputs.getSpeed() >= currentMaxSpeed){
			if(Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) <= 2.0|| Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) >= 178.0){
				command = (backMode) ? forward : backward;
			}else if(this.inputs.getAngleToNextWaypoint() >= 0){
				command = (backMode) ? forwardleft : backwardleft;
			}else{
				command = (backMode) ? forwardright : backwardright;
			}
		}

		
		// 旗を取る直前に次の旗へ向かってハンドルを切る
		//&& inputs.getSpeed() > 0.05
		if(inputs.getDistanceToNextWaypoint() <= 0.075&& inputs.getSpeed() > 0.05){
			command = goFowardNextNextFlagDirection();
		}

		/***
			２つの旗が近い時の処理
		***/
		Vector2d targetPoint = getTargetPoint(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition());
		if(!isLine && getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()) < 100.0) {
			System.out.println("in");
			int nd = nearTwoPointDistance(targetPoint);
			command = nd;
			if(targetPoint.x - 100 < inputs.getPosition().x && targetPoint.x + 100 > inputs.getPosition().x) {
				if(targetPoint.y - 100 < inputs.getPosition().y && targetPoint.y + 100 > inputs.getPosition().y) {
					isLine = true;
				}
			}
		}
		System.out.println("out");
		// System.out.println("角度 : "+radian2Degree(inputs.getOrientation()));

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
		２つの旗が近い時の処理
	***/
	private int nearTwoPointDistance(Vector2d targetPoint){
		int result = forward;
		double radian = getTwoPointDegreeThree(inputs.getPosition(), targetPoint);

		if(inputs.getPosition().y < targetPoint.y) {
			radian = 180 + radian;
		} else {
			radian = -(180 - radian);
		}

		isSpeed = false;

		if(targetPoint.x - 400 < inputs.getPosition().x && targetPoint.x + 400 > inputs.getPosition().x) {
			if(targetPoint.y - 400 < inputs.getPosition().y && targetPoint.y + 400 > inputs.getPosition().y) {
				if(inputs.getSpeed() <= 4.0) isSpeed = true;
			}
		} else if(targetPoint.x - 300 < inputs.getPosition().x && targetPoint.x + 300 > inputs.getPosition().x) {
			if(targetPoint.y - 300 < inputs.getPosition().y && targetPoint.y + 300 > inputs.getPosition().y) {
				if(inputs.getSpeed() <= 2.0) isSpeed = true;
			}
		} else if(targetPoint.x - 200 < inputs.getPosition().x && targetPoint.x + 200 > inputs.getPosition().x) {
			if(targetPoint.y - 200 < inputs.getPosition().y && targetPoint.y + 200 > inputs.getPosition().y) {
				if(inputs.getSpeed() <= 1.5) isSpeed = true;
			}
		} else if(targetPoint.x - 150 < inputs.getPosition().x && targetPoint.x + 150 > inputs.getPosition().x) {
			if(targetPoint.y - 150 < inputs.getPosition().y && targetPoint.y + 150 > inputs.getPosition().y) {
				if(inputs.getSpeed() <= 1.0) isSpeed = true;
			}
		} else if(targetPoint.x - 100 < inputs.getPosition().x && targetPoint.x + 100 > inputs.getPosition().x) {
			if(targetPoint.y - 100 < inputs.getPosition().y && targetPoint.y + 100 > inputs.getPosition().y) {
				if(inputs.getSpeed() <= 0.5) isSpeed = true;
			}
		}
		
		if(inputs.getPosition().x > targetPoint.x && inputs.getPosition().y < targetPoint.y) {
			System.out.println("ブロック１");
			if (radian2Degree(inputs.getOrientation()) <= -(180-radian) || radian2Degree(inputs.getOrientation()) >= radian) {
				result = (isSpeed) ? forwardleft : backwardleft;
			} else {
				result = (isSpeed) ? forwardright : backwardright;
			}
		} else if(inputs.getPosition().x < targetPoint.x && inputs.getPosition().y > targetPoint.y) {
			System.out.println("ブロック2");
			
			if (radian2Degree(inputs.getOrientation()) <= radian || radian2Degree(inputs.getOrientation()) >= (180+radian)) {
				result = (isSpeed) ? forwardright : backwardright;
			} else {
				result = (isSpeed) ? forwardleft : backwardleft;
			}
		} else if(inputs.getPosition().x > targetPoint.x && inputs.getPosition().y > targetPoint.y) {
			System.out.println("ブロック3");
			
			if (radian2Degree(inputs.getOrientation()) >= (180+radian) || radian2Degree(inputs.getOrientation()) <= radian) {
				result = (isSpeed) ? forwardright : backwardright;
			} else {
				result = (isSpeed) ? forwardleft : backwardleft;
			}
		} else if(inputs.getPosition().x < targetPoint.x && inputs.getPosition().y < targetPoint.y){
			System.out.println("ブロック4");
			
			if (radian2Degree(inputs.getOrientation()) >= radian || radian2Degree(inputs.getOrientation()) <= -(180-radian)) {
				result = (isSpeed) ? forwardleft : backwardleft;
			} else {
				result = (isSpeed) ? forwardright : backwardright;
			}
		}
		System.out.println(inputs.getSpeed());

		return result;
	}

	/***
		旗を取り逃した時の処理（未完成）
	***/
	private int missCatchFlag(){
		if(inputs.getDistanceToNextWaypoint() < 0.08){
			double angle = Math.abs(radian2Degree(inputs.getAngleToNextWaypoint()));
			if(inputs.getSpeed()>=1.0 && angle >=15.0 && angle <= 165.0){
				isMiss = true;
			}
		}

		if(isMiss){
			System.out.println("ismiss");
			isMiss = false;
			return goFowardNextFlagReverseDirection();
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
		double angle = Math.abs(radian2Degree(inputs.getAngleToNextWaypoint()));
		idealSpeed = 7.34 - (gap*4.0/150.0) + (distance*1.5/400) - (angle*2.0/180);
		return idealSpeed;
	}
	/***
		今のスピードから指定スピードに減速するにはどれくらいの距離を要するかを計算する
	***/
	private double calcReduceSpeedDistance(double currentSpeed,double targetSpeed){

		double cx = 0;
		double tx = 0;
		if(!backMode){
			//フロントモード
			cx = Math.pow(2.7,2.00*(Math.log(currentSpeed) - Math.log(19.0)));
			tx = Math.pow(2.7,2.00*(Math.log(targetSpeed) - Math.log(19.0)));

		}else{
			//バックモードこれ再測定しないといけない
			cx = Math.pow(2.7,2.3*(Math.log(currentSpeed) - Math.log(4.4)));
			tx = Math.pow(2.7,2.3*(Math.log(targetSpeed) - Math.log(4.4)));
		}
		//0.1は補正値
		double correctionValue = (0.10 - tx*0.02);
		if(correctionValue<=0) correctionValue = 0;
		double result = cx - tx + correctionValue;
		return result;
	}

	/***
		制限速度を決定する
	***/
	private double calcNextMaxSpeed(double distance){
		
		//後日ここを修正する
		double result = 10.0;
		if(distance<80){
			result = 3.0;
		}else if(distance < 150){
			result = 4.0;
		}else if(distance < 200){
			result = 5.0;
		}else if(distance < 250){
			result = 7.5;
		}else{
			result = 9.0;
		}	
		return result;
	}


    /***
		コマンド取得系メソッド
    ***/
    private int goFowardNextFlagDirection(){
    	if((int)(Math.random()*100.0) % 3 == 0){
    		if(this.inputs.getAngleToNextWaypoint() >= 0) {
				return  left;
			}
			return right;
    	}
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
			return (backMode) ? left : left;
		}
		return (backMode) ? right : right;
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

	protected Vector2d getTargetPoint(Vector2d v1, Vector2d v2) { // 直線上の座標を求める
		double a, b;
		double l = 100; 
		Vector2d tp1 = new Vector2d();
		Vector2d tp2 = new Vector2d();
		Vector2d tp = new Vector2d();

		a = (v2.y - v1.y) / (v2.x - v1.x);
		b = v1.y - (a * v1.x);

		double ta = 1.0 + a*a;
		double tb = (2.0*a*b) - (2.0*v1.x) - (2.0*a*v1.y); 
		double tc = (v1.x*v1.x) + (b*b) - (2.0*b*v1.y) + ((v1.y*v1.y) - (l*l));

		tp1.x = (-tb + (Math.pow(tb*tb - (4.0*ta*tc),0.5))) / (2.0*ta);
		tp1.y = a*tp1.x + b;
		tp2.x = (-tb - (Math.pow(tb*tb - (4.0*ta*tc),0.5))) / (2.0*ta);
		tp2.y = a*tp2.x + b;

		tp.x = tp1.x;
		tp.y = tp1.y;
		if(getTwoPointDistance(tp1, v2) <= getTwoPointDistance(tp2, v2)){
			tp.x = tp2.x;
			tp.y = tp2.y;
		}

		// System.out.println("1x: "+tp1.x);
		// System.out.println("1y: "+tp1.y);
		// System.out.println("---------------------------------------");
		// System.out.println("2x: "+tp2.x);
		// System.out.println("2y: "+tp2.y);
		// System.out.println("---------------------------------------");
		// System.out.println(getTwoPointDistance(tp1,inputs.getNextWaypointPosition()));
		// System.out.println(getTwoPointDistance(tp2,inputs.getNextWaypointPosition()));

		// System.out.println(ta);
		// System.out.println(tb);
		// System.out.println(tc);
		// System.out.println(tb*tb - 4*ta*tc);
		// System.out.println(a);
		// System.out.println(b);
		// System.out.println(tp1.x);
		// System.out.println(tp1.y);
		// System.out.println(tp2.x);
		// System.out.println(tp2.y);
		// System.out.println(tp.x);
		// System.out.println(tp.y);

    	return tp;
	}

	private double getTwoPointDegreeThree(Vector2d v1,Vector2d v2) {
	    // double radian = Math.cos(getTwoPointDistance(v1, v2) / Math.abs(v1.x-v2.x));
	    // double degree = radian2Degree(radian);

	    double my_angle = getTwoPointDegreeTwo(v1,v2);
		if(my_angle > 180) my_angle -= 360;
		my_angle = Math.abs(my_angle);

		my_angle = 180 - my_angle;

		if(v1.y < v2.y){
			my_angle = my_angle * (-1.0);
		}

		return my_angle;
	    // if(v1.x > v2.x) degree = 180 - degree;
	    // return Math.abs(degree);
	}

}
