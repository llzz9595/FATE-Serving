package com.webank.ai.fate.serving.guest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;

import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.bean.ModelInfo;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.rpc.FederatedRpcInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author kaideng
 **/
@FateService(name ="batchInferenece",  preChain= {
//        "overloadMonitor",
//        "batchParamInterceptor",
        "guestBatchParamInterceptor",
//        "federationModelInterceptor",
        "guestModelInterceptor",
//        "federationRouterService"
        "federationRouterInterceptor"
      },postChain = {
        "defaultPostProcess"
})
@Service
public class BatchGuestInferenceProvider extends AbstractServiceAdaptor<BatchInferenceRequest,BatchInferenceResult>{

    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;

    @Override
    public BatchInferenceResult doService(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {

        Model  model = context.getModel();

        Preconditions.checkArgument(model!=null);
        /**
         * 用于替代原来的pipelineTask
         */
        ModelProcessor modelProcessor = model.getModelProcessor();


        BatchInferenceRequest   batchInferenceRequest =(BatchInferenceRequest)inboundPackage.getBody();
        /**
         *  发往对端的参数
         */
        BatchHostFederatedParams  batchHostFederatedParams = buildBatchHostFederatedParams( context,batchInferenceRequest);

        /**
         * guest 端与host同步预测，再合并结果
         */

        ListenableFuture<Proxy.Packet> originBatchResultFuture = federatedRpcInvoker.asyncBatch(context,batchHostFederatedParams);

//        BatchFederatedResult    batchFederatedResult = modelProcessor.batchPredict(context,batchInferenceRequest,originBatchResultFuture);
        BatchInferenceResult batchFederatedResult = modelProcessor.guestBatchInference(context, batchInferenceRequest, originBatchResultFuture);

        return  batchFederatedResult;
    }




    @Override
    protected BatchInferenceResult transformErrorMap(Context context, Map data) {
        return null;
    }
}
