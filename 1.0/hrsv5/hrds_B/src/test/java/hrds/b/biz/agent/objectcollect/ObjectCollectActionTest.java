package hrds.b.biz.agent.objectcollect;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import fd.ng.core.annotation.Method;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.FileUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.db.resultset.Result;
import fd.ng.netclient.http.HttpClient;
import fd.ng.web.action.ActionResult;
import hrds.commons.codes.*;
import hrds.commons.entity.*;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.key.PrimayKeyGener;
import hrds.testbase.WebBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 对象采集接口测试用例
 */
public class ObjectCollectActionTest extends WebBaseTestCase {

	private static final File DICTINARYFILE = FileUtil.getFile("src/test/java/hrds/b/biz/agent" +
			"/objectcollect/dictionary");
	private static final Type LISTTYPE = new TypeReference<List<Map<String, Object>>>() {
	}.getType();
	private static String bodyString;
	private static ActionResult ar;
	//向object_collect表中初始化的数据条数
	private static final long OBJECT_COLLECT_ROWS = 2L;
	//向object_collect_task表中初始化的数据条数
	private static final long OBJECT_COLLECT_TASK_ROWS = 10L;
	//向object_storage表中初始化的数据条数
	private static final long OBJECT_STORAGE_ROWS = 10L;
	//向object_collect_struct表中初始化的数据条数
	private static final long OBJECT_COLLECT_STRUCT_ROWS = 10L;
	//向object_collect_handle表中初始化的数据条数
	private static final long OBJECT_COLLECT_HANDEL_ROWS = 3L;
	//Agent信息表id
	private static final long AGENT_ID = 10000001L;
	//用户id
	private static final long USER_ID = 9001L;
	//部门ID
	private static final long DEPT_ID = 9002L;
	//对象采集设置表id
	private static final long ODC_ID = 20000001L;
	//对象采集对应信息表任务
	private static final long OCS_ID = 30000001L;
	//对象采集存储设置表存储编号
	private static final long OBJ_STID = 40000001L;
	//对象采集结构信息表结构信息id
	private static final long STRUCT_ID = 50000001L;
	// 对象操作码表信息ID
	private static final long OBJECT_HANDLE_ID = 60000001L;

	/**
	 * 测试用例初始化参数
	 * <p>
	 * 1.造sys_user表数据，用于模拟用户登录
	 * 2.造部门表数据，用于模拟用户登录
	 * 3.造agent_down_info表数据，默认为1条，AGENT_ID为10000001
	 * 4.造Object_collect表数据，默认为2条,ODC_ID为20000001---20000002
	 * 5.造object_collect_task表数据，默认为10条,OCS_ID为30000001---30000010
	 * 6.造object_storage表数据，默认为10条,OBJ_STID为40000001---40000010
	 * 7.造object_collect_struct表数据，默认为10条,STRUCT_ID为50000001---50000010
	 * 8.模拟用户登录
	 */
	@Before
	public void beforeTest() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			//1.造sys_user表数据，用于模拟用户登录
			Sys_user user = new Sys_user();
			user.setUser_id(USER_ID);
			user.setCreate_id(USER_ID);
			user.setRole_id(USER_ID);
			user.setUser_name("测试用户(9001)");
			user.setUser_password("1");
			user.setUseris_admin(IsFlag.Shi.getCode());
			user.setUser_state(IsFlag.Shi.getCode());
			user.setCreate_date(DateUtil.getSysDate());
			user.setCreate_time(DateUtil.getSysTime());
			user.setToken("0");
			user.setValid_time("0");
			user.setDep_id(DEPT_ID);
			assertThat("初始化数据成功", user.add(db), is(1));
			//2.造部门表数据，用于模拟用户登录
			Department_info deptInfo = new Department_info();
			deptInfo.setDep_id(DEPT_ID);
			deptInfo.setDep_name("测试系统参数类部门init-zxz");
			deptInfo.setCreate_date(DateUtil.getSysDate());
			deptInfo.setCreate_time(DateUtil.getSysTime());
			deptInfo.setDep_remark("测试系统参数类部门init-zxz");
			assertThat("初始化数据成功", deptInfo.add(db), is(1));
			//3.造agent_down_info表数据，默认为1条，AGENT_ID为10000001
			Agent_down_info agent_down_info = new Agent_down_info();
			agent_down_info.setDown_id(PrimayKeyGener.getNextId());
			agent_down_info.setUser_id(USER_ID);
			agent_down_info.setAgent_id(AGENT_ID);
			agent_down_info.setAgent_ip("127.0.0.1");
			agent_down_info.setAgent_port("56000");
			agent_down_info.setAgent_type(AgentType.ShuJuKu.getCode());
			agent_down_info.setAgent_name("非结构化采集Agent");
			agent_down_info.setSave_dir("D:\\采集目录\\dhw");
			agent_down_info.setLog_dir("/aaa/ccc/log");
			agent_down_info.setDeploy(IsFlag.Shi.getCode());
			agent_down_info.setAgent_context("/agent");
			agent_down_info.setAgent_pattern("/receive/*");
			agent_down_info.setRemark("测试用例清除数据专用列");
			assertThat("初始化数据成功", agent_down_info.add(db), is(1));
			//4.造Object_collect表数据，默认为2条,ODC_ID为20000001---20000002
			for (int i = 0; i < OBJECT_COLLECT_ROWS; i++) {
				Object_collect object_collect = new Object_collect();
				object_collect.setOdc_id(ODC_ID + i);
				object_collect.setObject_collect_type(ObjectCollectType.HangCaiJi.getCode());
				object_collect.setObj_number("测试对象采集编号");
				object_collect.setObj_collect_name("测试对象采集名称" + i);
				object_collect.setSystem_name("Windows 10");
				object_collect.setHost_name("zhuxi");
				object_collect.setLocal_time(DateUtil.getDateTime());
				object_collect.setServer_date(DateUtil.getSysDate());
				object_collect.setS_date(DateUtil.getSysDate());
				object_collect.setE_date(DateUtil.getSysDate());
				object_collect.setDatabase_code(DataBaseCode.UTF_8.getCode());
				object_collect.setFile_path(DICTINARYFILE.getAbsolutePath());
				object_collect.setIs_sendok(IsFlag.Fou.getCode());
				object_collect.setAgent_id(AGENT_ID);
				object_collect.setIs_dictionary(IsFlag.Shi.getCode());
				if (i == 0) {
					object_collect.setIs_dictionary(IsFlag.Fou.getCode());
					object_collect.setData_date(DateUtil.getSysDate());
					object_collect.setFile_path(DICTINARYFILE.getAbsolutePath());
				} else {
					object_collect.setData_date("");
				}
				object_collect.setFile_suffix("json");
				assertThat("初始化数据成功", object_collect.add(db), is(1));
			}
			//5.造object_collect_task表数据，默认为10条,OCS_ID为30000001---30000010
			for (int i = 0; i < OBJECT_COLLECT_TASK_ROWS; i++) {
				Object_collect_task object_collect_task = new Object_collect_task();
				object_collect_task.setOcs_id(OCS_ID + i);
				object_collect_task.setAgent_id(AGENT_ID);
				object_collect_task.setEn_name("aaa" + i);
				object_collect_task.setZh_name("测试aaa" + i);
				object_collect_task.setCollect_data_type(CollectDataType.JSON.getCode());
				object_collect_task.setDatabase_code(DataBaseCode.UTF_8.getCode());
				object_collect_task.setOdc_id(ODC_ID);
				object_collect_task.setUpdatetype(UpdateType.DirectUpdate.getCode());
				object_collect_task.setFirstline("aaa");
				if (i == 0) {
					object_collect_task.setFirstline("[{\"columns\":[{\"column_id\":\"0\"," +
							"\"is_key\":\"1\",\"columnposition\":\"date\",\"column_name\":\"date\"," +
							"\"is_solr\":\"1\",\"column_type\":\"decimal(38,18)\",\"is_operate\":\"1\"," +
							"\"is_rowkey\":\"0\",\"is_hbase\":\"0\"}],\"handletype\":{\"insert\":\"\"," +
							"\"update\":\"\",\"delete\":\"\"},\"updatetype\":\"0\"," +
							"\"table_cn_name\":\"t_executedpersons\",\"table_name\":\"t_executedpersons\"}]");
				}
				assertThat("初始化数据成功", object_collect_task.add(db), is(1));
			}
			//7.造object_collect_struct表数据，默认为10条,STRUCT_ID为50000001---50000010
			for (int i = 0; i < OBJECT_COLLECT_STRUCT_ROWS; i++) {
				Object_collect_struct object_collect_struct = new Object_collect_struct();
				object_collect_struct.setStruct_id(STRUCT_ID + i);
				object_collect_struct.setOcs_id(OCS_ID + i);
				object_collect_struct.setColumn_name("testcol" + i);
				object_collect_struct.setData_desc("测试对象中文描述" + i);
				object_collect_struct.setColumn_type("decimal(18,32)");
				object_collect_struct.setColumnposition("columns." + "testcol" + i);
				if (i == 5) {
					object_collect_struct.setIs_operate(IsFlag.Shi.getCode());
				} else if (i == 6) {
					object_collect_struct.setIs_operate(IsFlag.Shi.getCode());
				} else if (i == 7) {
					object_collect_struct.setIs_operate(IsFlag.Fou.getCode());
				} else if (i == 8) {
					object_collect_struct.setIs_operate(IsFlag.Shi.getCode());
				} else if (i == 9) {
					object_collect_struct.setIs_operate(IsFlag.Shi.getCode());
				} else {
					object_collect_struct.setIs_operate(IsFlag.Shi.getCode());
				}
				assertThat("初始化数据成功", object_collect_struct.add(db), is(1));
			}
			// 8.构造object_handle_type表数据，默认30条object_handle_id为6000001-600000030
			for (int i = 0; i < OBJECT_COLLECT_HANDEL_ROWS; i++) {
				Object_handle_type object_handle_type = new Object_handle_type();
				object_handle_type.setOcs_id(OCS_ID + i);
				OperationType[] values = OperationType.values();
				for (int j = 0; j < values.length; j++) {
					object_handle_type.setObject_handle_id(OBJECT_HANDLE_ID + i + j);
					object_handle_type.setHandle_type(values[j].getCode());
					object_handle_type.setHandle_value(values[j].getValue());
					assertThat("初始化数据成功", object_handle_type.add(db), is(1));
				}
			}
			// 9.agent_info表数据
			Agent_info agent_info = new Agent_info();
			agent_info.setUser_id(USER_ID);
			agent_info.setSource_id("1000001");
			agent_info.setAgent_id(AGENT_ID);
			agent_info.setAgent_type(AgentType.ShuJuKu.getCode());
			agent_info.setAgent_name("非结构化采集Agent");
			agent_info.setAgent_ip("127.0.0.1");
			agent_info.setAgent_port("56000");
			agent_info.setAgent_status(AgentStatus.YiLianJie.getCode());
			agent_info.setCreate_date(DateUtil.getSysDate());
			agent_info.setCreate_time(DateUtil.getSysTime());
			agent_info.add(db);

