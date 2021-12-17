package org.apache.apisix.plugin.runner;

import com.google.flatbuffers.FlatBufferBuilder;
import io.github.api7.A6.ExtraInfo.Info;
import io.github.api7.A6.ExtraInfo.ReqBody;
import io.github.api7.A6.PrepareConf.Req;

import java.nio.ByteBuffer;

public class ExtraInfoRequest implements A6Response{

    private final String var;

    private final Boolean reqBody;

    public ExtraInfoRequest(String var, Boolean reqBody) {
        this.var = var;
        this.reqBody = reqBody;
    }


    @Override
    public ByteBuffer encode() {
        FlatBufferBuilder builder = new FlatBufferBuilder();

        if (var != null) {
            int nameOffset = builder.createString(var);
            io.github.api7.A6.ExtraInfo.Var.startVar(builder);
            io.github.api7.A6.ExtraInfo.Var.addName(builder, nameOffset);
            int endVar = io.github.api7.A6.ExtraInfo.Var.endVar(builder);
            buildExtraInfo(endVar, Info.Var, builder);
        }

        if(this.reqBody != null && this.reqBody) {
            io.github.api7.A6.ExtraInfo.ReqBody.startReqBody(builder);
            int reqBodyReq = ReqBody.endReqBody(builder);
            buildExtraInfo(reqBodyReq, Info.ReqBody, builder);
        }

        builder.finish(Req.endReq(builder));
        return builder.dataBuffer();
    }

    private void buildExtraInfo(int info, byte type, FlatBufferBuilder builder) {
        io.github.api7.A6.ExtraInfo.Req.startReq(builder);
        io.github.api7.A6.ExtraInfo.Req.addInfoType(builder, type);
        io.github.api7.A6.ExtraInfo.Req.addInfo(builder, info);
    }

    @Override
    public byte getType() {
        return 3;
    }
}
