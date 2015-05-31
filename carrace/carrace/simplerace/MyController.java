package simplerace;

public class MyController implements Controller, Constants {

	public static final double THRESHOLD_REDUCE_SPEED_DISTANCE = 0.12;
	public static final double MAX_SPEED = 7.5;

	private SensorModel inputs;

	private boolean backMode = false;

	private double reduceSpeedDistance = 0;
	//
	// private boolean isOk = true;
	// private double currentDis = 0;
	//

    public void reset() {}

    public int control (SensorModel inputs) {
    	this.inputs = inputs;
		int command = neutral;
		
		command = defaultThink();
		
		double idealSpeed = calcSpeedWhenGetNextFlag();

		if(calcReduceSpeedDistance(Math.abs(inputs.getSpeed()),idealSpeed) > reduceSpeedDistance){
			reduceSpeedDistance = calcReduceSpeedDistance(Math.abs(inputs.getSpeed()),idealSpeed);
		}

		if(inputs.getDistanceToNextWaypoint() < 0.05){
			//リセット
			reduceSpeedDistance = 0;
		}

		System.out.println(inputs.getDistanceToNextWaypoint());

		//|| Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) >= 175.0
		if(Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) <= 5.0){
			double therdhold = 3.5;
			 // if(backMode) therdhold = 2.0;

			if(reduceSpeedDistance * therdhold >= inputs.getDistanceToNextWaypoint()){
				//ブレーキを踏む
				System.out.println("ブレーキバック");
				command = (backMode) ? forward : backward;
			}
		}

		//バックモード/フロントモードへの切り替えの決定
		backMode = decisionBackMode();


		//バックする
		if(inputs.getDistanceToNextWaypoint() < 0.1 && Math.abs(radian2Degree(inputs.getAngleToNextWaypoint())) >= 7.0){
			backMode = true;
			command = backward;
		}

		
		/**ログ記録部分**/

		// if(isOk){
		// 	System.out.println(inputs.getSpeed());
		// 	currentDis = inputs.getDistanceToNextWaypoint();
		// 	isOk = false;
		// }else{
		// 	double next = currentDis + 0.01;
		// 	double current = inputs.getDistanceToNextWaypoint();
		// 	if(next < current){
		// 		isOk = true;
		// 	}
		// }

		/**ここまで**/

		/**仮実装部分**/

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

		/*ここまで*/

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
		

