package com.webank.ai.fate.serving.core.rpc.core;

import com.webank.ai.fate.serving.core.bean.Context;

public interface Interceptor<req,resp> {

    public void doPreProcess(Context context, InboundPackage<req> inboundPackage, OutboundPackage<resp> outboundPackage) throws Exception;

    public void doPostProcess(Context context,InboundPackage<req> inboundPackage,OutboundPackage<resp> outboundPackage) throws Exception;

}
