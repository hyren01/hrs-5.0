package hrds.agent.job.biz.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.netclient.http.HttpClient;
import fd.ng.netserver.conf.HttpServerConf;
import fd.ng.netserver.conf.HttpServerConfBean;
import fd.ng.web.action.ActionResult;
import hrds.commons.entity.Collect_case;
import hrds.commons.entity.Data_store_reg;
import hrds.commons.exception.AppSystemException;
import hrds.commons.utils.AgentActionUtil;
import hrds.commons.utils.PackUtil;

import java.io.*;
import java.util.List;

@DocClass(desc = "解决agent跟服务端做数据，已经通信失败做信息登记", author = "zxz", createdate = "2019/11/22 10:04")
public class CommunicationUtil {
	//通信异常存储目录
	private static final String FOLDER = System.getProperty("user.dir") + File.separator
			+ "CommunicationError" + File.separator;

	static {
		File file = new File(FOLDER);
		if (!file.exists()) {
			if (!file.mkdir()) {
				throw new AppSystemException("创建文件夹" + FOLDER + "失败");
			}
		}
	}

	/**
	 * 保存采集情况信息表
	 */
	public static void saveCollectCase(Collect_case collect_case, String loadMessage) {
		try {
			HttpServerConfBean confBean = HttpServerConf.getHttpServer("hyren_main");
			String url = AgentActionUtil.getServerUrl(confBean, AgentActionUtil.SAVECOLLECTCASE);
			//调用工具类方法给agent发消息，并获取agent响应
			HttpClient.ResponseValue resVal = new HttpClient()
					.addData("collect_case", PackUtil.packMsg(JSONArray.toJSONString(collect_case)))
					.addData("msg", StringUtil.isBlank(loadMessage) ? "excption is null " : loadMessage)
					.post(url);
			ActionResult ar = JsonUtil.toObjectSafety(resVal.getBodyString(), ActionResult.class)
					.orElseThrow(() -> new AppSystemException("连接" + url + "服务异常"));
			if (!ar.isSuccess()) {
				throw new AppSystemException("保存错误信息失败");
			}
		} catch (Exception e) {
			JSONObject object = new JSONObject();
			object.put("collect_case", JSONArray.toJSONString(collect_case));
			object.put("msg", StringUtil.isBlank(loadMessage) ? "excption is null " : loadMessage);
			//连接服务器报错则将信息存到文件
			writeCommunicationErrorFile(AgentActionUtil.SAVECOLLECTCASE,
					object.toString(), e.getMessage(), collect_case.getJob_rs_id());
		}

	}

//	/**
//	 * 保存错误信息
//	 */
//	public static void saveErrorInfo(String job_rs_id, String loadMessage) {
//		try {
//			HttpServerConfBean confBean = HttpServerConf.getHttpServer("hyren_main");
//			String url = AgentActionUtil.getServerUrl(confBean, AgentActionUtil.SAVEERRORINFO);
//			//调用工具类方法给agent发消息，并获取agent响应
//			HttpClient.ResponseValue resVal = new HttpClient()
//					.addData("job_rs_id", job_rs_id)
//					.addData("msg", StringUtil.isBlank(loadMessage) ? "excption is null " : loadMessage)
//					.post(url);
//			ActionResult ar = JsonUtil.toObjectSafety(resVal.getBodyString(), ActionResult.class)
//					.orElseThrow(() -> new AppSystemException("连接" + url + "服务异常"));
//			if (!ar.isSuccess()) {
//				throw new AppSystemException("连接远程服务器报错error_info表信息异常");
//			}
//		} catch (Exception e) {
//			JSONObject object = new JSONObject();
//			object.put("job_rs_id", job_rs_id);
//			object.put("msg", StringUtil.isBlank(loadMessage) ? "excption is null " : loadMessage);
//			//连接服务器报错则将信息存到文件
//			writeCommunicationErrorFile(AgentActionUtil.SAVEERRORINFO,
//					object.toString(), e.getMessage(), job_rs_id);
//		}
//	}

