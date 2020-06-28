package hrds.agent.job.biz.core.dbstage.writer.impl;

import fd.ng.core.utils.FileNameUtils;
import fd.ng.core.utils.StringUtil;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.TableBean;
import hrds.agent.job.biz.constant.JobConstant;
import hrds.agent.job.biz.core.dbstage.writer.AbstractFileWriter;
import hrds.agent.job.biz.dataclean.Clean;
import hrds.agent.job.biz.dataclean.CleanFactory;
import hrds.agent.job.biz.dataclean.DataCleanInterface;
import hrds.agent.job.biz.utils.ColumnTool;
import hrds.agent.job.biz.utils.ParquetUtil;
import hrds.commons.codes.FileFormat;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.Column_split;
import hrds.commons.entity.Data_extraction_def;
import hrds.commons.exception.AppSystemException;
import hrds.commons.utils.Constant;
import org.apache.avro.file.DataFileWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.GroupFactory;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.schema.MessageType;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * JdbcToParquetFileWriter
 * date: 2019/12/6 17:20
 * author: zxz
 */
public class JdbcToParquetFileWriter extends AbstractFileWriter {
	//打印日志
	private static final Log log = LogFactory.getLog(JdbcToParquetFileWriter.class);

	public JdbcToParquetFileWriter(ResultSet resultSet, CollectTableBean collectTableBean, int pageNum,
	                               TableBean tableBean, Data_extraction_def data_extraction_def) {
		super(resultSet, collectTableBean, pageNum, tableBean, data_extraction_def);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String writeFiles() {
		String eltDate = collectTableBean.getEtlDate();
		StringBuilder fileInfo = new StringBuilder(1024);
		String hbase_name = collectTableBean.getHbase_name();
//		String midName = Constant.JDBCUNLOADFOLDER + collectTableBean.getDatabase_id() + File.separator
//				+ collectTableBean.getTable_id() + File.separator;
		//数据抽取指定的目录
		String plane_url = data_extraction_def.getPlane_url();
		String midName = plane_url + File.separator + eltDate + File.separator + collectTableBean.getTable_name()
				+ File.separator + Constant.fileFormatMap.get(FileFormat.PARQUET.getCode()) + File.separator;
		midName = FileNameUtils.normalize(midName, true);
		String dataDelimiter = data_extraction_def.getDatabase_separatorr();
		DataFileWriter<Object> avroWriter = null;
		ParquetWriter<Group> parquetWriter = null;
		long counter = 0;
		int index = 0;
		GroupFactory factory;
		try {
			avroWriter = getAvroWriter(tableBean.getTypeArray(), hbase_name, midName, pageNum);
			//清洗配置
			final DataCleanInterface allClean = CleanFactory.getInstance().getObjectClean("clean_database");
			//获取所有查询的字段的名称，不包括列分割和列合并出来的字段名称
			List<String> selectColumnList = StringUtil.split(tableBean.getAllColumns(), Constant.METAINFOSPLIT);
			Map<String, Object> parseJson = tableBean.getParseJson();
			//字符合并
			Map<String, String> mergeIng = (Map<String, String>) parseJson.get("mergeIng");
			//字符拆分
			Map<String, Map<String, Column_split>> splitIng = (Map<String, Map<String, Column_split>>)
					parseJson.get("splitIng");
			Clean cl = new Clean(parseJson, allClean);
			StringBuilder midStringOther = new StringBuilder(1024 * 1024);//获取所有列的值用来生成MD5值
			StringBuilder sb_ = new StringBuilder();//用来写临时数据

			String currValue;
			int numberOfColumns = selectColumnList.size();
			int[] typeArray = tableBean.getTypeArray();
			MessageType parquetSchema = ParquetUtil.getSchema(tableBean.getColumnMetaInfo(),
					tableBean.getColTypeMetaInfo());
			factory = new SimpleGroupFactory(parquetSchema);
			String fileName = midName + hbase_name + pageNum + index + "." + data_extraction_def.getFile_suffix();
			parquetWriter = ParquetUtil.getParquetWriter(parquetSchema, fileName);
			fileInfo.append(fileName).append(Constant.METAINFOSPLIT);
			//获取所有查询的字段的类型，不包括列分割和列合并出来的字段类型
			List<String> type = StringUtil.split(tableBean.getAllType(), Constant.METAINFOSPLIT);
			while (resultSet.next()) {
				counter++;
				midStringOther.delete(0, midStringOther.length());
				//每一行获取一个group对象
				Group group = factory.newGroup();
				for (int i = 0; i < numberOfColumns; i++) {
					//获取原始值来计算 MD5
					sb_.delete(0, sb_.length());
					midStringOther.append(getOneColumnValue(avroWriter, counter, pageNum, resultSet,
							typeArray[i], sb_, selectColumnList.get(i), hbase_name, midName));
					// Add DELIMITER if not last value
					if (i < numberOfColumns - 1) {
						midStringOther.append(Constant.DATADELIMITER);
					}
					//清洗操作
					currValue = sb_.toString();
					currValue = cl.cleanColumn(currValue, selectColumnList.get(i).toUpperCase(), group,
							type.get(i), FileFormat.PARQUET.getCode(), null,
							data_extraction_def.getDatabase_code(), dataDelimiter);
					if (splitIng.get(selectColumnList.get(i).toUpperCase()) == null
							|| splitIng.get(selectColumnList.get(i).toUpperCase()).size() == 0) {
						ColumnTool.addData2Group(group, type.get(i), selectColumnList.get(i), currValue);
					}
				}
				//如果有列合并处理合并信息
				if (!mergeIng.isEmpty()) {
					List<String> arrColString = StringUtil.split(midStringOther.toString(), Constant.DATADELIMITER);
					//字段合并
					allClean.merge(mergeIng, arrColString.toArray(new String[0]),
							selectColumnList.toArray(new String[0]), group, null,
							FileFormat.PARQUET.getCode(), data_extraction_def.getDatabase_code(), dataDelimiter);
				}
				group.append(Constant.SDATENAME, eltDate);
				//根据是否算MD5判断是否追加结束日期和MD5两个字段
				if (IsFlag.Shi.getCode().equals(collectTableBean.getIs_md5())) {
					String md5 = toMD5(midStringOther.toString());
					group.append(Constant.EDATENAME, Constant.MAXDATE).append(Constant.MD5NAME, md5);
				}
				if (JobConstant.WriteMultipleFiles) {
					//获取文件大小和当前读到的内容大小
					long messageSize = group.toString().length();
					long singleFileSize = new File(fileName).length();
					if (singleFileSize + messageSize > JobConstant.FILE_BLOCKSIZE) {
						//当文件满足阈值时 ，然后关闭当前流，并创建新的数据流
						parquetWriter.close();
						index++;
						fileName = midName + hbase_name + pageNum + index + "." + data_extraction_def.getFile_suffix();
						parquetWriter = ParquetUtil.getParquetWriter(parquetSchema, fileName);
						fileInfo.append(fileName).append(Constant.METAINFOSPLIT);
					}
				}
				parquetWriter.write(group);
				if (counter % JobConstant.BUFFER_ROW == 0) {
					log.info("正在写入文件，已写入" + counter + "行");
				}
			}
		} catch (Exception e) {
			log.error("卸数失败", e);
			throw new AppSystemException("数据库采集卸数Parquet文件失败" + e.getMessage());
		} finally {
			try {
				if (parquetWriter != null)
					parquetWriter.close();
				if (avroWriter != null)
					avroWriter.close();
			} catch (IOException e) {
				log.error(e);
			}
		}
		fileInfo.append(counter);
		//返回卸数一个或者多个文件名全路径和总的文件行数
		return fileInfo.toString();
	}
}