		if(!(angle >170) && !(angle < -170) && !(angle < 5 && angle > -5)){
			if(angle < -5.0){
				result = true;
			}else{
				result = false;
			}
			// result = !result;
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
		double idealSpeed = 0; //理想突入スピード

		double distance = getTwoPointDistance(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()); //次の旗と次と次の旗との距離
		double angle = getTwoPointDegree(inputs.getNextWaypointPosition(),inputs.getNextNextWaypointPosition()); //次の旗と次の次の旗との角度

		//それっぽい計算をする

		return idealSpeed;
	}
	/***
		今のスピードから指定スピードに減速するにはどれくらいの距離を要するかを計算する
	***/
	private double calcReduceSpeedDistance(double currentSpeed,double targetSpeed){
		double speeds[] = getSpeedArray();

		int currentNearestNo = (speeds.length - 1);
		int targetNearestNo = (speeds.length - 1);

		double nearestValue = 40;
		double nearestValueTarget = 40;

		for(int i=0;i<speeds.length;i++){
			double delta = Math.abs(currentSpeed - speeds[i]);
			double deltaTarget = Math.abs(targetSpeed - speeds[i]);
			if(nearestValue > delta){
				currentNearestNo = i;
				nearestValue = delta;
			}
			if(nearestValueTarget > deltaTarget){
				targetNearestNo = i;
				nearestValueTarget = deltaTarget;
			}
		}
		// System.out.println(currentNearestNo);
		// System.out.println(targetNearestNo);
		// System.out.println("---------------");

		int result = currentNearestNo - targetNearestNo;

		return (result/100.0);
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
	/***
		データセット取得用
	***/
	private double[] getSpeedArray(){
		double[] result= {
			0.0
			,2.7173860837204
			,3.82471699964782
			,4.89915908004128
			,5.9416891562049745
			,6.619449541996496
			,7.283722496110766
			,7.934776418438162
			,8.572874367711243
			,9.198274167793791
			,9.811228511854694
			,10.411985064468784
			,11.000786561685855
			,11.577870909108306
			,12.143471278017051
			,12.69781619958451
			,13.24112965721278
			,13.773631177034245
			,14.295535916611263
			,14.807054751870698
			,15.308394362308473
			,15.799757314498533
			,16.281342143940012
			,16.753343435275607
			,17.215951900913623
			,17.669354458085444
			,18.113734304369544
			,18.54927099171259
			,18.976140498977507
			,19.394515303047854
			,19.8045644485172
			,20.20645361599171
			,20.600345189033472
			,20.98639831977171
			,21.36476899320825
			,21.73561009024341
			,22.099071449447564
			,22.45529992760356
			,22.804439459044247
			,23.146631113809267
			,23.482013154644463
			,23.810721092867038
			,24.132887743118985
			,24.448643277030914
			,24.758115275818
			,25.06142878182922
			,25.358706349070818
			,25.65006809272431
			,25.935631737679095
			,26.215512666099283
			,26.489823964043907
			,26.758676467159432
			,27.02217880546296
			,27.28043744723425
			,27.53355674203429
			,27.781638962867806
			,28.02478434750674
			,28.263091138991353
			,28.496655625325424
			,28.725572178381448
			,28.94993329203166
			,29.16982961952023
			,29.385350010091777
			,29.596581544890952
			,29.80360957214762
			,30.006517741661884
			,30.205388038602813
			,30.400300816634616
			,30.591334830383587
			,30.778567267258953
			,30.9620737786405
			,31.141928510445556
			,31.31820413308769
			,31.490971870839246
			,31.660301530609544
			,31.826261530150415
			,31.98891892570042
			,32.14833943907898
			,32.30458748424131
			,32.45772619330491
			,32.607817442058135
			,32.75492187496118
			,32.89909892964945
			,33.040406860949425
			,33.178902764416534
			,33.31464259940465
			,33.447681211676496
			,33.57807235556414
			,33.70586871568842
			,33.83112192824622
			,33.95388260187412
			,34.07420033809683
			,34.192123751368705
			,34.30770048871646
			,34.42097724899101
			,34.531999801736085
			,34.64081300568154
			,34.747460826868476
			,34.851986356413796
			,34.95443182792116
			,35.05483863454553
			,35.15324734571808
			,35.24969772353829
			,35.344228738839874
			,35.43687858693696
			,35.52768470305692
			,35.616683777466086
			,35.70391177029451
			,35.789403926065646
			,35.87319478793694
			,35.955318211656994
			,36.03580737924502
			,36.11469481239805
			,36.19201238563133
			,36.26779133915726
			,36.342062291508036
			,36.41485525190703
			,36.486199632394076
			,36.556124259709435
			,36.624657386941216
			,36.691826704941086
			,36.757659353512764
			,36.82218193237786
			,36.885420511923535
			,36.94740064373626
			,37.008147370925904
			,37.06768523824448
			,37.126038302003415
			,37.18323013979355
			,37.23928386001165
			,37.29422211119742
			,37.348067091184596
			,37.40084055607002
			,37.45256382900423
			,37.50325780880704
			,37.552942978411785
			,37.60163941314139
			,37.649366788819876
			,37.696144389722356
			,37.74199111636688
			,37.78692549315118
			,37.83096567583747
			,37.8741294588883
			,37.91643428265642
			,37.957897240431556
			,37.998535085346965
			,38.03836423714856
			,38.07740078882931
			,38.11566051313161
			,38.153158868920286
			,38.189911007428776
			,38.22593177838095
			,38.26123573599117
			,38.295837144844945
			,38.32974998566253
			,38.36298796094785
			,38.39556450052498
			,38.427492766964534
			,38.45878566090194
			,38.489455826249994
			,38.51951565530762
			,38.548977293767
			,38.57785264562104
			,38.606153377973186
			,38.633890925751516
			,38.661076496329066
			,38.68772107405212
			,38.71383542467848
			,38.73943009972737
			,38.7645154407428
			,38.78910158347201
			,38.813198461960916
			,38.8368158125679
			,38.8599631778978
			,38.88264991065763
			,38.904885177435546
			,38.926677962404575
			,38.94803707095272
			,38.968971133240764
			,38.98948860768927
			,39.009597784396256
			,39.02930678848677
			,39.04862358339588
			,39.067555974086304
			,39.08611161020198
			,39.104297989158965
			,39.122122459174705
			,39.13959222223713
			,39.156714337014606
			,39.173495721708015
			,39.18994315684602
			,39.20606328802478
			,39.22186262859309
			,39.237347562284086
			,39.25252434579463
			,39.26739911131332
			,39.281977868998176
			,39.29626650940511
			,39.31027080586795
			,39.32399641683118
			,39.33744888813624
			,39.35063365526232
			,39.36355604552261
			,39.37622128021671
			,39.3886344767404
			,39.40080065065327
			,39.41272471770527
			,39.42441149582293
			,39.43586570705605
			,39.447091979485634
			,39.45809484909387
			,39.4688787615969
			,39.47944807424112
			,39.489807057563716
			,39.499959897118195
			,39.509910695165544
			,39.51966347233175
			,39.52922216923235
			,39.53859064806463
			,39.54777269416814
			,39.556772017554195
			,39.56559225440486
			,39.57423696854221
			,39.58270965286822
			,39.59101373077613
			,39.59915255753369
			,39.607129421638774
			,39.614947546148166
			,39.62261008997982
			,39.630120149189224
			,39.63748075822036
			,39.64469489113178
			,39.65176546279825
			,39.65869533008857
			,39.66548729301981
			,39.67214409588871
			,39.678668428380526
			,39.68506292665575
			,39.6913301744153
			,39.697472703944435
			,39.70349299713594
			,39.709393486492935
			,39.71517655611173
			,39.72084454264511
			,39.72639973624647
			,39.731844381495165
			,39.73718067830341
			,39.74241078280517
			,39.74753680822735
			,39.75256082574363
			,39.75748486531133
			,39.762310916491636
			,39.76704092925345
			,39.77167681476131
			,39.776220446147555
			,39.78067365926922
			,39.78503825344976
			,39.78931599220611
			,39.79350860396121
			,39.79761778274238
			,39.801645188865805
			,39.805592449607374
			,39.80946115986019
			,39.813252882778976
			,39.81696915041167
			,39.82061146431848
			,39.824181296178544
			,39.82768008838459
			,39.83110925462574
			,39.83447018045868
			,39.83776422386756
			,39.8409927158126
			,39.844156960767926
			,39.847258237248646
			,39.8502977983274
			,39.85327687214068
			,39.85619666238509
			,39.85905834880362
			,39.86186308766243
			,39.86461201221795
			,39.86730623317481
			,39.869946839134634
			,39.872534897035855
			,39.87507145258484
			,39.8775575306784
			,39.879994135817896
			,39.88238225251512
			,39.88472284569007
			,39.887016861060836
			,39.889265225525726
			,39.891468847537766
			,39.89362861747176
			,39.89574540798407
			,39.897820074365185
			,39.89985345488532
			,39.9018463711331
			,39.90379962834755
			,39.905714015743435
			,39.907590306830144
			,39.90942925972422
			,39.911231617455705
			,39.91299810826833
			,39.91472944591379
		};
		return result;
	}
}