	/**
	 * 批量添加SourceFileAttribute表
	 *
	 * @param addParamsPool 批量添加的数据内容
	 * @param addSql        批量添加的sql
	 * @param job_rs_id     作业id
	 */
	public static void batchAddSourceFileAttribute(List<Object[]> addParamsPool, String addSql,
	                                               String job_rs_id) {
		try {
			HttpServerConfBean confBean = HttpServerConf.getHttpServer("hyren_main");
			String url = AgentActionUtil.getServerUrl(confBean, AgentActionUtil.BATCHADDSOURCEFILEATTRIBUTE);
			//调用工具类方法给agent发消息，并获取agent响应
			HttpClient.ResponseValue resVal = new HttpClient()
					.addData("addSql", addSql)
					.addData("addParamsPool", PackUtil.packMsg(JSONArray.toJSONString(addParamsPool)))
					.post(url);
			ActionResult ar = JsonUtil.toObjectSafety(resVal.getBodyString(), ActionResult.class)
					.orElseThrow(() -> new AppSystemException("连接" + url + "服务异常"));
			if (!ar.isSuccess()) {
				throw new AppSystemException("agent连接服务端批量添加source_file_attribute信息异常");
			}
		} catch (Exception e) {
			JSONObject object = new JSONObject();
			object.put("addSql", addSql);
			object.put("addParamsPool", JSONArray.toJSONString(addParamsPool));
			//连接服务器报错则将信息存到文件
			writeCommunicationErrorFile(AgentActionUtil.BATCHADDSOURCEFILEATTRIBUTE,
					object.toString(), e.getMessage(), job_rs_id);
		}
	}

	/**
	 * 批量添加ftp_transfered(ftp已传输)表
	 *
	 * @param addParamsPool 批量添加的数据内容
	 * @param addSql        批量添加的sql
	 * @param job_rs_id     作业id
	 */
	public static void batchAddFtpTransfer(List<Object[]> addParamsPool, String addSql,
	                                       String job_rs_id) {
		try {
			HttpServerConfBean confBean = HttpServerConf.getHttpServer("hyren_main");
			String url = AgentActionUtil.getServerUrl(confBean, AgentActionUtil.BATCHADDFTPTRANSFER);
			//调用工具类方法给agent发消息，并获取agent响应
			HttpClient.ResponseValue resVal = new HttpClient()
					.addData("addSql", addSql)
					.addData("addParamsPool", PackUtil.packMsg(JSONArray.toJSONString(addParamsPool)))
					.post(url);
			ActionResult ar = JsonUtil.toObjectSafety(resVal.getBodyString(), ActionResult.class)
					.orElseThrow(() -> new AppSystemException("连接" + url + "服务异常"));
			if (!ar.isSuccess()) {
				throw new AppSystemException("agent连接服务端批量添加ftp_transfered信息异常");
			}
		} catch (Exception e) {
			JSONObject object = new JSONObject();
			object.put("addSql", addSql);
			object.put("addParamsPool", JSONArray.toJSONString(addParamsPool));
			//连接服务器报错则将信息存到文件
			writeCommunicationErrorFile(AgentActionUtil.BATCHADDFTPTRANSFER,
					object.toString(), e.getMessage(), job_rs_id);
		}
	}

