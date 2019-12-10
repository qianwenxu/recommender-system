
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class svd_with_itemattr {
	protected static Map<Integer, Integer> mItemId2Map= new HashMap<Integer, Integer>();
	static int dim=50;
	static int usernum=19835;
	static int itemnum=0;
	static int MaxRate=100;
	static int MinRate=0;
	protected static float[][] p;
	protected static float[][] q;
	protected static float[] bi1;
	protected static float[] bi2;
	private static float sum[];
	static int all_mean=0;
	static List<Node>[] useritemmatrix=new ArrayList[usernum];
	int itemattr[][]=new int[usernum][2];
	static float []usermean=new float[usernum];
	static int nRateNum = 0;
	public static void loadfile() throws NumberFormatException, IOException{
		for(int i=0;i<19835;i++){
			useritemmatrix[i] = new ArrayList<Node>();
		}
		BufferedReader br = new BufferedReader(new FileReader(new File("doc\\train.txt")));
		int userId;
		int useduserid=-1;
		int itemId = 0;
		int num_rate_items = 0;
		String mLine;
		int score=0;
		int userscore=0;
		int rate_num=0;
		while ((mLine = br.readLine()) != null&&(!mLine.equals(""))) {
			String[] splits = mLine.split("\\|");
			userId = Integer.valueOf(splits[0]);
			rate_num = Integer.valueOf(splits[1]);
			for(int i=0;i<rate_num;i++){
				mLine = br.readLine();
				String[] splits1 = mLine.split("  ");
				itemId = Integer.valueOf(splits1[0]);
				score = Integer.valueOf(splits1[1]);
				if(score!=0){
					if (!mItemId2Map.containsKey(itemId)) {
						mItemId2Map.put(itemId, itemnum);
						itemnum++;
					}
					nRateNum++;
					all_mean+=score;
					useritemmatrix[userId].add(new Node(mItemId2Map.get(itemId), score));
					if(userId==useduserid){
						userscore+=score;
						num_rate_items++;
					}else{
						if(useduserid!=-1){
							if(num_rate_items==0){
								usermean[useduserid]=0;
							}else{
								usermean[useduserid]=(float)userscore/(float)num_rate_items;
							}
						}
						userscore=score;
						num_rate_items=1;
					}
				}else{
					if(userId!=useduserid){
						if(useduserid!=-1){
							if(num_rate_items==0){
								usermean[useduserid]=0;
							}else{
								usermean[useduserid]=(float)userscore/(float)num_rate_items;
							}
						}
						userscore=0;
						num_rate_items=0;
					}
				}
				useduserid=userId;
			}
		}
		all_mean/=nRateNum;
	}
	public static void init() {
		p=new float[usernum][dim];
		q=new float[itemnum][dim];
		for (int i = 0; i < usernum; i++)
			for (int j = 0; j < dim; j++)
				p[i][j] = (float) (Math.random()/2);
				//p[i][j] =0.6f;
		for (int i = 0; i < itemnum; i++)
			for (int j = 0; j < dim; j++)
				q[i][j] = (float) (Math.random()/2);
				//q[i][j] = 0.6f;
	}
	public static void train(float gama, float lambda, int nIter) {
		long startTime = System.currentTimeMillis();    //获取开始时间
		sum = new float[dim];
		System.out.println("------start training------");
		long Rmse = 0;
		float mLastRmse = 100000;
		float endrmse=0;
		int nRateNum = 0;
		float rui = 0;
		for (int n = 0; n < nIter; n++) {
			Rmse = 0;
			nRateNum = 0;
			for (int i = 0; i < usernum; i++)
				for (int j = 0; j < useritemmatrix[i].size(); j++) {
					rui = usermean[i]
					        - 0.0035f*bi1[useritemmatrix[i].get(j).getId()]
					        + 0.1470f*bi2[useritemmatrix[i].get(j).getId()]
							+ getInnerProduct(p[i], q[useritemmatrix[i].get(j)
									.getId()]);
					if (rui > MaxRate)
						rui = MaxRate;
					else if (rui < MinRate)
						rui = MinRate;
					float e = useritemmatrix[i].get(j).getRate() - rui;
					if(Float.isNaN(e)){
						System.out.println("e is NaN:1)"+useritemmatrix[i].get(j).getRate()+"2)"+rui);
					}
					for (int k = 0; k < dim; k++) {
						p[i][k] += gama
								* (e * q[useritemmatrix[i].get(j).getId()][k] - lambda
										* p[i][k]);
						q[useritemmatrix[i].get(j).getId()][k] += gama
								* (e * p[i][k] - lambda
										* q[useritemmatrix[i].get(j).getId()][k]);
					}
					Rmse += e * e ;
					//System.out.println(Rmse);
					nRateNum++;
				}
			endrmse = (float) Math.sqrt(Rmse/nRateNum);
			System.out.println("n = " + n + " Rmse = " + endrmse);
			System.out.println("p[0][0]="+p[0][0]);
			System.out.println("q[0][0]="+q[0][0]+",p[0][1]="+p[0][1]+",q[0][1]="+q[0][1]);
			if (endrmse > mLastRmse)
				break;
			mLastRmse = endrmse;
			gama *= 1;
		}
		System.out.println("------training complete!------");
		long endTime = System.currentTimeMillis();    //获取结束时间
		System.out.println("训练运行时间：" + (endTime - startTime)/(1000) + "s");    //输出程序运行时间
	}
	public static void predict() throws IOException{
		System.out.println("------predicting------");
		int userId, itemId;
		int testnum=0;
		String mLine;
		BufferedReader br = new BufferedReader(new FileReader(new File("doc\\test.txt")));
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(new File("doc\\avg_svd_withattr_result.txt")));
		while ((mLine = br.readLine()) != null) {
			String[] splits = mLine.split("\\|");
			userId = Integer.valueOf(splits[0]);
			testnum = Integer.valueOf(splits[1]);
			bw.write(userId + "|" + testnum + "\n");
			for(int i=0;i<testnum;i++){
				mLine = br.readLine();
				itemId=Integer.valueOf(mLine);
				float rui;
				if(mItemId2Map.containsKey(itemId)){
					rui = usermean[userId]
					- 0.0035f*bi1[mItemId2Map.get(itemId)]
					+ 0.1470f*bi2[mItemId2Map.get(itemId)]
					+ getInnerProduct(p[userId],
							q[mItemId2Map.get(itemId)]);
				}
				else{
					rui = (float) usermean[userId] ;
				}
				if (rui > MaxRate||Float.isNaN(rui))
					rui = MaxRate;
				else if (rui < MinRate)
					rui = MinRate;
				bw.write(itemId + "  " + rui + "\n");
				bw.flush();
			}
		}
		br.close();
		if (bw != null)
			bw.close();
		System.out.println("------predicting complete!------");
	}
	public static float getInnerProduct(float[] x, float[] y) {
		float result = 0;
		for (int i = 0; i < x.length; i++) {
			result += x[i] * y[i];
		}
		return result;
	}
	
	public static void laoditemattribute() throws NumberFormatException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File("doc\\itemAttribute.txt")));
		int itemId;
		int attr1=0,attr2=0;
		String mLine;
		bi1=new float[624961];
		bi2=new float[624961];
		while ((mLine = br.readLine()) != null) {
			String[] splits = mLine.split("\\|");
			itemId = Integer.valueOf(splits[0]);
			if(!splits[1].equals("None")){
				attr1 = Integer.valueOf(splits[1]);
			}
			if(!splits[2].equals("None")){
				attr2 = Integer.valueOf(splits[2]);
			}
			bi1[itemId]=attr1/100000;
			bi2[itemId]=attr2/100000;
		}
	}
	
	public static void main(String arg[]) throws NumberFormatException, IOException{
		Runtime r = Runtime.getRuntime();  
		r.gc();
		long startMem = r.freeMemory(); // 开始时的剩余内存  
		loadfile();
		laoditemattribute();
		init();
		train(0.001f,0.4f, 100);
		predict();
		long orz = r.freeMemory() - startMem; // 剩余内存 现在
		System.out.println("current mem: " + orz);
	}
}
class Node {
	private int mId;
	private float mRate;

	public Node(int id, float rate) {
		mId = id;
		mRate = rate;
	}

	public int getId() {
		return mId;
	}

	public float getRate() {
		return mRate;
	}
}