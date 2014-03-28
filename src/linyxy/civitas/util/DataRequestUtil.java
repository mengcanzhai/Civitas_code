package linyxy.civitas.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import linyxy.civitas.FullscreenActivity;
import linyxy.civitas.SQLiteActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import structure.DialogUtil;
import structure.HttpUtil;
import structure.SharedPreferenceUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/*
 * 这是一个用来访问网络数据类，
 * 包含基础的数据请求函数，
 * 和用来刷新数据库，sharedP里面的内容的函数
 * 相当于Http＋数据库操作
 */
public class DataRequestUtil extends Activity{

	public static final String pseronStatus = "personStatus";
	public static final String dataR = "DataRequest";
	
	public DataRequestUtil() {
		// TODO Auto-generated constructor stub
		
	}
	

	
	public void process(String action)
	{
//		if(action.equals("getStatus")) getStatus();
//		if(action.equals("getChat")) getChat();
//		if(action.equals("SharedTest")) sharePTest(null);
		
	}
	
	

	/**
	 * 通过post函数向服务器调取内容的函数
	 * 传入 连接位置 请求map
	 * @param conectPosition
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public static JSONObject query(String conectPosition,Map<String, String> requestMap) throws Exception
	{
		// 定义发送请求的URL
		Log.d(dataR, "send request to server| query");
		String url = HttpUtil.BASE_URL + conectPosition+".jsp";
		// 发送请求
		return new JSONObject(HttpUtil.postRequest(url,requestMap));
	}
	
	public static JSONArray requestData(String conectPosition,Map<String, String> requestMap) throws Exception
	{
		// 定义发送请求的URL
		Log.d(dataR, "send request to server | requestData");
		String url = HttpUtil.BASE_URL + conectPosition+".jsp";
		// 发送请求
		return new JSONArray(HttpUtil.postRequest(url,requestMap));
	}
	
	/**
	 * 用来进行登陆的函数
	 * 传入用户名密码，后台进行登陆
	 * @param ctx
	 * @param userName 用户名
	 * @param password 密码
	 * 
	 * @return boolean success/failed
	 */
	public static String login(Context ctx,String userName,String password)
	{
		try
		{
			Map<String,String> values = new HashMap<String,String>();
			values.put("userName", userName);
			values.put("password",password);
			JSONObject result = query("lgoin",values);
			// 用户名结果返回的不是“false”

			
			if (!result.getString("userName").equals("false"))
			{
				SharedPreferenceUtil.updateSharedPreference(ctx, DataRequestUtil.pseronStatus, "userName",result.getString("userName"));
				Log.d(FullscreenActivity.Login, "successfully logined");
				return "loginTrue";
			}
			else 
			{
				return "loginFalse";
			}
			
		}
		catch (Exception e)
		{	
			Log.d(FullscreenActivity.Login, "sever response failed");
			//DialogUtil.showDialog(ctx, "服(wo)务(ye)器(bu)响(zhi)应(dao)异(zen me)常(le)！", false);
			e.printStackTrace();
		}
		
	
		return "badSever";
	}
	
	/**获取当前玩家的属性值
	@input 玩家帐号
	@return 一个包含玩家名字，等级，经验，精力，快乐，健康，饥饿的列表
	 */
	@SuppressWarnings("null")
	public void getStatus(Context ctx)
	{
		Map<String, String> map =getName(ctx);
		
		
		List<String> key = null;
		key.add("name");
		key.add("level");
		key.add("Exp");
		key.add("energy");
		key.add("happy");
		key.add("health");
		key.add("hunger");
		//数据列表
		
		
		//将得到数据存入sharedP
		try {
			JSONObject status = query("getStatus",map);
			
			for(String in:key)
			{
				SharedPreferenceUtil.updateSharedPreference(DataRequestUtil.this,pseronStatus, in, status.getString(in));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("在获取status时出现错误");
			DialogUtil.showDialog(DataRequestUtil.this, "服(wo)务(ye)器(bu)响(zhi)应(dao)异(zen me)常(le)！", false);
			e.printStackTrace();
		}
		
		return;
		
	}
	
	/**获取工作地址
	@input 用户名
	@return String 工作地点，没有工作return null（将存入SharedP）

	 */
	public void getWorkPlace(Context ctx)
	{
		Map<String, String> map = getName(ctx);
		
		try {
			JSONObject w = query("getWorkPlace",map);
			SharedPreferenceUtil.updateSharedPreference(DataRequestUtil.this, pseronStatus, "workPlace", w.getString("workPlace"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	/**
	 * 从服务器获取chats的函数
	 */
	public void getChat(Context ctx)
	{
		Map<String, String> map = getName(ctx);
		
		try {
			JSONArray arr = requestData("getChat",map);
			//从服务器获取的JSONArray需要进行解析，然后存入SQLite中等待调取
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 从服务器获取Notifications的函数
	 */
	public void getNotifications(Context ctx)
	{
		Map<String,String> map = getName(ctx);
		
		try{
			JSONArray arr = requestData("getNotifications",map);	
			List<JSONObject> array = new ArrayList<JSONObject>();
			array = JSONAnalysis.JSONArrayDivider(arr);
			SQLiteActivity SQL = new SQLiteActivity(ctx);
			SQL.refreshData("notifications", array, "content","notificationType");
			
			
		} catch (Exception e) {
			Log.d(dataR, "something wrong when handling the notifications");
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * 用于读取sharedP中的用户名
	 * @return map containing of name
	 */
	public static Map<String, String> getName(Context ctx)
	{
		String username = SharedPreferenceUtil.readSharedPreference(ctx,pseronStatus, "unserNmae");
		Map<String, String> map = new HashMap<String, String>();
		map.put("userName", username);
		return map;
	}
	
	/**
	 * 
	 * 发送一个新的站内信
	 * @param ctx
	 * @param receiver
	 * @param content
	 * @return
	 */
	public static String sendNewLetter(Context ctx,String receiver,String content)
	{
		String result="badServer";
		String cus ="newLetterTrue";
		
		
		
		Map<String,String> letter = getName(ctx);
		letter.put("TODO", "TODO");//TODO
		letter.put("receiver", receiver);
		letter.put("content", content);
		
		try {
			System.out.println("this part is runnable");
			JSONObject re = query("",letter);
			System.out.println("得到的"+re.get(result));
			
			if(re.get(result).equals("TODO"))
				return "newLetterTrue";
			return "newLetterFalse";
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("result is --->"+result);
		
		return cus;
	}
	
	
	//--------test---------test---------test-------------
	public static void sharePTest(Context ctx)
	{
		Log.d(dataR, "Tring to put sample SharedP");
		SharedPreferenceUtil.updateSharedPreference(ctx, pseronStatus, "ShareTest", "sample imput");
		
		String outByThisCtx = SharedPreferenceUtil.readSharedPreference(ctx, pseronStatus, "ShareTest");
		Log.d(dataR, "ctx------>"+outByThisCtx);
		
		String outByAppCtx = SharedPreferenceUtil.readSharedPreference(ctx.getApplicationContext(), pseronStatus, "ShareTest");
		Log.d(dataR, "aplicationCtx----->"+outByAppCtx);
		
		return ;
	}
	
	public static void SQLiteTest(Context ctx)
	{
		SQLiteActivity SQL = new SQLiteActivity(ctx);
		Log.d(dataR, "Opening a SQLite test");
		SQL.tableTest();
	}

}
