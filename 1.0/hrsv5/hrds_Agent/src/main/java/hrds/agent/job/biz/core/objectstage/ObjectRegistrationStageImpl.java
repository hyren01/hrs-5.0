package hrds.agent.job.biz.core.objectstage;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Return;
import hrds.agent.job.biz.bean.StageParamInfo;
import hrds.agent.job.biz.constant.StageConstant;
import hrds.agent.job.biz.core.AbstractJobStage;

@DocClass(desc = "半结构化对象采集登记是否成功实现类", author = "zxz", createdate = "2019/10/24 14:27")
public class ObjectRegistrationStageImpl extends AbstractJobStage {

	@Method(desc = "半结构化对象采集，数据登记阶段实现，处理完成后，无论成功还是失败，" +
			"将相关状态信息封装到StageStatusInfo对象中返回", logicStep = "")
	@Return(desc = "StageStatusInfo是保存每个阶段状态信息的实体类", range = "不会为null,StageStatusInfo实体类对象")
	@Override
	public StageParamInfo handleStage(StageParamInfo stageParamInfo) {
		return null;
	}

	@Override
	public int getStageCode(){
		return StageConstant.DATAREGISTRATION.getCode();
	}
}