	/**
	 * 批量更新SourceFileAttribute表
	 *
	 * @param updateParamsPool 批量更新SourceFileAttribute表的内容
	 * @param updateSql        更新sql
	 * @param job_rs_id        作业id
	 */
	public static void batchUpdateSourceFileAttribute(List<Object[]> updateParamsPool, String updateSql,
	                                                  String job_rs_id) {
		try {
			HttpServerConfBean confBean = HttpServerConf.getHttpServer("hyren_main");
			String url = AgentActionUtil.getServerUrl(confBean, AgentActionUtil.BATCHUPDATESOURCEFILEATTRIBUTE);
			//调用工具类方法给agent发消息，并获取agent响应
			HttpClient.ResponseValue resVal = new HttpClient()
					.addData("updateSql", updateSql)
					.addData("updateParamsPool", PackUtil.packMsg(JSONArray.toJSONString(updateParamsPool)))
					.post(url);
			ActionResult ar = JsonUtil.toObjectSafety(resVal.getBodyString(), ActionResult.class)
					.orElseThrow(() -> new AppSystemException("连接" + url + "服务异常"));
			if (!ar.isSuccess()) {
				throw new AppSystemException("agent连接服务端批量更新source_file_attribute信息异常");
			}
		} catch (Exception e) {
			JSONObject object = new JSONObject();
			object.put("updateSql", updateSql);
			object.put("updateParamsPool", JSONArray.toJSONString(updateParamsPool));
			//连接服务器报错则将信息存到文件
			writeCommunicationErrorFile(AgentActionUtil.BATCHUPDATESOURCEFILEATTRIBUTE,
					object.toString(), e.getMessage(), job_rs_id);
		}
	}

	private static void writeCommunicationErrorFile(String methodName, String param, String errorMsg,
	                                                String job_rs_id) {
		JSONObject object = new JSONObject();
		object.put("methodName", methodName);
		object.put("param", param);
		object.put("errorMsg", errorMsg);
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
					FOLDER + job_rs_id + ".error"), true)));
			out.write(object.toString());
			out.newLine();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

//	public static void addSourceFileAttribute(Source_file_attribute file_attribute, String database_id) {
//		try {
//			HttpServerConfBean confBean = HttpServerConf.getHttpServer("hyren_main");
//			String url = AgentActionUtil.getServerUrl(confBean, AgentActionUtil.ADDSOURCEFILEATTRIBUTE);
//			//调用工具类方法给agent发消息，并获取agent响应
//			HttpClient.ResponseValue resVal = new HttpClient()
//					.addData("source_file_attribute", PackUtil.packMsg(JSONObject.toJSONString(file_attribute)))
//					.post(url);
//			ActionResult ar = JsonUtil.toObjectSafety(resVal.getBodyString(), ActionResult.class)
//					.orElseThrow(() -> new AppSystemException("连接" + url + "服务异常"));
//			if (!ar.isSuccess()) {
//				throw new AppSystemException("agent连接服务端批量更新source_file_attribute信息异常");
//			}
//		} catch (Exception e) {
//			JSONObject object = new JSONObject();
//			object.put("source_file_attribute", JSONObject.toJSONString(file_attribute));
//			//连接服务器报错则将信息存到文件
//			writeCommunicationErrorFile(AgentActionUtil.ADDSOURCEFILEATTRIBUTE,
//					object.toString(), e.getMessage(), database_id);
//		}
//	}

	public static void addDataStoreReg(Data_store_reg data_store_reg, String database_id) {
		try {
			HttpServerConfBean confBean = HttpServerConf.getHttpServer("hyren_main");
			String url = AgentActionUtil.getServerUrl(confBean, AgentActionUtil.ADDDATASTOREREG);
			//调用工具类方法给agent发消息，并获取agent响应
			HttpClient.ResponseValue resVal = new HttpClient()
					.addData("data_store_reg", PackUtil.packMsg(JSONObject.toJSONString(data_store_reg)))
					.post(url);
			ActionResult ar = JsonUtil.toObjectSafety(resVal.getBodyString(), ActionResult.class)
					.orElseThrow(() -> new AppSystemException("连接" + url + "服务异常"));
			if (!ar.isSuccess()) {
				throw new AppSystemException("agent连接服务端批量更新data_store_reg信息异常");
			}
		} catch (Exception e) {
			JSONObject object = new JSONObject();
			object.put("data_store_reg", JSONObject.toJSONString(data_store_reg));
			//连接服务器报错则将信息存到文件
			writeCommunicationErrorFile(AgentActionUtil.ADDDATASTOREREG,
					object.toString(), e.getMessage(), database_id);
		}
	}
}