			SqlOperator.commitTransaction(db);
		}
		//8.模拟用户登录
		String responseValue = new HttpClient().buildSession()
				.addData("user_id", USER_ID)
				.addData("password", "1")
				.post("http://127.0.0.1:8888/A/action/hrds/a/biz/login/login").getBodyString();
		ActionResult ar = JsonUtil.toObjectSafety(responseValue, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败"));
		assertThat(ar.isSuccess(), is(true));
	}

	@Method(desc = "查询对象采集信息测试用例",
			logicStep = "1.agent_id不为空，odc_id为空查询信息，agent_id为正确造数据的值" +
					"2.agent_id不为空，odc_id为空查询信息，agent_id为不正确的值" +
					"3.agent_id不为空，odc_id也不为空查询信息，agent_id和odc_id为正确造数据的值" +
					"4.agent_id不为空，odc_id也不为空查询信息，agent_id为正确造数据的值和odc_id为不正确的值")
	@Test
	public void searchObjectCollectTest() {
		//1.agent_id不为空，odc_id为空查询信息，agent_id为正确造数据的值
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.post(getActionUrl("searchObjectCollect")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		assertThat(StringUtil.isBlank(ar.getDataForMap().get("osName").toString()), is(false));

		//2.agent_id不为空，odc_id为空查询信息，agent_id为不正确的值
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID + 111)
				.post(getActionUrl("searchObjectCollect")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));

		//3.agent_id不为空，odc_id也不为空查询信息，agent_id和odc_id为正确造数据的值
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("odc_id", ODC_ID)
				.post(getActionUrl("searchObjectCollect")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		assertThat(ar.getDataForMap().get("localDate"), is(DateUtil.getSysDate()));
		assertThat(StringUtil.isBlank(ar.getDataForMap().get("object_collect_info").toString()), is(false));

		//4.agent_id不为空，odc_id也不为空查询信息，agent_id为正确造数据的值和odc_id为不正确的值
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("odc_id", ODC_ID + 111)
				.post(getActionUrl("searchObjectCollect")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
	}

	/**
	 * addObjectCollect保存对象采集设置表测试用例
	 * <p>
	 * 1.添加一个正确的半结构化采集设置表
	 * 2.添加一个半结构化采集，但ftp采集任务名称重复
	 * 3.添加一个半结构化采集，但是file_path格式不正确
	 * 4.添加一个半结构化采集，但是database_code格式不正确
	 */
	@Test
	public void addObjectCollectTest() {
		//1.添加一个正确的半结构化采集设置表
		bodyString = new HttpClient()
				.addData("object_collect_type", ObjectCollectType.HangCaiJi.getCode())
				.addData("obj_number", "qqwwtt")
				.addData("obj_collect_name", "测试对象采集名称1112")
				.addData("system_name", "Windows 10")
				.addData("host_name", "zhuxi")
				.addData("local_time", DateUtil.getDateTime())
				.addData("server_date", DateUtil.getSysDate())
				.addData("s_date", DateUtil.getSysDate())
				.addData("e_date", DateUtil.getSysDate())
				.addData("database_code", DataBaseCode.UTF_8.getCode())
				.addData("run_way", ExecuteWay.MingLingChuFa.getCode())
				.addData("file_path", DICTINARYFILE.getAbsolutePath())
				.addData("is_sendok", IsFlag.Fou.getCode())
				.addData("agent_id", AGENT_ID)
				.addData("is_dictionary", IsFlag.Shi.getCode())
				.addData("data_date", "20200301")
				.addData("file_suffix", "json")
				.post(getActionUrl("addObjectCollect")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			long optionalLong = SqlOperator.queryNumber(db, "select count(1) count from "
					+ Object_collect.TableName + " WHERE agent_id = ?", AGENT_ID).orElseThrow(
					() -> new BusinessException("该查询有且仅有一条数据"));
			assertThat("校验object_collect表数据量正确", optionalLong, is(OBJECT_COLLECT_ROWS + 1));
			Object_collect collect = SqlOperator.queryOneObject(db, Object_collect.class, "select * from "
					+ Object_collect.TableName + " WHERE obj_collect_name = ?", "测试对象采集名称1112")
					.orElseThrow(() -> new BusinessException("测试用例异常"));
			assertThat("校验object_collect表数据量正确", collect.getFile_path()
					, is(DICTINARYFILE.getAbsolutePath()));
			assertThat("校验object_collect表数据量正确", collect.getObj_number()
					, is("qqwwtt"));
		}

		//2.添加一个半结构化采集，但半结构化采集任务名称重复
		bodyString = new HttpClient()
				.addData("object_collect_type", ObjectCollectType.HangCaiJi.getCode())
				.addData("obj_number", "qqww")
				.addData("obj_collect_name", "测试对象采集名称1")
				.addData("system_name", "Windows 10")
				.addData("host_name", "zhuxi")
				.addData("local_time", DateUtil.getDateTime())
				.addData("server_date", DateUtil.getSysDate())
				.addData("s_date", DateUtil.getSysDate())
				.addData("e_date", DateUtil.getSysDate())
				.addData("database_code", DataBaseCode.UTF_8.getCode())
				.addData("run_way", ExecuteWay.MingLingChuFa.getCode())
				.addData("file_path", "/aaaa/ccc/ddd")
				.addData("is_sendok", IsFlag.Fou.getCode())
				.addData("agent_id", AGENT_ID)
				.post(getActionUrl("addObjectCollect")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));

//		//3.添加一个半结构化采集，但是file_path格式不正确
//		bodyString = new HttpClient()
//				.addData("object_collect_type", ObjectCollectType.HangCaiJi.getCode())
//				.addData("obj_number", "qqwwttr")
//				.addData("obj_collect_name", "测试对象采集名称qwerr222")
//				.addData("system_name", "Windows 10")
//				.addData("host_name", "zhuxi")
//				.addData("local_time", DateUtil.getDateTime())
//				.addData("server_date", DateUtil.getSysDate())
//				.addData("s_date", DateUtil.getSysDate())
//				.addData("e_date", DateUtil.getSysDate())
//				.addData("database_code", DataBaseCode.UTF_8.getCode())
//				.addData("run_way", ExecuteWay.MingLingChuFa.getCode())
//				.addData("file_path", "adsad,cacsadwqwq/qweqdsaa\\asdas\\sad")
//				.addData("is_sendok", IsFlag.Fou.getCode())
//				.addData("agent_id", AGENT_ID)
//				.post(getActionUrl("addObjectCollect")).getBodyString();
//		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
//				-> new BusinessException("连接失败！"));
//		assertThat(ar.isSuccess(), is(false));
//
//		//4.添加一个半结构化采集，但是database_code格式不正确
//		bodyString = new HttpClient()
//				.addData("object_collect_type", ObjectCollectType.HangCaiJi.getCode())
//				.addData("obj_number", "qqwwttyy")
//				.addData("obj_collect_name", "测试对象采集名称qwerr")
//				.addData("system_name", "Windows 10")
//				.addData("host_name", "zhuxi")
//				.addData("local_time", DateUtil.getDateTime())
//				.addData("server_date", DateUtil.getSysDate())
//				.addData("s_date", DateUtil.getSysDate())
//				.addData("e_date", DateUtil.getSysDate())
//				.addData("database_code", "UTF-8")
//				.addData("run_way", ExecuteWay.MingLingChuFa.getCode())
//				.addData("file_path", "/aaaa/ccc/ddd")
//				.addData("is_sendok", IsFlag.Fou.getCode())
//				.addData("agent_id", AGENT_ID)
//				.post(getActionUrl("addObjectCollect")).getBodyString();
//		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
//				-> new BusinessException("连接失败！"));
//		assertThat(ar.isSuccess(), is(false));
	}

	/**
	 * updateObjectCollect更新对象采集设置表测试用例
	 * <p>
	 * 1.更新一个正确的半结构化采集设置表
	 * 2.更新一个半结构化采集，但ftp采集任务名称重复和其他任务名称重复
	 * 3.更新一个半结构化采集，但是run_way格式不正确
	 * 4.更新一个半结构化采集，但是obj_number格式不正确
	 */
	@Test
	public void updateObjectCollectTest() {
		//1.更新一个正确的半结构化采集设置表
		bodyString = new HttpClient()
				.addData("odc_id", ODC_ID)
				.addData("object_collect_type", ObjectCollectType.HangCaiJi.getCode())
				.addData("obj_number", "hahahahxianshi")
				.addData("obj_collect_name", "测试对象采集编号0")
				.addData("system_name", "Windows 10")
				.addData("host_name", "zhuxi")
				.addData("local_time", DateUtil.getDateTime())
				.addData("server_date", DateUtil.getSysDate())
				.addData("s_date", DateUtil.getSysDate())
				.addData("e_date", DateUtil.getSysDate())
				.addData("database_code", DataBaseCode.UTF_8.getCode())
				.addData("run_way", ExecuteWay.MingLingChuFa.getCode())
				.addData("file_path", DICTINARYFILE.getAbsolutePath())
				.addData("is_sendok", IsFlag.Fou.getCode())
				.addData("agent_id", AGENT_ID)
				.addData("is_dictionary", IsFlag.Shi.getCode())
//				.addData("data_date","20200301")
				.addData("file_suffix", "json")
				.post(getActionUrl("updateObjectCollect")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			long optionalLong = SqlOperator.queryNumber(db, "select count(1) count from "
					+ Object_collect.TableName + " where agent_id = ? ", AGENT_ID)
					.orElseThrow(() -> new BusinessException("该查询有且仅有一条数据"));
			assertThat("校验object_collect表数据量正确", optionalLong, is(OBJECT_COLLECT_ROWS));
			Object_collect collect = SqlOperator.queryOneObject(db, Object_collect.class, "select * from "
					+ Object_collect.TableName + " WHERE obj_collect_name = ?", "测试对象采集编号0")
					.orElseThrow(() -> new BusinessException("测试用例异常"));
			assertThat("校验object_collect表数据量正确", collect.getFile_path()
					, is(DICTINARYFILE.getAbsolutePath()));
			assertThat("校验object_collect表数据量正确", collect.getObj_number()
					, is("hahahahxianshi"));
		}

		//2.更新一个半结构化采集，但ftp采集任务名称重复和其他任务名称重复
		bodyString = new HttpClient()
				.addData("odc_id", ODC_ID)
				.addData("object_collect_type", ObjectCollectType.HangCaiJi.getCode())
				.addData("obj_number", "ttyy")
				.addData("obj_collect_name", "测试对象采集名称1")
				.addData("system_name", "Windows 10")
				.addData("host_name", "zhuxi")
				.addData("local_time", DateUtil.getDateTime())
				.addData("server_date", DateUtil.getSysDate())
				.addData("s_date", DateUtil.getSysDate())
				.addData("e_date", DateUtil.getSysDate())
				.addData("database_code", DataBaseCode.UTF_8.getCode())
				.addData("run_way", ExecuteWay.MingLingChuFa.getCode())
				.addData("file_path", "/aaaa/ccc/ddd")
				.addData("is_sendok", IsFlag.Fou.getCode())
				.addData("agent_id", AGENT_ID)
				.post(getActionUrl("updateObjectCollect")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));

