package hrds.agent.job.biz.core.dfstage.fileparser;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.utils.StringUtil;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.TableBean;
import hrds.agent.job.biz.core.dfstage.fileparser.impl.*;
import hrds.commons.codes.FileFormat;
import hrds.commons.exception.AppSystemException;

/**
 * FileWriterFactory
 * date: 2019/12/6 17:09
 * author: zxz
 */
@DocClass(desc = "卸数，写文件的工厂", author = "zxz", createdate = "2019/12/6 17:09")
public class FileParserFactory {

	private FileParserFactory() {

	}

	public static FileParserInterface getFileParserImpl(TableBean tableBean, CollectTableBean
			collectTableBean, String readFile) throws Exception {
		String format = collectTableBean.getSourceData_extraction_def().getDbfile_format();
		FileParserInterface fileParserInterface;
		if (FileFormat.CSV.getCode().equals(format)) {
			//写CSV文件实现类
			fileParserInterface = new CsvFileParserDeal(tableBean, collectTableBean, readFile);
		} else if (FileFormat.ORC.getCode().equals(format)) {
			//写ORC文件实现类
			fileParserInterface = new OrcFileParserDeal(tableBean, collectTableBean, readFile);
		} else if (FileFormat.PARQUET.getCode().equals(format)) {
			//写PARQUET文件实现类
			fileParserInterface = new ParquetFileParserDeal(tableBean, collectTableBean, readFile);
		} else if (FileFormat.SEQUENCEFILE.getCode().equals(format)) {
			//写SEQUENCE文件实现类
			fileParserInterface = new SequenceFileParserDeal(tableBean, collectTableBean, readFile);
		} else if (FileFormat.DingChang.getCode().equals(format)) {
			//定长文件如果列分隔符不为空，按非定长处理
			if (!StringUtil.isEmpty(tableBean.getColumn_separator())) {
				//写非定长文件实现类
				fileParserInterface = new NonFixedFileParserDeal(tableBean, collectTableBean, readFile);
			} else {
				//写定长文件实现类
				fileParserInterface = new FixedFileParserDeal(tableBean, collectTableBean, readFile);
			}
		} else if (FileFormat.FeiDingChang.getCode().equals(format)) {
			//写非定长文件实现类
			fileParserInterface = new NonFixedFileParserDeal(tableBean, collectTableBean, readFile);
		} else {
			throw new AppSystemException("系统仅支持落地CSV/PARQUET/ORC/SEQUENCE/定长/非定长数据文件");
		}
		return fileParserInterface;
	}
}
