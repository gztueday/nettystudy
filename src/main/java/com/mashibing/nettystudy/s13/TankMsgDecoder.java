package com.mashibing.nettystudy.s13;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class TankMsgDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		//写过来至少有8个字节
		//解决了TCP拆包、粘包的问题
		if(in.readableBytes()<8) return;
		//in.markReaderIndex();
		
		//因为先写的x，后写的y。所以读的时候，先读出来x，再读出来y
		int x = in.readInt();
		int y = in.readInt();
		
		//你把那个消息解析出来的对象，全都装到list里就可以了
		out.add(new TankMsg(x, y));
	}
}
