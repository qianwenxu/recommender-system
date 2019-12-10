
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class by_avg_user_item1 {
	static float usermean[]=new float[19835];
	static float itemmean[]=new float[624961];
	static int itemnum[]=new int[624961];
	static float allitemmean=0;
	static long allusermean=0;
	static int allitemmeannum=0;
	int allusermeannum=0;
	static float allmean=0;
	static int allmeannum=0;
	protected static float[] bi1;
	protected static float[] bi2;
	
	public static void main(String args[]) throws Exception{
		loadfile();
		laoditemattribute();
		predict();
	}
	
	public static void loadfile() throws NumberFormatException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File("doc\\train.txt")));
		int userId;
		int itemId = 0;
		int num_rate_items = 0;
		String mLine;
		int score=0;
		int useduserid=-1;
		int userscore=0;
		int rate_num=0;
		while ((mLine = br.readLine()) != null&&(!mLine.equals(""))) {
			String[] splits = mLine.split("\\|");
			userId = Integer.valueOf(splits[0]);
			rate_num = Integer.valueOf(splits[1]);
			//System.out.println(userId+"|"+rate_num);
			for(int i=0;i<rate_num;i++){
				mLine = br.readLine();
				String[] splits1 = mLine.split("  ");
				itemId = Integer.valueOf(splits1[0]);
				score = Integer.valueOf(splits1[1]);
				//System.out.println("itemId"+itemId+"score"+score);
				if(score!=0){
					itemmean[itemId]+=score;
					itemnum[itemId]++;
					allmean+=score;
					allmeannum++;
					//usermean[userId]=mean/num_rate_items;
					if(userId==useduserid){
						userscore+=score;
						num_rate_items++;
					}else{
						if(useduserid!=-1){
							usermean[useduserid]=(float)userscore/(float)num_rate_items;
							if(Float.isNaN(usermean[useduserid])){
								usermean[useduserid]=0;
							}
						}
						userscore=score;
						num_rate_items=1;
					}
				}else if(userId!=useduserid){
					if(useduserid!=-1){
						usermean[useduserid]=(float)userscore/(float)num_rate_items;
						//System.out.println("usermean[useduserid]"+usermean[useduserid]+"num_rate_items"+num_rate_items);
						if(Float.isNaN(usermean[useduserid])){
							usermean[useduserid]=0;
						}
					}
					userscore=0;
					num_rate_items=0;
				}
				useduserid=userId;
			}
		}
		allmean/=allmeannum;
		for(int i=0;i<624961;i++){
			if(itemnum[itemId]!=0){
				itemmean[i]=itemmean[itemId]/itemnum[itemId];
				allitemmean+=itemmean[i];
				allitemmeannum++;
			}else{
				itemmean[i]=-1;
			}
		}
		allitemmean/=allitemmeannum;
		for(int i=0;i<19835;i++){
			System.out.println("allusermean"+allusermean+"usermean[i]"+usermean[i]);
			allusermean = (long) (allusermean + usermean[i]);
		}
		System.out.println("allusermean"+allusermean);
		allusermean=allusermean/19835;
	}
	public static void predict() throws IOException{
		System.out.println("------predicting------");
		int userId, itemId;
		int testnum=0;
		String mLine;
		BufferedReader br = new BufferedReader(new FileReader(new File("doc\\test.txt")));
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(new File("doc\\avg_user_item_result.txt")));
		while ((mLine = br.readLine()) != null) {
			String[] splits = mLine.split("\\|");
			userId = Integer.valueOf(splits[0]);
			testnum = Integer.valueOf(splits[1]);
			bw.write(userId + "|" + testnum + "\n");
			for(int i=0;i<testnum;i++){
				mLine = br.readLine();
				itemId=Integer.valueOf(mLine);
				float tempscore;
				/*if(itemmean[itemId]!=-1){
					tempscore=(float) (0.8*usermean[userId]+0.2*itemmean[itemId]);
				}else{
					tempscore=usermean[userId];
				}*/
				//tempscore=allmean+usermean[userId]+itemmean[itemId]-allusermean-allitemmean;
				//tempscore=(float) (usermean[userId]-0.0035f*bi1[itemId]+0.1740f*bi2[itemId]);
				//System.out.println("allmean"+allmean+"usermean[userId]"+usermean[userId]+"itemmean[itemId]"+itemmean[itemId]+"allusermean"+allusermean+"allitemmean"+allitemmean);
				tempscore=usermean[userId]+1.0008f*itemmean[itemId]-0.0035f*bi1[itemId]+0.1740f*bi2[itemId];
				if(tempscore>100){
					tempscore=100;
				}else if(tempscore<0){
					tempscore=0;
				}
				bw.write(itemId + "  " + tempscore + "\n");
				bw.flush();
			}
		}
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
}