//		//3.更新一个半结构化采集，但是run_way格式不正确
//		bodyString = new HttpClient()
//				.addData("odc_id", ODC_ID)
//				.addData("object_collect_type", ObjectCollectType.HangCaiJi.getCode())
//				.addData("obj_number", "ttyy77")
//				.addData("obj_collect_name", "测试对象采集名称1")
//				.addData("system_name", "Windows 10")
//				.addData("host_name", "zhuxi")
//				.addData("local_time", DateUtil.getDateTime())
//				.addData("server_date", DateUtil.getSysDate())
//				.addData("s_date", DateUtil.getSysDate())
//				.addData("e_date", DateUtil.getSysDate())
//				.addData("database_code", DataBaseCode.UTF_8.getCode())
//				.addData("run_way", "我必须是代码项")
//				.addData("file_path", "/aaaa/ccc/ddd")
//				.addData("is_sendok", IsFlag.Fou.getCode())
//				.addData("agent_id", AGENT_ID)
//				.post(getActionUrl("updateObjectCollect")).getBodyString();
//		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
//				-> new BusinessException("连接失败！"));
//		assertThat(ar.isSuccess(), is(false));
//
//		//4.更新一个半结构化采集，但是obj_number格式不正确
//		bodyString = new HttpClient()
//				.addData("odc_id", ODC_ID)
//				.addData("object_collect_type", ObjectCollectType.HangCaiJi.getCode())
//				.addData("obj_number", "我不能是中文")
//				.addData("obj_collect_name", "测试对象采集名称1")
//				.addData("system_name", "Windows 10")
//				.addData("host_name", "zhuxi")
//				.addData("local_time", DateUtil.getDateTime())
//				.addData("server_date", DateUtil.getSysDate())
//				.addData("s_date", DateUtil.getSysDate())
//				.addData("e_date", DateUtil.getSysDate())
//				.addData("database_code", DataBaseCode.UTF_8.getCode())
//				.addData("run_way", ExecuteWay.MingLingChuFa.getCode())
//				.addData("file_path", "/aaaa/ccc/ddd")
//				.addData("is_sendok", IsFlag.Fou.getCode())
//				.addData("agent_id", AGENT_ID)
//				.post(getActionUrl("updateObjectCollect")).getBodyString();
//		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
//				-> new BusinessException("连接失败！"));
//		assertThat(ar.isSuccess(), is(false));
	}

	@Method(desc = "根据对象采集id查询对象采集对应信息表",
			logicStep = "1.使用正确的odc_id查询OBJECT_COLLECT_TASK表" +
					"2.使用错误的odc_id查询OBJECT_COLLECT_TASK表" +
					"3.使用错误的agent_id查询OBJECT_COLLECT_TASK表" +
					"注：此方法没有写到四个及以上的测试用例是因为此方法只是一个查询方法，只有正确和错误3种情况")
	@Test
	public void searchObjectCollectTaskTest() {
		//1.使用正确的odc_id查询OBJECT_COLLECT_TASK表
		bodyString = new HttpClient()
				.addData("odc_id", ODC_ID)
				.addData("agent_id", AGENT_ID)
				.post(getActionUrl("searchObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		assertThat(ar.getDataForResult().getRowCount(), is(Integer.parseInt(OBJECT_COLLECT_TASK_ROWS + "")));
		for (int i = 0; i < OBJECT_COLLECT_TASK_ROWS; i++) {
			assertThat(ar.getDataForResult().getString(i, "updatetype")
					, is(IsFlag.Fou.getCode()));
			assertThat(ar.getDataForResult().getString(i, "database_code")
					, is(DataBaseCode.UTF_8.getCode()));
			assertThat(ar.getDataForResult().getString(i, "collect_data_type")
					, is(CollectDataType.JSON.getCode()));
			if (i == 0) {
				assertThat(ar.getDataForResult().getString(i, "firstline")
						, is("[{\"columns\":[{\"column_id\":\"0\"," +
								"\"is_key\":\"1\",\"columnposition\":\"date\",\"column_name\":\"date\"," +
								"\"is_solr\":\"1\",\"column_type\":\"decimal(38,18)\",\"is_operate\":\"1\"," +
								"\"is_rowkey\":\"0\",\"is_hbase\":\"0\"}],\"handletype\":{\"insert\":\"\"," +
								"\"update\":\"\",\"delete\":\"\"},\"updatetype\":\"0\"," +
								"\"table_cn_name\":\"t_executedpersons\",\"table_name\":\"t_executedpersons\"}]"));
			} else {
				assertThat(ar.getDataForResult().getString(i, "firstline"), is("aaa"));
			}
			assertThat(ar.getDataForResult().getString(i, "en_name")
					, is("aaa" + i));
			assertThat(ar.getDataForResult().getString(i, "zh_name")
					, is("测试aaa" + i));
		}

		//2.使用错误的odc_id查询OBJECT_COLLECT_TASK表
		bodyString = new HttpClient()
				.addData("odc_id", "27266381")
				.addData("agent_id", AGENT_ID)
				.post(getActionUrl("searchObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		assertThat(ar.getDataForResult().isEmpty(), is(true));
		//3.使用错误的agent_id查询OBJECT_COLLECT_TASK表
		bodyString = new HttpClient()
				.addData("odc_id", ODC_ID)
				.addData("agent_id", "27266381")
				.post(getActionUrl("searchObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		assertThat(ar.getDataForResult().isEmpty(), is(true));
	}

	/**
	 * deleteObjectCollectTask删除对象采集任务测试用例
	 * <p>
	 * 1.使用ocs_id为30000007删除object_collect_task表，ocs_id为30000001对应的对象采集存储设置下有数据
	 * 2.使用ocs_id为30000001删除object_collect_task表，ocs_id为30000007对应的对象采集结构信息表
	 * 3.使用不存在的ocs_id删除object_collect_task表
	 * 注：此方法没有写到四个及以上的测试用例是因为此方法只是一个查询方法，只有正确和错误两种情况
	 */
	@Test
	public void deleteObjectCollectTaskTest() {
		//1.使用ocs_id为30000007删除object_collect_task表，ocs_id为30000001对应的对象采集存储设置下有数据
		bodyString = new HttpClient()
				.addData("ocs_id", OCS_ID + 6)
				.post(getActionUrl("deleteObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));

		//2.使用ocs_id为30000001删除object_collect_task表，ocs_id为30000007对应的对象采集结构信息表
		bodyString = new HttpClient()
				.addData("ocs_id", OCS_ID)
				.post(getActionUrl("deleteObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));

		//3.使用不存在的ocs_id删除object_collect_task表
		bodyString = new HttpClient()
				.addData("ocs_id", OCS_ID + 100)
				.post(getActionUrl("deleteObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
	}

	/**
	 * saveObjectCollectTask保存对象采集对应信息表测试用例
	 * <p>
	 * 1.保存对象采集对应信息表，ocs_id不为空，走编辑逻辑，更新数据
	 * 2.保存对象采集对应信息表，ocs_id为空，走新增逻辑，插入数据
	 * 3.保存对象采集对应信息表，en_name名称重复
	 * 4.保存对象采集对应信息表，en_name格式不正确
	 */
	@Test
	public void saveObjectCollectTaskTest() {
		//1.保存对象采集对应信息表，ocs_id不为空，走编辑逻辑，更新数据
		JSONArray array = new JSONArray();
		for (int i = 0; i < OBJECT_COLLECT_TASK_ROWS; i++) {
			JSONObject object = new JSONObject();
			object.put("ocs_id", OCS_ID + i);
			object.put("en_name", "aaaTestzzuuiqyqiw" + i);
			object.put("zh_name", "测试用例使用");
			object.put("remark", IsFlag.Shi.getCode());
			object.put("collect_data_type", CollectDataType.JSON.getCode());
			object.put("database_code", DataBaseCode.UTF_8.getCode());
			array.add(object);
		}
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("odc_id", ODC_ID)
				.addData("object_collect_task_array", array.toJSONString())
				.post(getActionUrl("saveObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			long optionalLong = SqlOperator.queryNumber(db, "select count(1) count from "
					+ Object_collect_task.TableName + " where agent_id = ?", AGENT_ID).orElseThrow(() ->
					new BusinessException("查询得到的数据必须有且只有一条"));
			assertThat("校验数据量正确", optionalLong, is(OBJECT_COLLECT_TASK_ROWS));
			Result result = SqlOperator.queryResult(db, "select * from " +
							Object_collect_task.TableName + " where en_name = ? "
					, "aaaTestzzuuiqyqiw1");
			assertThat("校验Object_collect_task表数据正确", result.getString(0
					, "collect_data_type"), is(CollectDataType.JSON.getCode()));
		}

		//2.保存对象采集对应信息表，ocs_id为空，走新增逻辑，插入数据
		array.clear();
		for (int i = 20; i < OBJECT_COLLECT_TASK_ROWS + 20; i++) {
			JSONObject object = new JSONObject();
			object.put("en_name", "gggggyyyyyyyytttr887921" + i);
			object.put("zh_name", "测试用例使用");
			object.put("remark", IsFlag.Shi.getCode());
			object.put("collect_data_type", CollectDataType.JSON.getCode());
			object.put("database_code", DataBaseCode.UTF_8.getCode());
			object.put("updatetype", UpdateType.DirectUpdate.getCode());
			object.put("firstline", "bbb");
			array.add(object);
		}
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("odc_id", ODC_ID)
				.addData("object_collect_task_array", array.toJSONString())
				.post(getActionUrl("saveObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			long optionalLong = SqlOperator.queryNumber(db, "select count(1) count from "
					+ Object_collect_task.TableName + " where agent_id = ?", AGENT_ID).orElseThrow(() ->
					new BusinessException("查询得到的数据必须有且只有一条"));
			assertThat("校验数据量正确", optionalLong, is(OBJECT_COLLECT_TASK_ROWS * 2));
			Result result = SqlOperator.queryResult(db, "select * from " +
							Object_collect_task.TableName + " where en_name = ? "
					, "gggggyyyyyyyytttr88792120");
			assertThat("校验Object_collect_task表数据正确", result.getLong(0
					, "odc_id"), is(ODC_ID));
			assertThat("校验Object_collect_task表数据正确", result.getString(0
					, "database_code"), is(DataBaseCode.UTF_8.getCode()));
		}

		//3.保存对象采集对应信息表，en_name名称重复
		array.clear();
		for (int i = 20; i < OBJECT_COLLECT_TASK_ROWS + 20; i++) {
			JSONObject object = new JSONObject();
			object.put("en_name", "gggggyyyyyyyytttr887921");
			object.put("zh_name", "测试用例使用");
			object.put("remark", IsFlag.Shi.getCode());
			object.put("collect_data_type", CollectDataType.JSON.getCode());
			object.put("database_code", DataBaseCode.UTF_8.getCode());
			array.add(object);
		}
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("odc_id", ODC_ID)
				.addData("object_collect_task_array", array.toJSONString())
				.post(getActionUrl("saveObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));

//		//4.保存对象采集对应信息表，en_name格式不正确
//		array.clear();
//		for (int i = 20; i < OBJECT_COLLECT_TASK_ROWS + 20; i++) {
//			JSONObject object = new JSONObject();
//			object.put("en_name", "英文名称为中文" + i);
//			object.put("zh_name", "测试用例使用");
//			object.put("remark", IsFlag.Shi.getCode());
//			object.put("collect_data_type", CollectDataType.JSON.getCode());
//			object.put("odc_id", ODC_ID);
//			object.put("database_code", DataBaseCode.UTF_8.getCode());
//			object.put("agent_id", AGENT_ID);
//			array.add(object);
//		}
//		bodyString = new HttpClient()
//				.addData("object_collect_task_array", array.toJSONString())
//				.post(getActionUrl("saveObjectCollectTask")).getBodyString();
//		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
//				-> new BusinessException("连接失败！"));
//		assertThat(ar.isSuccess(), is(false));
	}

	/**
	 * 查询半结构化采集列结构信息(采集列结构）
	 * <p>
	 * 1.测试一个正确的ocs_id查询数据
	 * 2.测试使用一个错误的ocs_id查询数据
	 * 注：此方法没有写到四个及以上的测试用例是因为此方法只是一个查询方法，只有正确和错误两种情况
	 */
	@Test
	public void searchCollectColumnStruct() {
		//1.测试一个正确的ocs_id查询数据
		bodyString = new HttpClient()
				.addData("ocs_id", OCS_ID)
				.post(getActionUrl("searchCollectColumnStruct")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		//验证数据
		Map<Object, Object> dataForMap = ar.getDataForMap();
		List<Map<String, String>> objectStructList = JsonUtil.toObject(dataForMap.get("objectStructList").toString(),
				LISTTYPE);
		assertThat(objectStructList.get(0).get("column_type"), is("decimal(18,32)"));
		assertThat(objectStructList.get(0).get("col_seq"), is(1));
		assertThat(objectStructList.get(0).get("is_key"), is(IsFlag.Shi.getCode()));
		assertThat(objectStructList.get(0).get("is_hbase"), is(IsFlag.Shi.getCode()));
		assertThat(objectStructList.get(0).get("is_rowkey"), is(IsFlag.Shi.getCode()));
		assertThat(objectStructList.get(0).get("is_solr"), is(IsFlag.Shi.getCode()));
		assertThat(objectStructList.get(0).get("columnposition"), is("columns.testcol0"));
		assertThat(objectStructList.get(0).get("is_operate"), is(IsFlag.Shi.getCode()));
		assertThat(objectStructList.get(0).get("is_solr"), is(IsFlag.Shi.getCode()));
		assertThat(objectStructList.get(0).get("data_desc"), is("测试对象中文描述0"));

		//2.测试使用一个错误的ocs_id查询数据
		bodyString = new HttpClient()
				.addData("ocs_id", 7826112L)
				.post(getActionUrl("searchCollectColumnStruct")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		dataForMap = ar.getDataForMap();
		objectStructList = JsonUtil.toObject(dataForMap.get("objectStructList").toString(),
				LISTTYPE);
		assertThat(objectStructList.size(), is(0));
	}

	@Method(desc = "删除对象采集结构信息表测试用例",
			logicStep = "1.测试一个正确的struct_id，删除对象采集结构信息表" +
					"2.测试一个错误的struct_id，删除对象采集结构信息表" +
					"注：此方法没有写到四个及以上的测试用例是因为此方法只是一个查询方法，只有正确和错误两种情况")
	@Test
	public void deleteObjectCollectStructTest() {
		//1.测试一个正确的struct_id，删除对象采集结构信息表
		bodyString = new HttpClient()
				.addData("struct_id", STRUCT_ID)
				.post(getActionUrl("deleteObjectCollectStruct")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		//验证数据
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			long optionalLong = SqlOperator.queryNumber(db, "select count(1) count from "
					+ Object_collect_struct.TableName + " where struct_id = ?", STRUCT_ID).orElseThrow(() ->
					new BusinessException("查询得到的数据必须有且只有一条"));
			assertThat("校验数据量正确", optionalLong, is(0L));
		}

		//2.测试一个错误的struct_id，删除对象采集结构信息表
		bodyString = new HttpClient()
				.addData("struct_id", 7826112L)
				.post(getActionUrl("deleteObjectCollectStruct")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
	}


	/**
	 * saveObject_collect_struct保存对象采集对应结构信息表测试用例
	 * <p>
	 * 1.保存对象采集对应结构信息表，struct_id不为空，走编辑逻辑，更新数据
	 * 2.保存对象采集对应结构信息表，struct_id为空，走新增逻辑，插入数据
	 * 3.保存对象采集对应结构信息表，en_name格式不正确
	 */
	@Test
	public void saveHBaseConfInfo() {
		//1.保存对象采集对应结构信息表，ocs_id不为空，走编辑逻辑，更新数据
		JSONArray array = new JSONArray();
		for (int i = 0; i < OBJECT_COLLECT_STRUCT_ROWS; i++) {
			JSONObject object = new JSONObject();
			object.put("struct_id", STRUCT_ID + i);
			object.put("column_name", "aaaTestzzuuiqyqiw" + i);
			object.put("remark", "测试用例使用" + i);
			object.put("struct_type", ObjectDataType.ZiFuChuan.getCode());
			object.put("column_type", "string");
			object.put("is_hbase", IsFlag.Fou.getCode());
			object.put("is_rowkey", IsFlag.Fou.getCode());
			object.put("is_solr", IsFlag.Fou.getCode());
			object.put("is_key", IsFlag.Fou.getCode());
			object.put("is_operate", IsFlag.Fou.getCode());
			object.put("col_seq", i + 1);
			object.put("columnposition", "aaaTestzzuuiqyqiw" + i);
			array.add(object);
		}
		bodyString = new HttpClient()
				.addData("ocs_id", OCS_ID)
				.addData("object_collect_struct_array", array.toJSONString())
				.post(getActionUrl("saveHBaseConfInfo")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			long optionalLong = SqlOperator.queryNumber(db, "select count(1) count from "
					+ Object_collect_struct.TableName + " where ocs_id = ?", OCS_ID).orElseThrow(() ->
					new BusinessException("查询得到的数据必须有且只有一条"));
			Result result = SqlOperator.queryResult(db, "select * from " +
							Object_collect_struct.TableName + " where column_name = ? "
					, "aaaTestzzuuiqyqiw1");
			assertThat("校验Object_collect_task表数据正确", result.getString(0
					, "column_type"), is("string"));
			assertThat("校验Object_collect_task表数据正确", result.getString(0
					, "remark"), is("测试用例使用1"));
			assertThat("校验Object_collect_task表数据正确", result.getString(0
					, "is_hbase"), is(IsFlag.Fou.getCode()));
			assertThat("校验Object_collect_task表数据正确", result.getString(0
					, "is_rowkey"), is(IsFlag.Fou.getCode()));
			assertThat("校验Object_collect_task表数据正确", result.getString(0
					, "is_solr"), is(IsFlag.Fou.getCode()));
			assertThat("校验Object_collect_task表数据正确", result.getString(0
					, "is_operate"), is(IsFlag.Fou.getCode()));
			assertThat("校验Object_collect_task表数据正确", result.getString(0
					, "is_key"), is(IsFlag.Fou.getCode()));
			assertThat("校验Object_collect_task表数据正确", result.getString(0
					, "columnposition"), is("aaaTestzzuuiqyqiw1"));
		}
		//2.保存对象采集对应结构信息表，column_name名称重复
		array.clear();
		for (int i = 20; i < OBJECT_COLLECT_STRUCT_ROWS + 20; i++) {
			JSONObject object = new JSONObject();
			object.put("column_name", "aaaTestzzuuiqyqiw");
			object.put("remark", "测试用例使用" + i);
			object.put("is_hbase", IsFlag.Fou.getCode());
			object.put("is_rowkey", IsFlag.Fou.getCode());
			object.put("is_solr", IsFlag.Fou.getCode());
			object.put("is_key", IsFlag.Fou.getCode());
			object.put("is_operate", IsFlag.Fou.getCode());
			object.put("col_seq", i + 1);
			object.put("columnposition", "aaaTestzzuuiqyqiw" + i);
			object.put("columntype", "string");
			array.add(object);
		}
		bodyString = new HttpClient()
				.addData("ocs_id", OCS_ID)
				.addData("object_collect_struct_array", array.toJSONString())
				.post(getActionUrl("saveHBaseConfInfo")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));

//		//5.保存对象采集对应结构信息表，en_name格式不正确
//		array.clear();
//		for (int i = 20; i < OBJECT_COLLECT_STRUCT_ROWS + 20; i++) {
//			JSONObject object = new JSONObject();
//			object.put("struct_id", STRUCT_ID + i);
//			object.put("column_name", "我是中文，哈哈哈，不正确"+i);
//			object.put("remark", "测试用例使用");
//			object.put("ocs_id", OCS_ID);
//			object.put("data_desc", "对象采集对应结构信息表column_name描述"+i);
//			array.add(object);
//		}
//		bodyString = new HttpClient()
//				.addData("object_collect_struct_array", array.toJSONString())
//				.post(getActionUrl("saveObject_collect_struct")).getBodyString();
//		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
//				-> new BusinessException("连接失败！"));
//		assertThat(ar.isSuccess(), is(false));
	}

	@Method(desc = "根据对象采集id查询对象采集任务存储设置测试用例",
			logicStep = "1.测试一个正确的odc_id查询数据" +
					"2.测试使用一个错误的odc_id查询数据" +
					"此方法没有写到四个及以上的测试用例是因为此方法只是一个查询方法，只有正确和错误两种情况")
	@Test
	public void searchObjectStorageTest() {
		//1.测试一个正确的odc_id查询数据
		bodyString = new HttpClient()
				.addData("odc_id", ODC_ID)
				.post(getActionUrl("searchObjectStorage")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		//验证数据
		assertThat(ar.getDataForResult().getRowCount(), is(Integer.parseInt(OBJECT_COLLECT_TASK_ROWS + "")));
		for (int i = 0; i < ar.getDataForResult().getRowCount(); i++) {
			long ocs_id = ar.getDataForResult().getLong(i, "ocs_id");
			if (ocs_id != OCS_ID && ocs_id != OCS_ID + 1 && ocs_id != OCS_ID + 2) {
				assertThat(ar.getDataForResult().getString(i, "is_hbase"), is(IsFlag.Fou.getCode()));
				assertThat(ar.getDataForResult().getString(i, "is_hdfs"), is(IsFlag.Shi.getCode()));
				assertThat(ar.getDataForResult().getString(i, "is_solr"), is(IsFlag.Fou.getCode()));
			} else {
				assertThat(ar.getDataForResult().getString(i, "is_hbase"), is(""));
				assertThat(ar.getDataForResult().getString(i, "is_hdfs"), is(""));
				assertThat(ar.getDataForResult().getString(i, "is_solr"), is(""));
			}
		}

		//2.测试使用一个错误的odc_id查询数据
		bodyString = new HttpClient()
				.addData("odc_id", 7826112L)
				.post(getActionUrl("searchObjectStorage")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
	}

	@Method(desc = "获取当前表的码表信息", logicStep = "1.正确的数据访问1，数据有效" +
			"2.错误的数据访问1，ocs_id不存在" +
			"此方法没有写到四个及以上的测试用例是因为此方法只是一个查询方法，只有正确和错误两种情况")
	@Test
	public void searchObjectHandleType() {
		// 1.正确的数据访问1，数据有效
		bodyString = new HttpClient()
				.addData("ocs_id", OCS_ID)
				.post(getActionUrl("searchObjectHandleType")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		List<Object_handle_type> objectHandleTypes = ar.getDataForEntityList(Object_handle_type.class);
		assertThat(objectHandleTypes.size(), is(3));
		for (int i = 0; i < objectHandleTypes.size(); i++) {
			assertThat(objectHandleTypes.get(i).getHandle_type(), is(i + ""));
			assertThat(objectHandleTypes.get(i).getHandle_value(), is(OperationType.ofValueByCode(i + "")));
			assertThat(objectHandleTypes.get(i).getObject_handle_id(), is(OBJECT_HANDLE_ID + i));
			assertThat(objectHandleTypes.get(i).getOcs_id(), is(OCS_ID));
		}
		// 2.错误的数据访问1，ocs_id不存在
		bodyString = new HttpClient()
				.addData("ocs_id", "111")
				.post(getActionUrl("searchObjectHandleType")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		assertThat(ar.getData().toString(), is("[]"));
	}

	@Method(desc = "选择文件路径", logicStep = "1.正确的数据访问1，文件路径为空" +
			"2.正确的数据访问2，文件路径不为空" +
			"3.错误的数据访问1，agent_id不存在" +
			"4.错误的数据访问2，file_path不存在")
	@Test
	public void selectFilePath() {
		// 1.正确的数据访问1，文件路径为空
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.post(getActionUrl("selectFilePath")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		assertThat(ar.getData().toString(), notNullValue());
		// 2.正确的数据访问2，文件路径不为空
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("file_path", DICTINARYFILE.getAbsolutePath())
				.post(getActionUrl("selectFilePath")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		assertThat(ar.getData().toString(), notNullValue());
		// 3.错误的数据访问1，agent_id不存在
		bodyString = new HttpClient()
				.addData("agent_id", "111")
				.post(getActionUrl("selectFilePath")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
		// 4.错误的数据访问2，file_path不存在
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("file_path", "/aaa")
				.post(getActionUrl("selectFilePath")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		assertThat(ar.getData(), is("[]"));
	}

	@Method(desc = "查看表", logicStep = "1.正确的数据访问1，数据都有效，数据字典不存在" +
			"2.正确的数据访问2，数据都有效，数据字典存在" +
			"3.错误的数据访问1，agent_id不存在" +
			"4.错误的数据访问2，file_path不存在" +
			"5.错误的数据访问3，is_dictionary不存在" +
			"6.错误的数据访问5，当是否存在数据字典是否的时候，数据日期为空")
	@Test
	public void viewTable() {
		// 1.正确的数据访问1，数据都有效，数据字典不存在
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("file_path", DICTINARYFILE.getAbsolutePath())
				.addData("is_dictionary", IsFlag.Fou.getCode())
				.addData("data_date", "20200301")
				.addData("file_suffix", "json")
				.post(getActionUrl("viewTable")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		Result dataForResult = ar.getDataForResult();
		assertThat(dataForResult.getRowCount(), is(1));
		assertThat(dataForResult.getString(0, "table_name"), is("dd_data"));
		assertThat(dataForResult.getString(0, "table_ch_name"), is("dd_data"));
		// 2.正确的数据访问2，数据都有效，数据字典存在
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("file_path", DICTINARYFILE.getAbsolutePath())
				.addData("is_dictionary", IsFlag.Shi.getCode())
				.addData("file_suffix", "json")
				.post(getActionUrl("viewTable")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		dataForResult = ar.getDataForResult();
		assertThat(dataForResult.getRowCount(), is(1));
		assertThat(dataForResult.getString(0, "table_name"), is("t_executedpersons"));
		assertThat(dataForResult.getString(0, "table_ch_name"), is("t_executedpersons"));
		// 3.错误的数据访问1，agent_id不存在
		bodyString = new HttpClient()
				.addData("agent_id", "111")
				.addData("file_path", DICTINARYFILE.getAbsolutePath())
				.addData("is_dictionary", IsFlag.Shi.getCode())
				.addData("file_suffix", "json")
				.post(getActionUrl("viewTable")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
		// 4.错误的数据访问2，file_path不存在
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("file_path", "/aaa")
				.addData("is_dictionary", IsFlag.Shi.getCode())
				.addData("file_suffix", "json")
				.post(getActionUrl("viewTable")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
		// 5.错误的数据访问3，is_dictionary不存在
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("file_path", DICTINARYFILE.getAbsolutePath())
				.addData("is_dictionary", "2")
				.addData("file_suffix", "json")
				.post(getActionUrl("viewTable")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
		// 6.错误的数据访问5，当是否存在数据字典是否的时候，数据日期为空
		bodyString = new HttpClient()
				.addData("agent_id", AGENT_ID)
				.addData("file_path", DICTINARYFILE.getAbsolutePath())
				.addData("is_dictionary", IsFlag.Fou.getCode())
				.addData("file_suffix", "json")
				.post(getActionUrl("viewTable")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
	}

	@Method(desc = "查询半结构化采集列结构信息",
			logicStep = "1.正确的数据访问1，数据都有效" +
					"2.错误的数据访问1，ocs_id不存在")
	@Test
	public void searchCollectColumnStructTest() {
		// 1.正确的数据访问1，数据都有效
		bodyString = new HttpClient().addData("ocs_id", OCS_ID)
				.post(getActionUrl("searchCollectColumnStruct")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		Map<Object, Object> dataForMap = ar.getDataForMap();
		// 2.错误的数据访问1，ocs_id不存在
		bodyString = new HttpClient().addData("ocs_id", "111")
				.post(getActionUrl("searchCollectColumnStruct")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
	}

	@Method(desc = "保存对象采集结构信息（采集列结构）",
			logicStep = "1.正确的数据访问1，数据都有效，struct_id为空，走新增路线" +
					"2.正确的数据访问，数据有效，struct_id不为空，走更新路线" +
					"3.错误的数据访问1，collectStruct数据格式有误" +
					"4.错误的数据访问2，ocs_id为空")
	@Test
	public void saveCollectColumnStructTest() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("is_hbase", IsFlag.Fou.getCode());
			map.put("is_solr", IsFlag.Fou.getCode());
			map.put("is_rowkey", IsFlag.Fou.getCode());
			map.put("is_operate", IsFlag.Shi.getCode());
			map.put("is_key", IsFlag.Shi.getCode());
			map.put("col_seq", i);
			switch (i) {
				case 0:
					map.put("column_name", "column_id");
					map.put("column_type", "decimal(38,18)");
					map.put("columnposition", "columns,column_id");
					break;
				case 1:
					map.put("column_name", "table_name");
					map.put("column_type", "varchar(512)");
					map.put("columnposition", "columns,table_name");
					break;
				case 2:
					map.put("column_name", "table_cn_name");
					map.put("column_type", "varchar(512)");
					map.put("columnposition", "columns,table_cn_name");
					break;
				case 3:
					map.put("column_name", "updatetype");
					map.put("column_type", "varchar(1)");
					map.put("columnposition", "updatetype");
					break;
				case 4:
					map.put("column_name", "insert");
					map.put("column_type", "decimal(38,18)");
					map.put("columnposition", "handletype,insert");
					break;
				case 5:
					map.put("column_name", "column_name");
					map.put("column_type", "decimal(38,18)");
					map.put("columnposition", "columns,column_name");
					break;
			}
			list.add(map);
		}
		// 1.正确的数据访问1，数据都有效，struct_id为空，走新增路线
		bodyString = new HttpClient()
				.addData("ocs_id", "10001")
				.addData("collectStruct", JsonUtil.toJson(list))
				.post(getActionUrl("saveCollectColumnStruct")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		// 验证数据保存是否正确
		List<Object_collect_struct> collectStructs;
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			collectStructs = SqlOperator.queryList(db, Object_collect_struct.class,
					"select * from " + Object_collect_struct.TableName + " where ocs_id=? order by col_seq",
					10001);
			assertThat(collectStructs.get(0).getOcs_id(), is(Long.parseLong("10001")));
			assertThat(collectStructs.get(0).getColumn_type(), is("decimal(38,18)"));
			assertThat(collectStructs.get(0).getColumnposition(), is("columns,column_id"));
			assertThat(collectStructs.get(0).getIs_operate(), is(IsFlag.Shi.getCode()));
		}
		// 2.正确的数据访问，数据有效，struct_id不为空，走更新路线
		List<Map<String, Object>> list2 = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("is_hbase", IsFlag.Shi.getCode());
			map.put("is_solr", IsFlag.Fou.getCode());
			map.put("is_rowkey", IsFlag.Fou.getCode());
			map.put("is_operate", IsFlag.Shi.getCode());
			map.put("is_key", IsFlag.Shi.getCode());
			map.put("col_seq", i);
			map.put("struct_id", collectStructs.get(i).getStruct_id());
			switch (i) {
				case 0:
					map.put("column_name", "column_id");
					map.put("column_type", "int(8)");
					map.put("columnposition", "columns,column_id");
					break;
				case 1:
					map.put("column_name", "table_name");
					map.put("column_type", "varchar(256)");
					map.put("columnposition", "columns,table_name");
					break;
				case 2:
					map.put("column_name", "table_cn_name");
					map.put("column_type", "varchar(256)");
					map.put("columnposition", "columns,table_cn_name");
					break;
				case 3:
					map.put("column_name", "updatetype");
					map.put("column_type", "bpchar(1)");
					map.put("columnposition", "updatetype");
					break;
				case 4:
					map.put("column_name", "insert");
					map.put("column_type", "bpchar(1)");
					map.put("columnposition", "handletype,insert");
					break;
				case 5:
					map.put("column_name", "column_name");
					map.put("column_type", "varchar(512)");
					map.put("columnposition", "columns,column_name");
					break;
			}
			list2.add(map);
		}
		bodyString = new HttpClient()
				.addData("ocs_id", "10002")
				.addData("collectStruct", JsonUtil.toJson(list2))
				.post(getActionUrl("saveCollectColumnStruct")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			collectStructs = SqlOperator.queryList(db, Object_collect_struct.class,
					"select * from " + Object_collect_struct.TableName + " where ocs_id=? order by col_seq",
					10001);
			assertThat(collectStructs.get(0).getOcs_id(), is(Long.parseLong("10001")));
			assertThat(collectStructs.get(0).getColumn_type(), is("int(8)"));
			assertThat(collectStructs.get(0).getColumnposition(), is("columns,column_id"));
			assertThat(collectStructs.get(0).getIs_operate(), is(IsFlag.Shi.getCode()));
		}
		// 3.错误的数据访问1，collectStruct数据格式有误
		bodyString = new HttpClient()
				.addData("ocs_id", "10003")
				.addData("collectStruct", "aaaa")
				.post(getActionUrl("saveCollectColumnStruct")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
		// 4.错误的数据访问2，ocs_id为空
		bodyString = new HttpClient()
				.addData("ocs_id", "")
				.addData("collectStruct", "aaaa")
				.post(getActionUrl("saveCollectColumnStruct")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
	}

	@Method(desc = "保存表的码表信息（操作码表）",
			logicStep = "1.正确的数据访问1，数据都有效")
	@Test
	public void saveHandleTypeTest() {
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			Map<String, Object> map = new HashMap<>();
			switch (i) {
				case 0:
					map.put("handle_type", OperationType.INSERT.getCode());
					map.put("handle_value", OperationType.INSERT.getValue());
					break;
				case 1:
					map.put("handle_type", OperationType.UPDATE.getCode());
					map.put("handle_value", OperationType.UPDATE.getValue());
					break;
				case 2:
					map.put("handle_type", OperationType.DELETE.getCode());
					map.put("handle_value", OperationType.DELETE.getValue());
					break;
			}
			list.add(map);
		}
		// 1.正确的数据访问1，数据都有效
		bodyString = new HttpClient()
				.addData("ocs_id", OCS_ID)
				.addData("handleType", JsonUtil.toJson(list))
				.post(getActionUrl("saveHandleType")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		// 2.错误的数据访问1，handleType格式错误不存在
		bodyString = new HttpClient()
				.addData("ocs_id", OCS_ID)
				.addData("handleType", "aaa")
				.post(getActionUrl("saveHandleType")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
	}

	@Method(desc = "保存对象文件配置信息时检查字段",
			logicStep = "1.正确的数据访问1，数据都有效" +
					"2.错误的数据访问1，objColTask数据格式错误" +
					"3.错误的数据访问2，objColTask数据格式正确，英文名为空" +
					"4.错误的数据访问3，objColTask数据格式正确，采集列结构为空" +
					"5.错误的数据访问4，objColTask数据格式正确，操作码表为空")
	@Test
	public void checkFieldsForSaveObjectCollectTaskTest() {
		// 1.正确的数据访问1，数据都有效
		List<Object_collect_task> list = new ArrayList<>();
		for (int i = 0; i < OBJECT_COLLECT_HANDEL_ROWS; i++) {
			Object_collect_task objectCollectTask = new Object_collect_task();
			objectCollectTask.setOcs_id(OCS_ID + i);
			objectCollectTask.setOdc_id(ODC_ID);
			objectCollectTask.setUpdatetype(UpdateType.DirectUpdate.getCode());
			objectCollectTask.setEn_name("aaa" + i);
			objectCollectTask.setZh_name("测试aaa" + i);
			objectCollectTask.setFirstline("");
			objectCollectTask.setDatabase_code(DataBaseCode.UTF_8.getCode());
			objectCollectTask.setAgent_id(AGENT_ID);
			objectCollectTask.setCollect_data_type(CollectDataType.JSON.getCode());
			list.add(objectCollectTask);
		}
		bodyString = new HttpClient()
				.addData("objColTask", JsonUtil.toJson(list))
				.post(getActionUrl("checkFieldsForSaveObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(true));
		// 2.错误的数据访问1，objColTask数据格式错误
		bodyString = new HttpClient()
				.addData("objColTask", "aaa")
				.post(getActionUrl("checkFieldsForSaveObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
		// 3.错误的数据访问2，objColTask数据格式正确，英文名为空
		List<Object_collect_task> list2 = new ArrayList<>();
		for (int i = 0; i < OBJECT_COLLECT_HANDEL_ROWS; i++) {
			Object_collect_task objectCollectTask = new Object_collect_task();
			objectCollectTask.setOcs_id(OCS_ID + i);
			objectCollectTask.setOdc_id(ODC_ID);
			objectCollectTask.setUpdatetype(UpdateType.DirectUpdate.getCode());
			objectCollectTask.setEn_name("");
			objectCollectTask.setZh_name("测试aaa" + i);
			objectCollectTask.setFirstline("");
			objectCollectTask.setDatabase_code(DataBaseCode.UTF_8.getCode());
			objectCollectTask.setAgent_id(AGENT_ID);
			objectCollectTask.setCollect_data_type(CollectDataType.JSON.getCode());
			list2.add(objectCollectTask);
		}
		bodyString = new HttpClient()
				.addData("objColTask", JsonUtil.toJson(list2))
				.post(getActionUrl("checkFieldsForSaveObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
		// 4.错误的数据访问3，objColTask数据格式正确，采集列结构为空
		List<Object_collect_task> list3 = new ArrayList<>();
		for (int i = 0; i < OBJECT_COLLECT_HANDEL_ROWS; i++) {
			Object_collect_task objectCollectTask = new Object_collect_task();
			objectCollectTask.setOcs_id(OCS_ID - i);
			objectCollectTask.setOdc_id(ODC_ID);
			objectCollectTask.setUpdatetype(UpdateType.DirectUpdate.getCode());
			objectCollectTask.setEn_name("aaa" + i);
			objectCollectTask.setZh_name("测试aaa" + i);
			objectCollectTask.setFirstline("");
			objectCollectTask.setDatabase_code(DataBaseCode.UTF_8.getCode());
			objectCollectTask.setAgent_id(AGENT_ID);
			objectCollectTask.setCollect_data_type(CollectDataType.JSON.getCode());
			list3.add(objectCollectTask);
		}
		bodyString = new HttpClient()
				.addData("objColTask", JsonUtil.toJson(list3))
				.post(getActionUrl("checkFieldsForSaveObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
		// 5.错误的数据访问4，objColTask数据格式正确，操作码表为空
		List<Object_collect_task> list4 = new ArrayList<>();
		for (int i = 0; i < OBJECT_COLLECT_HANDEL_ROWS + 1; i++) {
			Object_collect_task objectCollectTask = new Object_collect_task();
			objectCollectTask.setOcs_id(OCS_ID + i);
			objectCollectTask.setOdc_id(ODC_ID);
			objectCollectTask.setUpdatetype(UpdateType.DirectUpdate.getCode());
			objectCollectTask.setEn_name("aaa" + i);
			objectCollectTask.setZh_name("测试aaa" + i);
			objectCollectTask.setFirstline("");
			objectCollectTask.setDatabase_code(DataBaseCode.UTF_8.getCode());
			objectCollectTask.setAgent_id(AGENT_ID);
			objectCollectTask.setCollect_data_type(CollectDataType.JSON.getCode());
			list4.add(objectCollectTask);
		}
		bodyString = new HttpClient()
				.addData("objColTask", JsonUtil.toJson(list4))
				.post(getActionUrl("checkFieldsForSaveObjectCollectTask")).getBodyString();
		ar = JsonUtil.toObjectSafety(bodyString, ActionResult.class).orElseThrow(()
				-> new BusinessException("连接失败！"));
		assertThat(ar.isSuccess(), is(false));
	}


	/**
	 * 测试用例清理数据
	 * <p>
	 * 1.清理sys_user表中造的数据
	 * 2.清理Department_info表中造的数据
	 * 3.删除测试用例造的agent_down_info表数据，默认为1条，AGENT_ID为10000001
	 * 4.删除测试用例造的Object_collect表数据，默认为2条,ODC_ID为20000001---20000002
	 * 5.删除测试用例造的object_collect_task表数据，默认为10条,OCS_ID为30000001---30000010
	 * 6.删除测试用例造的object_storage表数据，默认为10条,OBJ_STID为40000001---40000010
	 * 7.删除测试用例造的object_collect_struct表数据，默认为10条,STRUCT_ID为50000001---50000010
	 */
	@After
	public void afterTest() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			//1.清理sys_user表中造的数据
			SqlOperator.execute(db, "DELETE FROM " + Sys_user.TableName + " WHERE user_id = ?"
					, USER_ID);
			//2.清理Department_info表中造的数据
			SqlOperator.execute(db, "DELETE FROM " + Department_info.TableName + " WHERE dep_id = ?"
					, DEPT_ID);
			//3.删除测试用例造的agent_down_info表数据，默认为1条，AGENT_ID为10000001
			SqlOperator.execute(db, "DELETE FROM " + Agent_down_info.TableName + " WHERE remark = ?"
					, "测试用例清除数据专用列");
			//4.删除测试用例造的Object_collect表数据，默认为2条,ODC_ID为20000001---20000002
			SqlOperator.execute(db, "DELETE FROM " + Object_collect.TableName + " WHERE agent_id = ?"
					, AGENT_ID);
			SqlOperator.execute(db, "DELETE FROM " + Object_collect_task.TableName
					+ " WHERE agent_id = ?", AGENT_ID);
			//7.删除测试用例造的object_collect_struct表数据，默认为10条,STRUCT_ID为50000001---50000010
			for (long i = 0; i < OBJECT_COLLECT_STRUCT_ROWS; i++) {
				SqlOperator.execute(db, "DELETE FROM " + Object_collect_struct.TableName
						+ " WHERE ocs_id = ?", OCS_ID + i);
			}
			// 8.删除测试用例造的Object_handle_type表数据，默认为3条,object_handle_id为60000001---60000003
			for (long i = 0; i < OBJECT_COLLECT_TASK_ROWS; i++) {
				SqlOperator.execute(db, "DELETE FROM " + Object_handle_type.TableName
						+ " WHERE ocs_id = ?", OCS_ID + i);
			}
			//9.删除agent_info表数据
			SqlOperator.execute(db, "DELETE FROM " + Agent_info.TableName + " WHERE agent_id = ?"
					, AGENT_ID);
			SqlOperator.commitTransaction(db);
		}
	}
}
