package simplerace;

public class MyController implements Controller, Constants {
	private SensorModel inputs;

	private boolean backMode = false;//デフォルトは前向き走行
	private boolean isNextNext = false;//NextNextを狙いにいくかどうかのフラグ

	private double reduceSpeedDistance = 0;
	private boolean isMiss = false;
	private int timeCount=0; //1〜1000までの値を取る

	private double nextMaxSpeed = 10;//次の速度制限
	private double currentMaxSpeed = -1; //速度制限
	private double startAngle =0; //旗を取った時のアングル
	private int flagGetTimeCount = 0; //旗を取った時のカウント

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

		//相手との距離を測って次の次の旗を狙いにいくかを決定する
		this.isNextNext = decisionNextNextFlag();

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

		double enemyFlagDistance = this.getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getOtherVehiclePosition ()); //相手の車と次の旗との距離
		enemyFlagDistance = enemyFlagDistance/Math.pow(360000,0.5); //正規化が必要
		if(inputs.getDistanceToNextWaypoint() <= 0.04 || enemyFlagDistance <= 0.038){
			//理想スピードをリセット
			reduceSpeedDistance = 0;
			//次の制限速度をセット
			currentMaxSpeed = enemyFlagDistance <0.05 ? -1 :nextMaxSpeed ;
			//次の次の旗を狙いに行くフラグをリセット
			isNextNext = false;
			//Angleを計算
			startAngle = Math.abs(radian2Degree(getAngle()));
			//旗をとったときのカウントをセット
			flagGetTimeCount = timeCount;
		}

		//減速開始判定処理
		// if( Math.abs(radian2Degree(getAngleToNextWaypoint())) <= 4.0|| Math.abs(radian2Degree(getAngleToNextWaypoint())) >= 176.0){
			if(reduceSpeedDistance >= getDistance()){
				//ブレーキを踏む
				if(Math.abs(radian2Degree(getAngle())) <= 4.0|| Math.abs(radian2Degree(getAngle())) >= 176.0){
					command = (backMode) ? forward : backward;
				}else if(this.getAngle() >= 0){
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
			if(Math.abs(radian2Degree(getAngle())) <= 2.0|| Math.abs(radian2Degree(getAngle())) >= 178.0){
				command = (backMode) ? forward : backward;
			}else if(this.getAngle() >= 0){
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

		//旗取り逃し処理。バックする
		if(inputs.getSpeed()<=2.0) command = defaultThink();
		// int c = missCatchFlag();
		// if(c != -1) command = c;

		//次の次の旗を狙っている状態（isNextNext=Trueのとき）で、旗の上に乗っている状態ならばそこで車をストップさせる
		if(this.isNextNext && this.getDistance()<0.03){
			if(inputs.getSpeed()>=0.1){
				command = backward;
			}
			if(inputs.getSpeed()<0.1){
				command = neutral;
			}
		}

		//時間が残りすくないときは突っ込ませる
		if(this.timeCount >= 960){
			this.isNextNext = false;
			command = defaultThink();
		}

		return command;
	}

    /***
		直進するか、次の旗の方向に向かってハンドルを切るかを決定する
    ***/
	private int defaultThink(){
		double currentAngle = radian2Degree(getAngle());
		double allowGoFowardAngle = calcAllowFowardAngle(); //旗との距離から許容角度を算出

		if(Math.abs(currentAngle) <= allowGoFowardAngle){
			return backMode ? backward : forward; //バックモード時で分岐
		}else{
			return goFowardNextFlagDirection();
		}

	}
	/***
		相手の旗との距離を調べてNextNextを狙いに行くかのフラグを決定する
	***/
	private boolean decisionNextNextFlag(){
		Vector2d myPos = inputs.getPosition();
		Vector2d enePos = inputs.getOtherVehiclePosition();
		Vector2d nextFlagPos =inputs.getNextWaypointPosition();

		double myDistance = getTwoPointDistance(myPos,nextFlagPos);
		double eneDistance = getTwoPointDistance(enePos,nextFlagPos);

		//120フレーム間異常遅れてる場合は回転し続けてると判断して次の旗に向かうようにする
		if(timeCount - flagGetTimeCount >= 120){
			return false;
		}

		if(myDistance - eneDistance > 0){
			return true;
		}else{
			return false;
		}
	}
	/***
		次の旗もしくは次の次の旗との距離を返す
	***/
	private double getDistance(){
		if(this.isNextNext){
			return inputs.getDistanceToNextNextWaypoint();
		}else{
			return inputs.getDistanceToNextWaypoint();
		}
	}
	/***
		次の旗もしくは次の次の旗との角度を返す
	***/
	private double getAngle(){
		if(this.isNextNext){
			return inputs.getAngleToNextNextWaypoint();
		}else{
			return inputs.getAngleToNextWaypoint();
		}
	}

	/***
		バックモード/フロントモードの切り替えを判断する（未完成）
	***/
	private boolean decisionBackMode(){
		boolean result = this.backMode;
		double distance = getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()); //次の旗と次と次の旗との距離
		double angle = Math.abs(radian2Degree(getAngle()));

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
		if(getDistance() < 0.08){
			double angle = Math.abs(radian2Degree(getAngle()));
			if(inputs.getSpeed()>=1.0 && angle >=15.0 && angle <= 165.0){
				isMiss = true;
			}
		}

		if(isMiss){
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
		double distance = this.getDistance();
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
		double angle = Math.abs(radian2Degree(getAngle()));
		idealSpeed = 7.34 - (gap*4.0/150.0) + (distance*1.5/400) - (angle*2.0/180);

		//時間が残りすくないときは突っ込ませる
		if(this.timeCount >= 850) idealSpeed = 100000;
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
		return result + Math.random();
	}


    /***
		コマンド取得系メソッド
    ***/
    private int goFowardNextFlagDirection(){
    	if((int)(Math.random()*100.0) % 3 == 0){
    		if(this.getAngle() >= 0) {
				return  left;
			}
			return right;
    	}
    	if(this.getAngle() >= 0) {
			return  (backMode) ? backwardleft :forwardleft;
		}
		return (backMode) ? backwardright :forwardright;
    }

    private int goFowardNextFlagReverseDirection(){
    	if(this.getAngle() >= 0) {
			return  (backMode) ? backwardright :forwardright;
		}
		return (backMode) ? backwardleft :forwardleft;
    }

    private int goFowardNextNextFlagDirection(){
    	if(this.getAngle() >= 0) {
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
}
