package hrds.agent.job.biz.core.dbstage.writer.impl;

import fd.ng.core.utils.FileNameUtils;
import fd.ng.core.utils.StringUtil;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.TableBean;
import hrds.agent.job.biz.constant.JobConstant;
import hrds.agent.job.biz.core.dbstage.writer.AbstractFileWriter;
import hrds.agent.job.biz.utils.WriterFile;
import hrds.commons.codes.DataBaseCode;
import hrds.commons.codes.FileFormat;
import hrds.commons.entity.Data_extraction_def;
import hrds.commons.exception.AppSystemException;
import hrds.commons.utils.Constant;
import org.apache.avro.file.DataFileWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JdbcToIncrementFileWriter
 * date: 2020/4/8 16:39
 * author: zxz
 */
public class JdbcToIncrementFileWriter extends AbstractFileWriter {
	//打印日志
	private static final Log log = LogFactory.getLog(JdbcToIncrementFileWriter.class);

	public JdbcToIncrementFileWriter(ResultSet resultSet, CollectTableBean collectTableBean, int pageNum,
	                                 TableBean tableBean, Data_extraction_def data_extraction_def) {
		super(resultSet, collectTableBean, pageNum, tableBean, data_extraction_def);
	}

	@Override
	public String writeFiles() {
		DataFileWriter<Object> avroWriter = null;
		BufferedWriter writer;
		long counter = 0;
		int index = 0;
		WriterFile writerFile = null;
		StringBuilder fileInfo = new StringBuilder(1024);
		String hbase_name = collectTableBean.getHbase_name();
		String eltDate = collectTableBean.getEtlDate();
		//数据抽取指定的目录
		String plane_url = data_extraction_def.getPlane_url();
		String midName = plane_url + File.separator + eltDate + File.separator + collectTableBean.getTable_name()
				+ File.separator + Constant.fileFormatMap.get(FileFormat.DingChang.getCode()) + File.separator;
		try {
			String database_code = data_extraction_def.getDatabase_code();
			midName = FileNameUtils.normalize(midName, true);
			//卸数文件名为hbase_name加线程唯一标识加此线程创建文件下标
			String fileName = midName + hbase_name + pageNum + index + "." + data_extraction_def.getFile_suffix();
			fileInfo.append(fileName).append(Constant.METAINFOSPLIT);
			writerFile = new WriterFile(fileName);
			writer = writerFile.getIncrementBufferedWriter(DataBaseCode.ofValueByCode(database_code));
			/* Get result set metadata */
			List<String> queryColumnList = new ArrayList<>();
			Map<String, Integer> typeValueMap = new HashMap<>();
			ResultSetMetaData rsMetaData = resultSet.getMetaData();
			//获取查询的列，放到集合中
			for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
				queryColumnList.add(rsMetaData.getColumnName(i).toUpperCase());
				typeValueMap.put(rsMetaData.getColumnName(i).toUpperCase(), rsMetaData.getColumnType(i));
			}
			avroWriter = getAvroWriter(typeValueMap, hbase_name, midName, pageNum);
			List<String> allColumnList = StringUtil.split(tableBean.getColumnMetaInfo(),
					Constant.METAINFOSPLIT);
			List<Integer> allLengthList = stringToIntegerList(StringUtil.split(tableBean.getColLengthInfo(),
					Constant.METAINFOSPLIT));
			StringBuilder sb_ = new StringBuilder();//用来写临时数据
			StringBuilder line = new StringBuilder();//用来写一行数据
			String operate = tableBean.getOperate();
			while (resultSet.next()) {
				//最前面拼接操作方式
				line.append(operate);
				counter++;
				for (int i = 0; i < allColumnList.size(); i++) {
					if (queryColumnList.contains(allColumnList.get(i))) {
						//如果是查询的列，取值拼接
						getOneColumnValue(avroWriter, counter, pageNum, resultSet,
								typeValueMap.get(allColumnList.get(i)), sb_, allColumnList.get(i), hbase_name, midName);
						line.append(columnToFixed(sb_.toString(), allLengthList.get(i), database_code));
						sb_.delete(0, sb_.length());
					} else {
						//如果不是查询的列，直接拼空值
						line.append(columnToFixed(" ", allLengthList.get(i), database_code));
					}
				}
				line.append(data_extraction_def.getRow_separator());
				if (JobConstant.WriteMultipleFiles) {
					long messageSize = line.toString().length();
					long singleFileSize = new File(fileName).length();
					if (singleFileSize + messageSize > JobConstant.FILE_BLOCKSIZE) {
						//当文件满足阈值时 ，然后关闭当前流，并创建新的数据流
						writerFile.bufferedWriterClose();
						index++;
						fileName = midName + hbase_name + pageNum + index + "." + data_extraction_def.getFile_suffix();
						writerFile = new WriterFile(fileName);
						writer = writerFile.getBufferedWriter(DataBaseCode.ofValueByCode(
								database_code));
						fileInfo.append(fileName).append(Constant.METAINFOSPLIT);
					}
				}
				writer.write(line.toString());
				if (counter % JobConstant.BUFFER_ROW == 0) {
					log.info("正在写入文件，已写入" + counter + "行");
					writer.flush();
				}
				line.delete(0, line.length());
			}
			writer.flush();
		} catch (Exception e) {
			log.error("表" + collectTableBean.getTable_name() + "数据库增量抽取卸数文件失败", e);
			throw new AppSystemException("数据库增量抽取卸数文件失败", e);
		} finally {
			try {
				if (writerFile != null)
					writerFile.incrementBufferedWriterClose();
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
