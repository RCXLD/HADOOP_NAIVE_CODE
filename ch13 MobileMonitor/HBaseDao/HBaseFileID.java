package HBaseIndexAndQuery.HBaseDao;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

public class HBaseFileID {

	HBaseDaoImp imp ;
	 static String tableName="FileNameAndID";
	 static String famliyKey ="n";
	HTable table = null;
	static String cut ="@";
	
	
	
	public HBaseFileID(HBaseDaoImp limp)
	{
		imp = limp;
	}
	
	public HBaseFileID()
	{
		imp = HBaseDaoImp.GetDefaultDao();
	}
	
	public static long GetMaxFileID()
	{
		HBaseDaoImp a = HBaseDaoImp.GetDefaultDao();
		try {
			HTable t = a.getHBaseTable(HBaseFileID.tableName);
			Get get = new Get(Bytes.toBytes(-1L));
			 Result r = t.get(get);
			 byte[] reslut = r.getValue(HBaseFileID.famliyKey.getBytes(), null);
				if( reslut == null)
				{
					return 0;
				} 
				//return 1;
			 long returnValue =  Long.parseLong(Bytes.toString(reslut));
			 return returnValue;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	//String�ĸ�ʽΪ��id@fileName
	public static ArrayList<String> GetAfterFileName(long id)
	{
		ArrayList<String> list = new ArrayList<String>();
		HBaseDaoImp a = HBaseDaoImp.GetDefaultDao();
		try {
			HTable t = a.getHBaseTable(HBaseFileID.tableName);
			Scan scan = new Scan(Bytes.toBytes(id+1));
			ResultScanner scanner = t.getScanner(scan);
			Result[] indexResults = null;
			while(true)
			{
				indexResults = scanner.next(100);
				for(int i = 0; i < indexResults.length ; i++)
				{
					//format  id@fileName
					long  localid		=  Bytes.toLong((indexResults[i].getRow()));
					String fileName = Bytes.toString(indexResults[i].getValue(HBaseFileID.famliyKey.getBytes(), null));
					if( localid == -1 )
					{
						return list;
					}
					list.add(localid+cut+fileName);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return list;
			
		}
	}
	
	public boolean InsertFileAndID(String fileName,long id)
	{
		
		try
		{
		table = imp.getHBaseTable(HBaseFileID.tableName);
		Put put = new Put(Bytes.toBytes(id));
		put.add(HBaseFileID.famliyKey.getBytes(), null, fileName.getBytes());
		table.put(put);
		table.flushCommits();
		return true;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public boolean setMaxID(long id)
	{
		
		try
		{
		table = imp.getHBaseTable(HBaseFileID.tableName);
		Put put = new Put(Bytes.toBytes(-1L));
		put.add(HBaseFileID.famliyKey.getBytes(), null,Bytes.toBytes(id));
		table.put(put);
		table.flushCommits();
		return true;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public  static ArrayList<Pair<Long,String>> ConvertIDToPath(long id , int range ,HBaseDaoImp imp)
	{
		try {
			HTable table = imp.getHBaseTable(HBaseFileID.tableName);
			Scan scan = new Scan(Bytes.toBytes(id));			
			ResultScanner scanner =  table.getScanner(scan);

			Result[] indexResults = null;
			indexResults = scanner.next(range);
			ArrayList<Pair<Long,String>>  list = new ArrayList<Pair<Long,String>>();
			for (int i = 0; i < indexResults.length && indexResults[i] != null; i++)
			{
				Pair<Long,String> pair = new Pair<Long,String>( Bytes.toLong(indexResults[i].getRow()), 
																Bytes.toString(indexResults[i].getValue(HBaseFileID.famliyKey.getBytes(), null))   
															  );
				list.add(pair);
			}
			
			return list;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static void main(String[] args) throws IOException {

		System.out.println("Start Time: "
				+ Calendar.getInstance().getTime().toString());

		HBaseFileID.ConvertIDToPath(1, 100, HBaseDaoImp.GetDefaultDao());
	
	}
	
	
}
