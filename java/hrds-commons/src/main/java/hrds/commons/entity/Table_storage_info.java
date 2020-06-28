package hrds.commons.entity;
/**Auto Created by VBScript Do not modify!*/
import hrds.commons.entity.fdentity.ProjectTableEntity;
import fd.ng.db.entity.anno.Table;
import fd.ng.core.annotation.DocBean;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * 表存储信息
 */
@Table(tableName = "table_storage_info")
public class Table_storage_info extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "table_storage_info";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 表存储信息 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("storage_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="storage_id",value="储存编号:",dataType = Long.class,required = true)
	private Long storage_id;
	@DocBean(name ="file_format",value="文件格式(FileFormat):0-定长<DingChang> 1-非定长<FeiDingChang> 2-CSV<CSV> 3-SEQUENCEFILE<SEQUENCEFILE> 4-PARQUET<PARQUET> 5-ORC<ORC> ",dataType = String.class,required = true)
	private String file_format;
	@DocBean(name ="table_id",value="表名ID:",dataType = Long.class,required = false)
	private Long table_id;
	@DocBean(name ="storage_type",value="进数方式(StorageType):1-增量<ZengLiang> 2-追加<ZhuiJia> 3-替换<TiHuan> ",dataType = String.class,required = true)
	private String storage_type;
	@DocBean(name ="storage_time",value="存储期限（以天为单位）:",dataType = Long.class,required = true)
	private Long storage_time;
	@DocBean(name ="is_zipper",value="是否拉链存储(IsFlag):1-是<Shi> 0-否<Fou> ",dataType = String.class,required = true)
	private String is_zipper;
	@DocBean(name ="hyren_name",value="进库之后拼接的表名:",dataType = String.class,required = true)
	private String hyren_name;

	/** 取得：储存编号 */
	public Long getStorage_id(){
		return storage_id;
	}
	/** 设置：储存编号 */
	public void setStorage_id(Long storage_id){
		this.storage_id=storage_id;
	}
	/** 设置：储存编号 */
	public void setStorage_id(String storage_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(storage_id)){
			this.storage_id=new Long(storage_id);
		}
	}
	/** 取得：文件格式 */
	public String getFile_format(){
		return file_format;
	}
	/** 设置：文件格式 */
	public void setFile_format(String file_format){
		this.file_format=file_format;
	}
	/** 取得：表名ID */
	public Long getTable_id(){
		return table_id;
	}
	/** 设置：表名ID */
	public void setTable_id(Long table_id){
		this.table_id=table_id;
	}
	/** 设置：表名ID */
	public void setTable_id(String table_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(table_id)){
			this.table_id=new Long(table_id);
		}
	}
	/** 取得：进数方式 */
	public String getStorage_type(){
		return storage_type;
	}
	/** 设置：进数方式 */
	public void setStorage_type(String storage_type){
		this.storage_type=storage_type;
	}
	/** 取得：存储期限（以天为单位） */
	public Long getStorage_time(){
		return storage_time;
	}
	/** 设置：存储期限（以天为单位） */
	public void setStorage_time(Long storage_time){
		this.storage_time=storage_time;
	}
	/** 设置：存储期限（以天为单位） */
	public void setStorage_time(String storage_time){
		if(!fd.ng.core.utils.StringUtil.isEmpty(storage_time)){
			this.storage_time=new Long(storage_time);
		}
	}
	/** 取得：是否拉链存储 */
	public String getIs_zipper(){
		return is_zipper;
	}
	/** 设置：是否拉链存储 */
	public void setIs_zipper(String is_zipper){
		this.is_zipper=is_zipper;
	}
	/** 取得：进库之后拼接的表名 */
	public String getHyren_name(){
		return hyren_name;
	}
	/** 设置：进库之后拼接的表名 */
	public void setHyren_name(String hyren_name){
		this.hyren_name=hyren_name;
	}
}
