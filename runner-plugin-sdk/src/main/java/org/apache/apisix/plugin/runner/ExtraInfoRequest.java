/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.apisix.plugin.runner;

import com.google.flatbuffers.FlatBufferBuilder;
import io.github.api7.A6.ExtraInfo.Info;
import io.github.api7.A6.ExtraInfo.ReqBody;
import io.github.api7.A6.PrepareConf.Req;

import java.nio.ByteBuffer;

public class ExtraInfoRequest implements A6Response {

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

        if (this.reqBody != null && this.reqBody) {
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
